package controller;

import model.Commit;
import model.Metrics;
import model.ReleaseTag;
import model.Repo;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MetricsRetriever {


    public Metrics metricsHelper(ReleaseTag taggedReleaseToGetFileMetrics, ReleaseTag previousTaggedReleaseToGetFileMetrics, Boolean isFirst, TreeWalk treeWalk, List<Commit> relatedCommitsOfCurrentTaggedRelease) throws IOException, GitAPIException {

        // Retrieve LOC Metric -------------------------
        int locMetric = locMetric(taggedReleaseToGetFileMetrics, taggedReleaseToGetFileMetrics.getCurrentRepo().getjGitRepository().findRef("HEAD").getObjectId(), treeWalk.getPathString());

        // Retrieve others LOC Metrics -----------------
        List<Integer> locMetrics = locOtherMetrics(taggedReleaseToGetFileMetrics, previousTaggedReleaseToGetFileMetrics, treeWalk, isFirst, relatedCommitsOfCurrentTaggedRelease);
        // Integer list where:
        //      1' Element: LOC Added
        //      2' Element: Max LOC Added
        //      3' Element: LOC Touched
        //      4' Element: LOC Deleted
        //      5' Element: Churn
        //      6' Element: Max Churn
        int locAdded = locMetrics.get(0);
        int locMaxAdded = locMetrics.get(1);
        int locTouchedMetric = locMetrics.get(2);
        int locDeleted = locMetrics.get(3);

        // Retrieve Number of Revision -----------------
        int numberRevision = relatedCommitsOfCurrentTaggedRelease.size();

        // Retrieve Average LOC Added ------------------
        float averageLocAdded = 0;
        if(numberRevision>0){averageLocAdded=(float)locAdded / numberRevision;};

        // Retrieve Nauth Metric -----------------------
        int nAuthMetric = numberAuthorsMetric(relatedCommitsOfCurrentTaggedRelease);

        // Retrieve Churn Metric -----------------------
        int churn = locMetrics.get(4);

        // Retrieve Max Churn Metric -------------------
        int maxChurn = locMetrics.get(5);

        // Average Churn Metric ------------------------
        float averageChurn = 0;
        if(numberRevision>0){averageChurn=(float)churn / numberRevision;};


        return new Metrics(
                locMetric,
                locTouchedMetric,
                nAuthMetric,
                locAdded,
                locMaxAdded,
                numberRevision,
                averageLocAdded,
                churn,
                maxChurn,
                averageChurn
        );
    }


    private int locMetric(ReleaseTag taggedReleaseToGetFileMetrics, ObjectId objectId, String pathOfFile) throws IOException {
        // Size (LOC):
        //      Lines of code of current file on the current release.
        Repository repository = taggedReleaseToGetFileMetrics.getCurrentRepo().getjGitRepository();

        int nline = 0;

        RevWalk walk = new RevWalk(repository);

        RevCommit commit = walk.parseCommit(objectId);
        RevTree tree = commit.getTree();

        TreeWalk treeWalk = new TreeWalk(repository);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);

        while (treeWalk.next()) {
            if (treeWalk.getPathString().equals(pathOfFile)) {

                ObjectLoader loader = repository.open(treeWalk.getObjectId(0));

                // loader.openstream passa un inputstream del laoder
                BufferedReader reader = new BufferedReader(new InputStreamReader(loader.openStream()));
                while(reader.ready()) {
                    String line = reader.readLine();
                    nline = nline + 1;
                }

                break;
            }
        }

        System.out.println(nline);
        return nline;
    }



    private List<Integer> locOtherMetrics(ReleaseTag taggedReleaseToGetFileMetrics, ReleaseTag previousTaggedReleaseToGetFileMetrics, TreeWalk treeWalk, Boolean isFirst, List<Commit> relatedCommitsOfCurrentTaggedRelease) throws IOException, GitAPIException {
        // LOC Touched:
        //      sum over revisions of LOC added and deleted.

        Repository repository = taggedReleaseToGetFileMetrics.getCurrentRepo().getjGitRepository();
        Git git = taggedReleaseToGetFileMetrics.getCurrentRepo().getGitHandle();

        // The diff works on TreeIterators, we prepare two for the two tagged release
        JGitHelper jgh = new JGitHelper();

        AbstractTreeIterator oldTreeParser = jgh.prepareTreeParser(repository, previousTaggedReleaseToGetFileMetrics.getRefObjectIdGetName());
        AbstractTreeIterator newTreeParser = jgh.prepareTreeParser(repository, taggedReleaseToGetFileMetrics.getRefObjectIdGetName());

        DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
        df.setRepository(repository);
        df.setDiffComparator(RawTextComparator.DEFAULT);
        df.setDetectRenames(true);

        // If it's the first Release and there are commit related to the current file
        if(isFirst && !relatedCommitsOfCurrentTaggedRelease.isEmpty()){return locOtherMetricsFirstRelease(relatedCommitsOfCurrentTaggedRelease, treeWalk,  df, taggedReleaseToGetFileMetrics);}

        List<DiffEntry> diffs = git.diff().
                setOldTree(oldTreeParser).
                setNewTree(newTreeParser).
                setPathFilter(PathFilter.create( treeWalk.getPathString() )).   // Set path of file from treeWalk instance
                        call();

        // Calc and return LOC Based Metrics
        return locBasedMetricsCalculator(diffs, df);

        //System.out.println("\nADDED: " + linesAdded + " | MAX: " + maxLocAdded + " | TOUCHED: " + (linesDeleted + linesAdded));

    }

    private List<Integer> locOtherMetricsFirstRelease(List<Commit> relatedCommitsOfCurrentTaggedRelease, TreeWalk treeWalk,  DiffFormatter df, ReleaseTag taggedReleaseToGetFileMetrics) throws IOException {

        int linesAdded = 0;
        int linesDeleted = 0;
        int firstCommitLocAdded = 0;
        int maxLocAdded = 0;
        int maxChurn = 0;
        int totalChurn = 0;

        List<Integer> locMetrics = new ArrayList<>(Arrays.asList(0,0,0,0,0,0));

        Repository repository = taggedReleaseToGetFileMetrics.getCurrentRepo().getjGitRepository();


        // Scroll all current file's commit and count the added and deleted line,
        // but this not count the first commit changes
        for(int j=relatedCommitsOfCurrentTaggedRelease.size()-1; j>0; j--){
            RevCommit commit = relatedCommitsOfCurrentTaggedRelease.get(j-1).getCommitFromGit();
            RevCommit parent = relatedCommitsOfCurrentTaggedRelease.get(j).getCommitFromGit();

            // Select only file current file path
            df.setPathFilter(PathFilter.create( treeWalk.getPathString() ));
            List<DiffEntry> diffs = df.scan(parent.getTree(), commit.getTree());

            // Calc loc Metrics
            List<Integer> locMetricsTemp = locBasedMetricsCalculator(diffs, df);

            // Calc LOC Added and Deleted
            linesAdded += locMetricsTemp.get(0);
            linesDeleted += locMetricsTemp.get(3);

            // Check max LOC Added
            if(locMetricsTemp.get(1)>maxLocAdded){maxLocAdded=locMetricsTemp.get(1);}

            // Calc total Churn
            totalChurn += locMetricsTemp.get(4);

            // Check max Churn
            if(locMetricsTemp.get(5)>maxChurn){maxChurn=locMetricsTemp.get(5);}
        }

        // ------ First Commit Consideration ------
        // If the file it's created in this release return the loc number of file in the first commit
        // Position on the latest commit of current tagged Release
        ObjectId commitId = repository.resolve(relatedCommitsOfCurrentTaggedRelease.get(relatedCommitsOfCurrentTaggedRelease.size()-1).getCommitFromGit().getId().getName());
        firstCommitLocAdded = locMetric(taggedReleaseToGetFileMetrics, commitId, treeWalk.getPathString());

        // Check max LOC Added
        if(firstCommitLocAdded>maxLocAdded){maxLocAdded=firstCommitLocAdded;}

        // #NOTE-TO-THINKING-OF:
        //      Se il file è stato creato in una release precedente, non prendo in considerazione
        //      le modifiche sul primo commit, perchè non prendendo in considerazione tutte le release
        //      non saprei con quale commit fare il confronto se appartenesse a una release che non faccio
        //      riferimento da Jira.

        locMetrics.set(0,linesAdded);
        locMetrics.set(1,maxLocAdded);
        locMetrics.set(2,(linesDeleted + linesAdded + firstCommitLocAdded));
        locMetrics.set(3,linesDeleted);
        locMetrics.set(4,totalChurn);
        locMetrics.set(5,maxChurn);

        System.out.println("\nADDED: " + linesAdded + " | MAX: " + maxLocAdded + " | TOUCHED: " + (linesDeleted + linesAdded + firstCommitLocAdded) + " | FIRSTADD: " + firstCommitLocAdded);

        return locMetrics;
    }

    private List<Integer> locBasedMetricsCalculator(List<DiffEntry> diffs, DiffFormatter df) throws IOException {

        // 'locMetrics' is a Integer List, where every element is a metric to return:
        //      1' Element: LOC Added
        //      2' Element: Max LOC Added
        //      3' Element: LOC Touched
        //      4' Element: LOC Deleted
        //      5' Element: Churn
        //      6' Element: Max Churn
        List<Integer> locMetrics = new ArrayList<>(Arrays.asList(0,0,0,0,0,0));

        int linesAdded = 0;
        int linesDeleted = 0;
        int firstCommitLocAdded = 0;
        int maxLocAdded = 0;
        int maxChurn = 0;
        int totalChurn = 0;

        for (DiffEntry diff : diffs) {
            for (Edit edit : df.toFileHeader(diff).toEditList()) {
                int tempLinesDeleted = edit.getEndA() - edit.getBeginA();
                int tempLinesAdded = edit.getEndB() - edit.getBeginB();

                // Calc LOC Added and Deleted
                linesAdded += tempLinesAdded;
                linesDeleted += tempLinesDeleted;

                // Check max LOC Added
                if(tempLinesAdded>maxLocAdded){maxLocAdded=tempLinesAdded;}

                // Calc current churn
                int currentChurn = Math.abs(tempLinesAdded-tempLinesDeleted);
                // Calc total Churn
                totalChurn += currentChurn;
                // Check max Churn
                if(currentChurn>maxChurn){maxChurn=currentChurn;}
            }
        }

        locMetrics.set(0,linesAdded);
        locMetrics.set(1,maxLocAdded);
        locMetrics.set(2,(linesDeleted + linesAdded));
        locMetrics.set(3,linesDeleted);
        locMetrics.set(4,totalChurn);
        locMetrics.set(5,maxChurn);

        return locMetrics;
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
