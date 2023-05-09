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

    public List<Commit> bugListRefFile(String pathOfFile, ReleaseTag taggedReleaseToGetCommit, ReleaseTag previousTaggedRelease, Boolean isFirst) throws IOException, GitAPIException, ParseException {
        List<Commit> commitsToReturn = new ArrayList<>();

        Git git = taggedReleaseToGetCommit.getCurrentRepo().getGitHandle();

        // Retrieve Ref object of passed tag
        Ref currentTag = git.getRepository().exactRef(taggedReleaseToGetCommit.getGitTag());
        Ref previousTag = git.getRepository().exactRef(previousTaggedRelease.getGitTag());

        // Retrieve ref for lightweight tag (used in Syncope)
        Ref peeledRefcurrent = git.getRepository().peel(currentTag);
        Ref peeledRefprevious = git.getRepository().peel(previousTag);

        // Init commit list
        Iterable<RevCommit> logs = new ArrayList<>();

        if(Boolean.TRUE.equals(isFirst)){
            // If I'm getting the files for the first tagged release, I don't set the tag's range
            logs = git.log()
                    .addPath( pathOfFile )      // File name to retrieve commits
                    .call();
        }
        else{
            logs = git.log()
                    .addPath( pathOfFile )                                                          // File name to retrieve commits
                    .addRange(peeledRefprevious.getPeeledObjectId(), peeledRefcurrent.getPeeledObjectId())      // Set tag range
                    .call();
        }


        // Scroll commit
        for (RevCommit rev : logs) {
            // Init commit to add
            Commit commitToAdd = new Commit();
            commitToAdd.setCommitFromGit(rev);

            // Add commit to commit list for passed file
            commitsToReturn.add(commitToAdd);
        }

        // Reverse commit list
        Collections.reverse(commitsToReturn);

        return commitsToReturn;
    }


    public Bug commitJiraGitLinker(RevCommit commit, List<Bug> bugList){

        // Scroll all valid bug and find match with passed commit
        for(Bug validBugIndex: bugList){

            // If commit's message contains bug name like 'BOOKKEEPER-46', there is a match
            if(commit.getShortMessage().matches("(.*)" + validBugIndex.getNameKey()  + "(.*)")){return validBugIndex;}

        }

        // If there isn't match, return a default bug error
        return new Bug("NOMATCH");
    }

}
