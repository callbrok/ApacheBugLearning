package controller;

import model.Commit;
import model.Metrics;
import model.ReleaseTag;
import model.RepoFile;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class FileRetriever {
    private static final Boolean GETTESTCLASS = false;

    public List<RepoFile> getAllFilesOfTagRelease(ReleaseTag taggedReleaseToGetFiles, ReleaseTag previousTaggedRelease, Boolean isFirst) throws IOException, GitAPIException, ParseException {
        List<RepoFile> filesToReturn = new ArrayList<>();

        String fileExtension;
        String fileName;

        // Point repository of current passed branch
        Repository repository = taggedReleaseToGetFiles.getCurrentRepo().getjGitRepository();

        // Point tagged release with git-checkout
        taggedReleaseToGetFiles.getCurrentRepo().getGitHandle().checkout().setName( taggedReleaseToGetFiles.getGitTag() ).call();

        // Point current branch
        Ref head = repository.findRef("HEAD");


        // RevWalk allows to walk over commits based on some filtering that is defined
        RevWalk walk = new RevWalk(repository);

        RevCommit commit = walk.parseCommit(head.getObjectId());
        RevTree tree = commit.getTree();
        System.out.println("\n\n+----------------------------------------------------------------------------------------------------------------------+\n"
                + "+++ Having tree: " + tree + " for TAG: " + taggedReleaseToGetFiles.getGitTag()  +
                "\n+----------------------------------------------------------------------------------------------------------------------+\n\n" );

        // Now use a TreeWalk to iterate over all files in the Tree recursively
        // you can set Filters to narrow down the results if needed
        TreeWalk treeWalk = new TreeWalk(repository);

        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);

        // Scroll all files of tagged release, listed in the treeWalk
        while (treeWalk.next()) {

            // Take the substring after the latest '/' character, so name of the file
            fileName = treeWalk.getPathString().substring(treeWalk.getPathString().lastIndexOf("/") + 1);

            // Take the substring after the latest '.' character, so the extension of the file
            fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);

            // If isn't a Java class skip it, WE NEED ONLY JAVA CLASSES TO CALCULATE BUGGINESS
            if(!fileExtension.equals("java")){continue;}

            // Check if we need skip test class, if yes skip it
            if(!GETTESTCLASS && treeWalk.getPathString().matches("(.*)/test/(.*)")){continue;}

            // If the current file match, search related commit that corrisponde to jira bug retrieved jet
            CommitRetriever gtc = new CommitRetriever();
            List<Commit> relatedCommitsOfCurrentTaggedRelease = gtc.bugListRefFile(treeWalk.getPathString(), taggedReleaseToGetFiles, previousTaggedRelease, isFirst);

            // Check returned list of commits
            if(relatedCommitsOfCurrentTaggedRelease.isEmpty()){System.out.println("NESSUN COMMIT RELATIVO ALLA TAGGED RELEASE DEL SEGUENTE FILE");}


            // Set file's metrics
            MetricsRetriever mtr = new MetricsRetriever();
            Metrics metricsToAdd = mtr.metricsHelper(taggedReleaseToGetFiles, isFirst, treeWalk, relatedCommitsOfCurrentTaggedRelease);


            // Add file to the list
            filesToReturn.add(new RepoFile(
                    fileName,
                    new File(treeWalk.getPathString()),  // Path of current file
                    relatedCommitsOfCurrentTaggedRelease,
                    metricsToAdd,
                    false
            ));

            System.out.println(" -> found: " + treeWalk.getPathString());
        }

        return filesToReturn;
    }

}
