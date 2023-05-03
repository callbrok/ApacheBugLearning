package controller;

import model.Repo;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JGitHelper {

    // Initialize project's repo object
    private static final List<Repo> GITPROJECTS = new ArrayList<>() {{
        add(new Repo("BOOKKEEPER", "https://github.com/apache/bookkeeper.git"));
        add(new Repo("AVRO", "https://github.com/apache/avro.git"));
        add(new Repo("STORM", "https://github.com/apache/storm.git"));
        add(new Repo("OPENJPA", "https://github.com/apache/openjpa.git"));
        add(new Repo("ZOOKEEPER", "https://github.com/apache/zookeeper.git"));
        add(new Repo("SYNCOPE", "https://github.com/apache/syncope.git"));
        add(new Repo("TAJO", "https://github.com/apache/tajo.git"));
    }};

    public Repo getJGitRepository(String projectName) throws IOException, GitAPIException {
        // Init Repo to clone and return object
        Repo repoToCloneReturn = null;

        // Find Repo from list
        for(Repo repoIndex: GITPROJECTS){
            if(repoIndex.getApacheProjectName().equals(projectName)){repoToCloneReturn=repoIndex;break;}
        }

        // IMPLEMENTARE SISTEMA DI ERRORE CHE MANDA IN BREAK IL SISTEMA
        // If passed projectName doesn't match, return an error
        if(repoToCloneReturn == null){System.out.print("\nERRORE | Il progetto '" + projectName + "' passato NON CORRISPONDE A NESSUN PROGETTO ANALIZZABILE.\n"); return null;}

        // Clone and set the local path where repo was cloned
        //repoToCloneReturn.setPathOfRepo(cloneRepository(repoToCloneReturn));

        // FOR TESTING
        repoToCloneReturn.setPathOfRepo(new File("C:\\Users\\Marco\\GitHub\\bookkeeper"));

        FileRepositoryBuilder builder = new FileRepositoryBuilder();

        // Set Repository object
        repoToCloneReturn.setjGitRepository(builder
                .readEnvironment() // Scan environment GIT_* variables
                .findGitDir(repoToCloneReturn.getPathOfLocalRepo()) // Scan up the file system tree
                .build());

        // FOR TESTING
        Git git = new Git(repoToCloneReturn.getjGitRepository());
        repoToCloneReturn.setGitHandle(git);

        return repoToCloneReturn;
    }

    private File cloneRepository(Repo repoToClone) throws IOException, GitAPIException {

        // Prepare new folder for the cloned repository
        File localPath = File.createTempFile("MarcoPurificato_Retriever" + repoToClone.getApacheProjectName() + "_", "");

        if(!localPath.delete()) {
            throw new IOException("Could not delete temporary file " + localPath);
        }

        // Clone repo
        System.out.println("Cloning from " + repoToClone.getGitUrl() + " to " + localPath);
        try (Git result = Git.cloneRepository()
                .setURI(repoToClone.getGitUrl())
                .setDirectory(localPath)
                .setProgressMonitor(new SimpleProgressMonitor())
                .call()) {
            // Note: the call() returns an opened repository already which needs to be closed to avoid file handle leaks!
            System.out.println("Having repository: " + result.getRepository().getDirectory());

            // Set gitHandle for the repo
            //
            // #NOTE-TO-THINKING-OF:
            //      Mi tengo l'handle del progetto corrente cosi da non riaprirlo ogni volta
            //      e chiuderlo alla fine facilmente
            repoToClone.setGitHandle(result);
        }

        return localPath;
    }

    public void deleteRepository(Repo projectRepo) throws IOException {
        // Close handle
        projectRepo.getGitHandle().close();
        System.out.println("\n\nHANDLE CLOSED");

        // clean up here to not keep using more and more disk-space for these samples
        //FileUtils.deleteDirectory(projectRepo.getPathOfLocalRepo());
        //System.out.println("\nREPO DELETED");
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