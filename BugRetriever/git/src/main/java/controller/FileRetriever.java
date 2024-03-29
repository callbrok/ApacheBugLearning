package controller;

import model.*;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileRetriever {
    private static final Logger LOGGER = Logger.getLogger( FileRetriever.class.getName() );


    public List<RepoFile> getAllFilesOfTagRelease(ReleaseTag taggedReleaseToGetFiles) throws Exception {
        List<RepoFile> filesToReturn = new ArrayList<>();

        Properties configurationProperties = new Properties();
        configurationProperties.load(new FileInputStream("configuration.properties"));


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
        LOGGER.log(Level.INFO, ("\n\n+----------------------------------------------------------------------------------------------------------------------+\n"
                + "+++ Having tree: " + tree + " for TAG: " + taggedReleaseToGetFiles.getGitTag()  +
                "\n+----------------------------------------------------------------------------------------------------------------------+\n\n" ));

        // Now use a TreeWalk to iterate over all files in the Tree recursively
        // you can set Filters to narrow down the results if needed
        TreeWalk treeWalk = new TreeWalk(repository);

        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);

        // Init an empty Commit list for avoid getting null list
        List<Commit> tempCommitList = new ArrayList<>();

        // Scroll all files of tagged release, listed in the treeWalk
        while (treeWalk.next()) {

            // Take the substring after the latest '/' character, so name of the file
            fileName = treeWalk.getPathString().substring(treeWalk.getPathString().lastIndexOf("/") + 1);

            // Take the substring after the latest '.' character, so the extension of the file
            fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);

            // If isn't a Java class skip it, WE NEED ONLY JAVA CLASSES TO CALCULATE BUGGINESS
            // Check if we need skip test class, if yes skip it
            if((configurationProperties.getProperty("get_test_classes").equals("false") && treeWalk.getPathString().matches("(.*)/test/(.*)")) || !fileExtension.equals("java")){continue;}


            // Add file to the list
            filesToReturn.add(new RepoFile(
                    fileName,
                    treeWalk.getPathString(),  // Path of current file
                    false,
                    tempCommitList
            ));

        }

        return filesToReturn;
    }

}
