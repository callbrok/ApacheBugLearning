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

    enum methodType {PUBLIC, PRIVATE, STATIC, ALL}

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

        Metrics metricsToReturn = new Metrics();

        metricsToReturn.setLoc(locMetric);
        metricsToReturn.setLocTouched(locTouchedMetric);
        metricsToReturn.setnAuth(nAuthMetric);
        metricsToReturn.setLocAdded(locAdded);
        metricsToReturn.setLocMaxAdded(locMaxAdded);
        metricsToReturn.setnRevision(numberRevision);
        metricsToReturn.setAverageLocAdded(averageLocAdded);
        metricsToReturn.setChurn(churn);
        metricsToReturn.setMaxChurn(maxChurn);
        metricsToReturn.setAverageChurn(averageChurn);

        metricsToReturn.setnPublicMethods(nPublicMethods);
        metricsToReturn.setnPrivateMethods(nPrivateMethods);
        metricsToReturn.setnStaticMethods(nStaticMethods);
        metricsToReturn.setnMethods(nMethods);
        metricsToReturn.setnCommentedLines(nCommentedLines);

        return metricsToReturn;
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

                    if(Boolean.TRUE.equals(skipRegex)){continue;}

                    // Methods counter
                    nPublicMethods = publicMethodCounter(line, nPublicMethods);
                    nPrivateMethods = privateMethodCounter(line, nPrivateMethods);
                    nStaticMethods = staticMethodsCounter(line, nStaticMethods);
                    nMethods = allMethodsCounter(line, nMethods);

                    // Comments counter
                    nCommentedLines = commentsCounter(line, nCommentedLines);

                    // Blank-line counter
                    blankLine = blankLineCounter(line, blankLine);
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

    private int publicMethodCounter(String line, int nPublicMethods){
        // Regex Pattern for methods declaration
        Pattern patternPublicMethod = Pattern.compile(regexSelector(methodType.PUBLIC));

        // Methods counter
        if(patternPublicMethod.matcher(line).find()){nPublicMethods+=1;}
        return nPublicMethods;
    }

    private int privateMethodCounter(String line, int nPrivateMethods){
        // Regex Pattern for methods declaration
        Pattern patternPrivateMethod = Pattern.compile(regexSelector(methodType.PRIVATE));

        // Methods counter
        if(patternPrivateMethod.matcher(line).find()){nPrivateMethods+=1;}
        return nPrivateMethods;
    }

    private int staticMethodsCounter(String line, int nStaticMethods){
        // Regex Pattern for methods declaration
        Pattern patternStaticMethod = Pattern.compile(regexSelector(methodType.STATIC));

        // Methods counter
        if(patternStaticMethod.matcher(line).find()){nStaticMethods+=1;}
        return nStaticMethods;
    }

    private int allMethodsCounter(String line, int nMethods){
        // Regex Pattern for methods declaration
        Pattern patternAllMethods = Pattern.compile(regexSelector(methodType.ALL));

        // Methods counter
        if(patternAllMethods.matcher(line).find()){nMethods+=1;}
        return nMethods;
    }

    private String regexSelector(methodType mtp){
        String wordToAdd;

        switch (mtp){
            case PUBLIC:
                wordToAdd="public";
                break;
            case PRIVATE:
                wordToAdd="private";
                break;
            case STATIC:
                wordToAdd="static";
                break;
            default:
                wordToAdd = "public|private|protected|static|final|native|synchronized|abstract|transient";
        }

        return "(?:(?:" + wordToAdd +")+\\s+)+[$_\\w<>\\[\\]\\s]*\\s+[\\$_\\w]+\\([^\\)]*\\)?\\s*\\{?[^\\}]*\\}?";
    }

    private int commentsCounter(String line, int nCommentedLines){
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

        // Comments counter
        if(patternComment1.matcher(line).find() || patternComment2.matcher(line).find() || patternComment3.matcher(line).find() || patternComment4.matcher(line).find()){nCommentedLines+=1;}

        return nCommentedLines;
    }

    private int blankLineCounter(String line, int blankLine){
        if(line.trim().isEmpty()){blankLine += 1;}

        return blankLine;
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
            RevCommit parent = null;

            // #NOTE-TO-THINKING-OF:
            //      If is the first commmit and there is a previous release of file, take parent for the first commit
            //      the latest commit of previous release file
            if(k==0 && Boolean.FALSE.equals(isFirst)){
                parent = takeLatestCommitOfTag(previousTaggedReleaseToGetFileMetrics, treeWalk);

            }
            //      If is the first commmit and there isn't a previous release of file, add entire line size like
            //      the file is just added
            else if(k==0 && Boolean.TRUE.equals(isFirst)){
                ObjectId commitId = repository.resolve(relatedCommitsOfCurrentTaggedRelease.get(0).getCommitFromGit().getId().getName());
                int lineFirstCommit = locAndMethodsNumberMetrics(taggedReleaseToGetFileMetrics, commitId, treeWalk, true).get(0);

                linesAdded += lineFirstCommit;

                // Check if is the max
                if(lineFirstCommit>linesMaxAdded){linesMaxAdded=lineFirstCommit;}
            }
            //      Take previous commit for parent assign
            else{parent = relatedCommitsOfCurrentTaggedRelease.get(k-1).getCommitFromGit();}

            // Condition to continue to next iteration for loop
            // If parent return null, the previous release doesn't have a commit and if k==0 && isFirst always at the end
            if((k==0 && !isFirst && parent==null) || (k==0 && isFirst)){continue;}

            DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
            df.setRepository(repository);
            df.setDiffComparator(RawTextComparator.DEFAULT);
            df.setDetectRenames(true);

            List<DiffEntry> diffs = df.scan(parent.getTree(), commit.getTree());

            List<Integer> tempValueList = tempDifferencesFileCounter(diffs, df, treeWalk);
            int tempAdded = tempValueList.get(0);
            int tempDeleted = tempValueList.get(1);

            // Set line LOC
            linesDeleted += tempDeleted;
            linesAdded += tempAdded;

            // Check max Added line
            linesMaxAdded = checkLinesMaxAdded(tempAdded, linesMaxAdded);

            int tempChurn = Math.abs(tempAdded-tempDeleted);

            // Check max Churn
            churnMax = checkChurnMax(tempChurn, churnMax);

            churn += tempChurn;

        }

        locMetrics.set(0, linesAdded);
        locMetrics.set(1, linesMaxAdded);
        locMetrics.set(2, linesAdded+linesDeleted);
        locMetrics.set(3, linesDeleted);
        locMetrics.set(4, churn);
        locMetrics.set(5, churnMax);

        return locMetrics;
    }


    private List<Integer> tempDifferencesFileCounter(List<DiffEntry> diffs, DiffFormatter df, String treeWalk) throws IOException {
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

        return new ArrayList<>(Arrays.asList(tempAdded, tempDeleted));
    }


    private int checkLinesMaxAdded(int tempAdded, int linesMaxAdded){
        if(tempAdded>linesMaxAdded){linesMaxAdded=tempAdded;}
        return linesMaxAdded;
    }

    private int checkChurnMax(int tempChurn, int churnMax){
        if(tempChurn > churnMax){churnMax=tempChurn;}
        return churnMax;
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