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
import java.util.regex.Pattern;

public class MetricsRetriever {

    public Metrics metricsHelper(ReleaseTag taggedReleaseToGetFileMetrics, ReleaseTag previousTaggedReleaseToGetFileMetrics, Boolean isFirst, String treeWalk, List<Commit> relatedCommitsOfCurrentTaggedRelease) throws Exception {

        // Retrieve LOC Metric -------------------------
        List<Integer> locMetricAndNMethods = locAndMethodsNumberMetrics(taggedReleaseToGetFileMetrics, taggedReleaseToGetFileMetrics.getCurrentRepo().getjGitRepository().exactRef(taggedReleaseToGetFileMetrics.getGitTag()).getObjectId(), treeWalk, false);
        // Integer 'locMetricAndNMethods' list where:
        //      1' Element: LOC (Whit comment and blank lines)
        //      2' Element: NPBM (Number of public methods)
        //      3' Element: NPVM (Number of private methods)
        //      4' Element: NSM (Number of static methods)
        //      5' Element: NAM (Number of all method's class)
        //      6' Element: LOCM (Number of commented lines)
        int locMetric = locMetricAndNMethods.get(0);
        int nPublicMethods = locMetricAndNMethods.get(1);
        int nPrivateMethods = locMetricAndNMethods.get(2);
        int nStaticMethods = locMetricAndNMethods.get(3);
        int nMethods = locMetricAndNMethods.get(4);
        int nCommentedLines = locMetricAndNMethods.get(5);


        // Retrieve others LOC Metrics -----------------
        List<Integer> locMetrics = locOtherMetrics(taggedReleaseToGetFileMetrics, previousTaggedReleaseToGetFileMetrics, treeWalk, isFirst, relatedCommitsOfCurrentTaggedRelease);
        // Integer 'locMetrics' list where:
        //      1' Element: LOC Added
        //      2' Element: Max LOC Added
        //      3' Element: LOC Touched
        //      4' Element: LOC Deleted
        //      5' Element: Churn
        //      6' Element: Max Churn
        int locAdded = locMetrics.get(0);
        int locMaxAdded = locMetrics.get(1);
        int locTouchedMetric = locMetrics.get(2);

        // Retrieve Number of Revision -----------------
        int numberRevision = relatedCommitsOfCurrentTaggedRelease.size();

        // Retrieve Average LOC Added ------------------
        float averageLocAdded = 0;
        if(numberRevision>0){averageLocAdded=(float)locAdded / numberRevision;}

        // Retrieve Nauth Metric -----------------------
        int nAuthMetric = numberAuthorsMetric(relatedCommitsOfCurrentTaggedRelease);

        // Retrieve Churn Metric -----------------------
        int churn = locMetrics.get(4);

        // Retrieve Max Churn Metric -------------------
        int maxChurn = locMetrics.get(5);

        // Average Churn Metric ------------------------
        float averageChurn = 0;
        if(numberRevision>0){averageChurn=(float)churn / numberRevision;}


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
                averageChurn,
                nPublicMethods,
                nPrivateMethods,
                nStaticMethods,
                nMethods,
                nCommentedLines
        );
    }


    private List<Integer> locAndMethodsNumberMetrics(ReleaseTag taggedReleaseToGetFileMetrics, ObjectId objectId, String pathOfFile, Boolean skipRegex) throws IOException {
        // Size (LOC):
        //      Lines of code of current file on the current release.
        Repository repository = taggedReleaseToGetFileMetrics.getCurrentRepo().getjGitRepository();
        List<Integer> locMetricsAndNMethods = new ArrayList<>(Arrays.asList(0,0,0,0,0,0));

        int nline = 0;
        int nPublicMethods = 0;
        int nPrivateMethods = 0;
        int nStaticMethods = 0;
        int nMethods = 0;
        int blankLine = 0;
        int nCommentedLines = 0;

        // Regex Pattern for methods declaration
        Pattern patternPublicMethod = Pattern.compile("(?:(?:public)+\\s+)+[$_\\w<>\\[\\]\\s]*\\s+[\\$_\\w]+\\([^\\)]*\\)?\\s*\\{?[^\\}]*\\}?");
        Pattern patternPrivateMethod = Pattern.compile("(?:(?:private)+\\s+)+[$_\\w<>\\[\\]\\s]*\\s+[\\$_\\w]+\\([^\\)]*\\)?\\s*\\{?[^\\}]*\\}?");
        Pattern patternStaticMethod = Pattern.compile("(?:(?:static)+\\s+)+[$_\\w<>\\[\\]\\s]*\\s+[\\$_\\w]+\\([^\\)]*\\)?\\s*\\{?[^\\}]*\\}?");
        Pattern patternAllMethods = Pattern.compile("(?:(?:public|private|protected|static|final|native|synchronized|abstract|transient)+\\s+)+[$_\\w<>\\[\\]\\s]*\\s+[\\$_\\w]+\\([^\\)]*\\)?\\s*\\{?[^\\}]*\\}?");

        // Regex Pattern for comments
        //      Search on the line for the sequence of characters: */
        Pattern patternComment1 = Pattern.compile("\\*/");
        //      Search on the line for the sequence of characters: /*
        Pattern patternComment2 = Pattern.compile("\\/\\*");
        //      Search on the line for the sequence of characters: //
        Pattern patternComment3 = Pattern.compile("//");
        //      Search on the line for the character * but not followed by character =, so to
        //      distinguish comments from mathematical expressions
        Pattern patternComment4 = Pattern.compile("\\*[^=]");


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

                    // LOC metric counter
                    nline = nline + 1;

                    if(skipRegex){continue;}

                    // Methods counter
                    if(patternPublicMethod.matcher(line).find()){nPublicMethods+=1;}
                    if(patternPrivateMethod.matcher(line).find()){nPrivateMethods+=1;}
                    if(patternStaticMethod.matcher(line).find()){nStaticMethods+=1;}
                    if(patternAllMethods.matcher(line).find()){nMethods+=1;}

                    // Comments counter
                    if(patternComment1.matcher(line).find() || patternComment2.matcher(line).find() || patternComment3.matcher(line).find() || patternComment4.matcher(line).find()){nCommentedLines+=1;}

                    // Blank-line counter
                    if(line.trim().isEmpty()){blankLine += 1;}
                }

                break;
            }
        }

        locMetricsAndNMethods.set(0, nline);
        locMetricsAndNMethods.set(1, nPublicMethods);
        locMetricsAndNMethods.set(2, nPrivateMethods);
        locMetricsAndNMethods.set(3, nStaticMethods);
        locMetricsAndNMethods.set(4, nMethods);
        locMetricsAndNMethods.set(5, nCommentedLines);

        return locMetricsAndNMethods;
    }


    private List<Integer> locOtherMetrics(ReleaseTag taggedReleaseToGetFileMetrics, ReleaseTag previousTaggedReleaseToGetFileMetrics, String treeWalk, Boolean isFirst, List<Commit> relatedCommitsOfCurrentTaggedRelease) throws Exception {
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
                parent = takeLatestCommitOfTag(previousTaggedReleaseToGetFileMetrics, treeWalk);

                // If parent return null, the previous release doesn't have a commit
                if(parent==null){continue;}
            }
            //      If is the first commmit and there isn't a previous release of file, add entire line size like
            //      the file is just added
            else if(k==0 && isFirst){
                ObjectId commitId = repository.resolve(relatedCommitsOfCurrentTaggedRelease.get(0).getCommitFromGit().getId().getName());
                int lineFirstCommit = locAndMethodsNumberMetrics(taggedReleaseToGetFileMetrics, commitId, treeWalk, true).get(0);

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

            List<DiffEntry> diffs = new ArrayList<>();
            diffs = df.scan(parent.getTree(), commit.getTree());

            int tempAdded = 0;
            int tempDeleted = 0;

            for (DiffEntry diff : diffs) {
                if(diff.getNewPath().equals(treeWalk)){
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


            //System.out.println("PER IL COMMIT " + k + ": " + relatedCommitsOfCurrentTaggedRelease.get(k).getCommitFromGit().getShortMessage() + " AGGIUNTE: " + tempAdded + " ELIMINATE: " + tempDeleted);

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