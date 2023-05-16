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
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReleaseTagRetriever {
    private static final Logger LOGGER = Logger.getLogger( ReleaseTagRetriever.class.getName() );


    public List<ReleaseTag> makeTagReleasesList(Repo project, List<Release> listOfJiRelease){

        // Init list of ragged release's objects
        List<ReleaseTag> listOfTagReleases = new ArrayList<>();


        for (Release rlsIndex : listOfJiRelease) {
            String tagPathName = project.getPrefixTagPath() + rlsIndex.getName();

            // Set, Create and Add ReleaseTag objetc to valid tagged release list
            listOfTagReleases.add(new ReleaseTag(
                    tagPathName,                    // Set tag name path
                    rlsIndex,                       // Set Release associated object
                    project                        // Set Repo objects
            ));

            LOGGER.log(Level.INFO, ("\nAdded Tag: " + tagPathName));
        }

        return listOfTagReleases;
    }


    public List<ReleaseTag> setBugginess(List<ReleaseTag> tagRelesesToDoThinks, String fileName, List<Release> affectedVersions, List<Release> released, Boolean doOnALLRelease){

        // Scroll all file of all Release
        for(ReleaseTag rlsIndex : tagRelesesToDoThinks){
            // Find a ReleaseTag Object that match with one of passed affected version
            if(affectedVersions.stream().anyMatch(o -> rlsIndex.getReleaseFromJira().getName().equals(o.getName()))){
                for(RepoFile rpIndex : rlsIndex.getReferencedFilesList()){
                    // Control for Walk Forward bugginess
                    if(!doOnALLRelease && (rlsIndex.getReleaseFromJira().getIndex() >= released.size())){break;}

                    // Find that file that it will set buggy and correspond to the passed path
                    if(rpIndex.getNameFile().equals(fileName)){rpIndex.setItsBuggy(true); break;}
                }
            }
        }

        return tagRelesesToDoThinks;
    }

}
