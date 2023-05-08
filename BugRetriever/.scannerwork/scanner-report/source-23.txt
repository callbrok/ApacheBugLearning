package controller;

import model.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class ReleaseTagRetriever {

    private static List<Release> LISTOFJIRARELEASE = new ArrayList<>();

    public List<ReleaseTag> makeTagReleasesList(Repo project) throws IOException, ParseException, GitAPIException {
        ReleaseRetriever rls = new ReleaseRetriever();

        // Init list of ragged release's objects
        List<ReleaseTag> listOfTagReleases = new ArrayList<>();

        // If there aren't Jira releases, retrieve and set it
        if(LISTOFJIRARELEASE.isEmpty()){LISTOFJIRARELEASE=rls.getReleaseFromProject(project.getApacheProjectName(), true);}

        Git git = project.getGitHandle();

        System.out.println("\n\nListing all tagged release:");

        // Retrieve all project's tags ref object
        List<Ref> call = git.tagList().call();

        for (Ref ref : call) {
            Release releaseToAdd = releasesTagJiraGitLinker(ref.getName());

            // Check match with one of Release retrived by Jira
            if(releaseToAdd.getName().equals("NOMATCH")){continue;}

            // Set, Create and Add ReleaseTag objetc to valid tagged release list
            listOfTagReleases.add(new ReleaseTag(
                    ref.getName(),                  // Set tag name path
                    releaseToAdd,                   // Set Release associated object
                    project,                        // Set Repo objects
                    ref.getObjectId().getName()     // Set value of ref object in the repository
            ));

            System.out.println("\nAdded Tag: " + ref.getName());
        }

        return listOfTagReleases;
    }

    private Release releasesTagJiraGitLinker(String releaseTagName){
        // Initialize 'no-match' Release object
        Release errorRelease = new Release("NOMATCH");

        // Check if the tag name is referred to docker version, so there is into name 'docker' word
        if(releaseTagName.matches("(.*)docker(.*)")){return errorRelease;}

        // Take the tag release name form, the last substring after '-' character
        //      example. 'refs/tags/release-4.2.1' --> substring take '4.2.1'
        String versionToCheck = releaseTagName.substring(releaseTagName.lastIndexOf("-") + 1);

        // Check matches with released jira Release
        for(Release releaseIndex: LISTOFJIRARELEASE){
            if(releaseIndex.getName().equals(versionToCheck)){ return releaseIndex;}
        }

        // Return default Release, that allert no matches
        return errorRelease;
    }

    public List<ReleaseTag> setBugginess(List<ReleaseTag> tagRelesesToDoThinks, String fileName, List<Release> affectedVersions){

        // Scroll all file of all Release
        for(ReleaseTag rlsIndex : tagRelesesToDoThinks){
            // Find a ReleaseTag Object that match with one of passed affected version
            if(affectedVersions.stream().anyMatch(o -> rlsIndex.getReleaseFromJira().getName().equals(o.getName()))){
                for(RepoFile rpIndex : rlsIndex.getReferencedFilesList()){
                    // Find that file that it will set buggy and correspond to the passed path
                    if(rpIndex.getNameFile().equals(fileName)){
                        rpIndex.setItsBuggy(true);
                    }
                }
            }
        }

        return tagRelesesToDoThinks;
    }

}
