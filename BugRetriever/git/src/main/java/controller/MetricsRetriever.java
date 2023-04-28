package controller;

import model.Commit;
import model.Metrics;
import model.ReleaseTag;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MetricsRetriever {


    public Metrics metricsHelper(ReleaseTag taggedReleaseToGetFileMetrics, ReleaseTag previousTaggedReleaseToGetFileMetrics, Boolean isFirst, TreeWalk treeWalk, List<Commit> relatedCommitsOfCurrentTaggedRelease) throws IOException, GitAPIException {
        Metrics metricsToReturn = new Metrics();


        // Retrieve LOC Metric -------------------------
        int locMetric = locMetric(taggedReleaseToGetFileMetrics.getCurrentRepo().getjGitRepository(), treeWalk);

        // Retrieve LOC Touched Metric -----------------
        int locTouchedMetric;
        // If is the first Release we don't have another older Release to compare with, so we take the number of code line (LOC)
        if(isFirst){locTouchedMetric=locMetric;}
        else{locTouchedMetric = locTouchedMetric(taggedReleaseToGetFileMetrics, previousTaggedReleaseToGetFileMetrics, treeWalk);}

        // Retrieve Nauth Metric -----------------------
        int nAuthMetric = numberAuthorsMetric(relatedCommitsOfCurrentTaggedRelease);




        

        metricsToReturn.setLoc(locMetric);
        metricsToReturn.setLocTouched(locTouchedMetric);
        metricsToReturn.setnAuth(nAuthMetric);

        return metricsToReturn;
    }


    private int locMetric(Repository repository, TreeWalk treeWalk) throws IOException {
        // Size (LOC):
        //      Lines of code of current file on the current release.

        ObjectLoader loader = repository.open(treeWalk.getObjectId(0));
        int lineNumber = 0;

        // 'loader.openstream' pass an inputstream object to loader
        BufferedReader reader = new BufferedReader(new InputStreamReader(loader.openStream()));

        while(reader.ready()) {
            String line = reader.readLine();
            lineNumber = lineNumber + 1;
        }

        System.out.println("loc: " + lineNumber);

        return lineNumber;
    }


    private int locTouchedMetric(ReleaseTag taggedReleaseToGetFileMetrics, ReleaseTag previousTaggedReleaseToGetFileMetrics, TreeWalk treeWalk) throws GitAPIException, IOException {
        // LOC Touched:
        //      sum over revisions of LOC added and deleted.

        Repository repository = taggedReleaseToGetFileMetrics.getCurrentRepo().getjGitRepository();
        Git git = taggedReleaseToGetFileMetrics.getCurrentRepo().getGitHandle();

        // The diff works on TreeIterators, we prepare two for the two tagged release
        JGitHelper jgh = new JGitHelper();

        AbstractTreeIterator oldTreeParser = jgh.prepareTreeParser(repository, previousTaggedReleaseToGetFileMetrics.getRefObjectIdGetName());
        AbstractTreeIterator newTreeParser = jgh.prepareTreeParser(repository, taggedReleaseToGetFileMetrics.getRefObjectIdGetName());

        int linesAdded = 0;
        int linesDeleted = 0;

        DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
        df.setRepository(repository);
        df.setDiffComparator(RawTextComparator.DEFAULT);
        df.setDetectRenames(true);

        List<DiffEntry> diffs = git.diff().
                setOldTree(oldTreeParser).
                setNewTree(newTreeParser).
                setPathFilter(PathFilter.create( treeWalk.getPathString() )).   // Set path of file from treeWalk instance
                        call();

        for (DiffEntry diff : diffs) {
            for (Edit edit : df.toFileHeader(diff).toEditList()) {
                linesDeleted += edit.getEndA() - edit.getBeginA();
                linesAdded += edit.getEndB() - edit.getBeginB();
            }
        }

        System.out.println("  |  locTouched: " + linesDeleted + linesAdded);

        return linesDeleted + linesAdded;
    }


    private int numberAuthorsMetric(List<Commit> relatedCommitsOfCurrentTaggedRelease){
        // Nauth:
        //      number of authors

        List<String> authorsName = new ArrayList<>();

        // Scroll all commit related to current version of file
        for(Commit commitIndex : relatedCommitsOfCurrentTaggedRelease){
            // Get author's name of current commit
            PersonIdent authorIdent = commitIndex.getCommitFromGit().getAuthorIdent();
            // Check if current author it's just added to authors list
            if(authorsName.contains(authorIdent.getName())){continue;}
            // Else add it
            authorsName.add(authorIdent.getName());
        }

        return authorsName.size();
    }




}
