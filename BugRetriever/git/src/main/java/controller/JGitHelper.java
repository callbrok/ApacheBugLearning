package controller;

import model.Repo;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.TagOpt;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JGitHelper {
    private static final Logger LOGGER = Logger.getLogger( JGitHelper.class.getName() );
    private static final String PROJECT_PATH = "C:\\"+"Users\\"+"Marco\\"+"Desktop\\"+"syncope";


    // Initialize project's repo object
    private static final List<Repo> GITPROJECTS = new ArrayList<>(
            Arrays.asList(new Repo("BOOKKEEPER", "https://github.com/apache/bookkeeper.git"),
                    new Repo("AVRO", "https://github.com/apache/avro.git"),
                    new Repo("STORM", "https://github.com/apache/storm.git"),
                    new Repo("OPENJPA", "https://github.com/apache/openjpa.git"),
                    new Repo("ZOOKEEPER", "https://github.com/apache/zookeeper.git"),
                    new Repo("SYNCOPE", "https://github.com/apache/syncope.git"),
                    new Repo("TAJO", "https://github.com/apache/tajo.git")
            ));


    public Repo getJGitRepository(String projectName) throws IOException, GitAPIException {
        // Init Repo to clone and return object
        Repo repoToCloneReturn = null;

        // Find Repo from list
        for(Repo repoIndex: GITPROJECTS){
            if(repoIndex.getApacheProjectName().equals(projectName)){repoToCloneReturn=repoIndex;break;}
        }

        // IMPLEMENTARE SISTEMA DI ERRORE CHE MANDA IN BREAK IL SISTEMA
        // If passed projectName doesn't match, return an error
        if(repoToCloneReturn == null){
            LOGGER.log(Level.INFO, () ->  ("\nERRORE | Il progetto '" + projectName + "' passato NON CORRISPONDE A NESSUN PROGETTO ANALIZZABILE.\n"));
            return null;
        }

        // Clone and set the local path where repo was cloned


        // FOR TESTING
        repoToCloneReturn.setPathOfRepo(new File(PROJECT_PATH));


        FileRepositoryBuilder builder = new FileRepositoryBuilder();

        // Set Repository object
        repoToCloneReturn.setjGitRepository(builder
                .readEnvironment() // Scan environment GIT_* variables
                .findGitDir(repoToCloneReturn.getPathOfLocalRepo()) // Scan up the file system tree
                .build());

        // FOR TESTING
        Git git = new Git(repoToCloneReturn.getjGitRepository());
        repoToCloneReturn.setGitHandle(git);

        // Set the prefix tag
        repoToCloneReturn.setPrefixTagPath(getPrefixTag(git));

        return repoToCloneReturn;
    }

    private String getPrefixTag(Git git) throws GitAPIException {
        // Fetch all remote tag

        List<Ref> call = git.tagList().call();
        String prefixToReturn = "";

        prefixToReturn = call.get(0).getName();

        return prefixToReturn.substring(0,  prefixToReturn.indexOf('-')+1);
    }

 

    public void deleteRepository(Repo projectRepo) throws IOException {
        // Close handle
        projectRepo.getGitHandle().close();
        LOGGER.log(Level.INFO, ("\n\nHANDLE CLOSED"));

        // clean up here to not keep using more and more disk-space for these samples

    }


    public AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException {
        // From the commit we can build the tree which allows us to construct the TreeParser

        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(ObjectId.fromString(objectId));
            RevTree tree = walk.parseTree(commit.getTree().getId());

            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            try (ObjectReader reader = repository.newObjectReader()) {
                treeParser.reset(reader, tree.getId());
            }

            walk.dispose();

            return treeParser;
        }
    }
}
