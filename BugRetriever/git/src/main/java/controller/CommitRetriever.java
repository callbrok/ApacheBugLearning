package controller;

import model.Bug;
import model.Commit;
import model.Release;
import model.ReleaseTag;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CommitRetriever {

    public List<Commit> bugListRefFile(String pathOfFile, ReleaseTag taggedReleaseToGetCommit, ReleaseTag previousTaggedRelease, Boolean isFirst, List<Bug> bugList) throws IOException, GitAPIException, ParseException {
        BugRetriever gtb = new BugRetriever();
        List<Commit> commitsToReturn = new ArrayList<>();

        int commitCounter = 0;


        Git git = taggedReleaseToGetCommit.getCurrentRepo().getGitHandle();

        // Retrieve Ref object of passed tag
        Ref currentTag = git.getRepository().exactRef(taggedReleaseToGetCommit.getGitTag());
        Ref previousTag = git.getRepository().exactRef(previousTaggedRelease.getGitTag());

        // Init commit list
        Iterable<RevCommit> logs = new ArrayList<>();

        if(isFirst){
            // If I'm getting the files for the first tagged release, I don't set the tag's range
            logs = git.log()
                    .addPath( pathOfFile )      // File name to retrieve commits
                    .call();
        }
        else{
            logs = git.log()
                    .addPath( pathOfFile )                                                          // File name to retrieve commits
                    .addRange(previousTag.getPeeledObjectId(), currentTag.getPeeledObjectId())      // Set tag range
                    .call();
        }


        // Scroll commit
        for (RevCommit rev : logs) {
            // Init commit to add
            Commit commitToAdd = new Commit();
            commitToAdd.setCommitFromGit(rev);

            // Check current commit
            Bug currentBug = commitJiraGitLinker(rev, bugList);

            // Check returned bug, if the name is different from 'NOMATCH' there is a match with Jira Bug
            if(!currentBug.getNameKey().equals("NOMATCH")){commitToAdd.setCommitFromJira(currentBug);}

            // Add commit to commit list for passed file
            commitsToReturn.add(commitToAdd);

            System.out.println("     -- AddedCommit: " + rev.getShortMessage() + "  |  ID: " + rev.getId().getName());
            commitCounter++;
        }


        return commitsToReturn;
    }


    private Bug commitJiraGitLinker(RevCommit commit, List<Bug> bugList){

        // Scroll all valid bug and find match with passed commit
        for(Bug validBugIndex: bugList){

            // If commit's message contains bug name like 'BOOKKEEPER-46', there is a match
            if(commit.getShortMessage().matches("(.*)" + validBugIndex.getNameKey()  + "(.*)")){return validBugIndex;}

        }

        // If there isn't match, return a default bug error
        return new Bug("NOMATCH");
    }

}
