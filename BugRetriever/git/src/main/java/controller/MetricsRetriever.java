package controller;

import model.*;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.*;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MetricsRetriever {

    public Metrics metricsHelper(ReleaseTag taggedReleaseToGetFileMetrics, ReleaseTag previousTaggedReleaseToGetFileMetrics, Boolean isFirst, TreeWalk treeWalk, List<Commit> relatedCommitsOfCurrentTaggedRelease) throws Exception {

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



    private List<Integer> locOtherMetrics(ReleaseTag taggedReleaseToGetFileMetrics, ReleaseTag previousTaggedReleaseToGetFileMetrics, TreeWalk treeWalk, Boolean isFirst, List<Commit> relatedCommitsOfCurrentTaggedRelease) throws Exception {
        // LOC Touched:
        //      sum over revisions of LOC added and deleted.
        List<Integer> locMetrics = new ArrayList<>(Arrays.asList(0,0,0,0,0,0));

        int linesAdded = 0;
        int linesDeleted = 0;
        int linesMaxAdded = 0;
        int churn = 0;
        int churnMax = 0;

        Repository repository = taggedReleaseToGetFileMetrics.getCurrentRepo().getjGitRepository();

        // Scori tutti i commit che fanno riferimento a quel file
        for(int k=0; k<relatedCommitsOfCurrentTaggedRelease.size(); k++){

            RevCommit commit = relatedCommitsOfCurrentTaggedRelease.get(k).getCommitFromGit();
            RevCommit parent;

            // #NOTE-TO-THINKING-OF:
            //      If is the first commmit and there is a previous release of file, take parent for the first commit
            //      the latest commit of previous release file
            if(k==0 && !isFirst){
                parent = takeLatestCommitOfTag(previousTaggedReleaseToGetFileMetrics, treeWalk.getPathString());
                // If parent return null, the previous release doesn't have a commit
                if(parent==null){continue;}
            }
            //      If is the first commmit and there isn't a previous release of file, add entire line size like
            //      the file is just added
            else if(k==0 && isFirst){
                ObjectId commitId = repository.resolve(relatedCommitsOfCurrentTaggedRelease.get(0).getCommitFromGit().getId().getName());
                int lineFirstCommit = locMetric(taggedReleaseToGetFileMetrics, commitId, treeWalk.getPathString());

                linesAdded += lineFirstCommit;

                // Check if is the max
                if(lineFirstCommit>linesMaxAdded){linesMaxAdded=lineFirstCommit;}
                continue;
            }
            //      Take previous commit for parent assign
            else{parent = relatedCommitsOfCurrentTaggedRelease.get(k-1).getCommitFromGit();}

            DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
            df.setRepository(repository);
            df.setDiffComparator(RawTextComparator.DEFAULT);
            df.setDetectRenames(true);

            List<DiffEntry> diffs;
            diffs = df.scan(parent.getTree(), commit.getTree());

            int tempAdded = 0;
            int tempDeleted = 0;

            for (DiffEntry diff : diffs) {
                if(diff.getNewPath().equals(treeWalk.getPathString())){
                    for (Edit edit : df.toFileHeader(diff).toEditList()) {
                        tempDeleted += edit.getEndA() - edit.getBeginA();
                        tempAdded += edit.getEndB() - edit.getBeginB();
                    }
                }
            }

            // Set line LOC
            linesDeleted += tempDeleted;
            linesAdded += tempAdded;

            // Check max Added line
            if(tempAdded>linesMaxAdded){linesMaxAdded=tempAdded;}

            int tempChurn = Math.abs(tempAdded-tempDeleted);

            // Check max Churn
            if(tempChurn > churnMax){churnMax=tempChurn;}

            churn += tempChurn;

            
            System.out.println("PER IL COMMIT " + k + ": " + relatedCommitsOfCurrentTaggedRelease.get(k).getCommitFromGit().getShortMessage() + " AGGIUNTE: " + tempAdded + " ELIMINATE: " + tempDeleted);

        }

        locMetrics.set(0, linesAdded);
        locMetrics.set(1, linesMaxAdded);
        locMetrics.set(2, linesAdded+linesDeleted);
        locMetrics.set(3, linesDeleted);
        locMetrics.set(4, churn);
        locMetrics.set(5, churnMax);

        return locMetrics;
    }

    private RevCommit takeLatestCommitOfTag(ReleaseTag previousTaggedReleaseToGetFileMetrics, String filePath){
        // Retrieve the latest commit of passed file of passed tag Release
        RevCommit commitToReturn = null;

        for(RepoFile fileIndex : previousTaggedReleaseToGetFileMetrics.getReferencedFilesList()){
            if(fileIndex.getPathOfFile().equals(filePath) && !fileIndex.getRelatedCommits().isEmpty()){
                commitToReturn = fileIndex.getRelatedCommits().get(fileIndex.getRelatedCommits().size()-1).getCommitFromGit();
            }
        }

        return commitToReturn;
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
