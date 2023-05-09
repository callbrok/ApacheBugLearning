package controller;

import model.*;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class GitController {

    public List<ReleaseTag> retrieveAllGitDataSet(String projectName, String releaseRange, List<ReleaseTag> finalListRelease) throws Exception {

        // From Jira
        ReleaseRetriever rls = new ReleaseRetriever();
        BugRetriever gtb = new BugRetriever();

        List<Release> released = rls.getReleaseFromProject(projectName, true, releaseRange);
        List<Bug> bugList = gtb.getBug(projectName, false, released);

        System.out.println("RELEASED SIZE: " + released.size());


        // From Git
        JGitHelper gtp = new JGitHelper();
        ReleaseTagRetriever gttr = new ReleaseTagRetriever();
        FileRetriever gtf = new FileRetriever();

        Repo repoToDoThinks = gtp.getJGitRepository(projectName);
        List<ReleaseTag> tagRelesesToDoThinks = gttr.makeTagReleasesList(repoToDoThinks, released);

        System.out.println("RELEASE RANGE: " + releaseRange);
        System.out.println("TAGTODOTHINKS SIZE: " + tagRelesesToDoThinks.size());



        // Set commit and metrics to all files
        CommitRetriever cmtr = new CommitRetriever();
        MetricsRetriever mtr = new MetricsRetriever();

        if(releaseRange.equals("ALL")) {
            // Set referenced files for every tagged release
            for(int i = 0; i < tagRelesesToDoThinks.size(); i++) {
                if(i==0){tagRelesesToDoThinks.get(i).setReferencedFilesList(gtf.getAllFilesOfTagRelease(tagRelesesToDoThinks.get(i), tagRelesesToDoThinks.get(i), true, bugList));}
                else{tagRelesesToDoThinks.get(i).setReferencedFilesList(gtf.getAllFilesOfTagRelease(tagRelesesToDoThinks.get(i), tagRelesesToDoThinks.get(i-1), false, bugList));}
            }
            System.out.println("\n\nFATTO INSERIMENTO DI TUTTI I FILE");

            for (int k = 0; k < tagRelesesToDoThinks.size(); k++) {
                for (RepoFile rpFile : tagRelesesToDoThinks.get(k).getReferencedFilesList()) {
                    if (k == 0) {
                        System.out.println("\n\nSTO FACENDO INSERIMENTO COMMIT PER IL FILE: " + rpFile.getPathOfFile() + " DELLA RELEASE CON TAG:" + tagRelesesToDoThinks.get(k).getGitTag());
                        rpFile.setRelatedCommits(cmtr.bugListRefFile(rpFile.getPathOfFile(), tagRelesesToDoThinks.get(k), tagRelesesToDoThinks.get(k), true));

                        System.out.println("\n\nSTO FACENDO INSERIMENTO METRICHE PER IL FILE: " + rpFile.getPathOfFile() + " DELLA RELEASE CON TAG:" + tagRelesesToDoThinks.get(k).getGitTag());
                        rpFile.setFileMetrics(mtr.metricsHelper(tagRelesesToDoThinks.get(k), tagRelesesToDoThinks.get(k), true, rpFile.getPathOfFile(), rpFile.getRelatedCommits()));
                    }
                    else {
                        System.out.println("QUELLA PRIMA" + tagRelesesToDoThinks.get(k-1).getGitTag());
                        System.out.println("\n\nSTO FACENDO INSERIMENTO COMMIT PER IL FILE: " + rpFile.getPathOfFile() + " DELLA RELEASE CON TAG:" + tagRelesesToDoThinks.get(k).getGitTag());
                        rpFile.setRelatedCommits(cmtr.bugListRefFile(rpFile.getPathOfFile(), tagRelesesToDoThinks.get(k), tagRelesesToDoThinks.get(k-1), false));

                        System.out.println("\n\nSTO FACENDO INSERIMENTO METRICHE PER IL FILE: " + rpFile.getPathOfFile() + " DELLA RELEASE CON TAG:" + tagRelesesToDoThinks.get(k).getGitTag());
                        rpFile.setFileMetrics(mtr.metricsHelper(tagRelesesToDoThinks.get(k), tagRelesesToDoThinks.get(k-1), false, rpFile.getPathOfFile(), rpFile.getRelatedCommits()));
                    }
                }
            }
        }


        // Set metrics to all files after the fist time
        if(!releaseRange.equals("ALL")) {
            for(int i = 0; i < tagRelesesToDoThinks.size(); i++) {

                // Set File list
                tagRelesesToDoThinks.get(i).setReferencedFilesList(
                        finalListRelease.get(i).getReferencedFilesList()
                );
                System.out.println("\n\nFATTO INSERIMENTO FILE");
            }

            for(int j = 0; j < tagRelesesToDoThinks.size(); j++){
                for(int y=0; y<tagRelesesToDoThinks.get(j).getReferencedFilesList().size(); y++){

                    // Reset Bugginess
                    tagRelesesToDoThinks.get(j).getReferencedFilesList().get(y).setItsBuggy(false);

                    // Set file Commits list
                    tagRelesesToDoThinks.get(j).getReferencedFilesList().get(y).setRelatedCommits(
                            finalListRelease.get(j).getReferencedFilesList().get(y).getRelatedCommits()
                    );
                    System.out.println("\n\nFATTO INSERIMENTO COMMIT");

                    // Set file Metrics list
                    tagRelesesToDoThinks.get(j).getReferencedFilesList().get(y).setFileMetrics(
                            finalListRelease.get(j).getReferencedFilesList().get(y).getFileMetrics()
                    );
                    System.out.println("\n\nFATTO INSERIMENTO METRICHE " + tagRelesesToDoThinks.get(j).getGitTag());

                }
            }
        }

        // Set Jira bug matches and the reletad bugginess
        List<ReleaseTag> tagRelesesWithBugginess = new ArrayList<>(tagRelesesToDoThinks);

        for(ReleaseTag rlsIndex : tagRelesesToDoThinks){
            for(RepoFile rpfInd : rlsIndex.getReferencedFilesList()){
                for(Commit cmmIndex : rpfInd.getRelatedCommits()){

                    // Check current commit
                    Bug currentBug = cmtr.commitJiraGitLinker(cmmIndex.getCommitFromGit(), bugList);

                    // Check returned bug, if the name is different from 'NOMATCH' there is a match with Jira Bug
                    if(!currentBug.getNameKey().equals("NOMATCH")){
                        cmmIndex.setCommitFromJira(currentBug);

                        // If there is match, set bugginess
                        tagRelesesWithBugginess = gttr.setBugginess(tagRelesesWithBugginess, rpfInd.getNameFile(), cmmIndex.getCommitFromJira().getAffectedVersions(), releaseRange);
                    }
                }
            }
        }


        // Print all dataset
        //printAllGitDataSet(tagRelesesWithBugginess);

        return tagRelesesWithBugginess;
    }

    private void printAllGitDataSet(List<ReleaseTag> tagRelesesWithBugginess){
        // Print alla data inside the ReleaseTag objects list
        int counterYes=0;
        int counterFile=0;

        for(ReleaseTag rlstIndex : tagRelesesWithBugginess){
            System.out.print("\n\n+----------------------------------------------------------------------------------------------------+\n" +
                    "+                             RELEASE REFERED BY TAG: " + rlstIndex.getGitTag()  +
                    "\n+----------------------------------------------------------------------------------------------------+\n\n");

            for(RepoFile rpfIndex : rlstIndex.getReferencedFilesList()){
                counterFile = counterFile + 1;

                System.out.print("\n\n+ FILE: " + rpfIndex.getPathOfFile());
                System.out.print("\n+ NELLA RELEASE CON TAG: " + rlstIndex.getGitTag());
                System.out.print("\n+ BUGGINESS: " + rpfIndex.getItsBuggy()); if(rpfIndex.getItsBuggy()){counterYes = counterYes + 1;}
                System.out.print("\n+ POSSIEDE I SEGUENTI [" + rpfIndex.getRelatedCommits().size() + "] COMMIT:");

                for(Commit comIndex : rpfIndex.getRelatedCommits()){
                    if(rpfIndex.getRelatedCommits().isEmpty()){System.out.print("  NESSUN COMMIT ASSEGNATO"); continue;}
                    if(comIndex.getCommitFromJira() != null){System.out.print("\n   JIRA-| " + comIndex.getCommitFromJira().getNameKey());continue;}
                    System.out.print("\n    GIT-| " + comIndex.getCommitFromGit().getShortMessage());
                }

                System.out.print("\n+ METRICHE:");
                System.out.print("\n+    LOC              -| " + rpfIndex.getFileMetrics().getLoc());
                System.out.print("\n+    LOC_ADDED        -| " + rpfIndex.getFileMetrics().getLocAdded());
                System.out.print("\n+    LOC_MAX_ADDED    -| " + rpfIndex.getFileMetrics().getLocMaxAdded());
                System.out.print("\n+    LOC_TOUCHED      -| " + rpfIndex.getFileMetrics().getLocTouched());
                System.out.print("\n+    N_REVISION       -| " + rpfIndex.getFileMetrics().getnRevision());
                System.out.print("\n+    AVG_LOC_ADDED    -| " + rpfIndex.getFileMetrics().getAverageLocAdded());
                System.out.print("\n+    N_AUTHORS        -| " + rpfIndex.getFileMetrics().getnAuth());
                System.out.print("\n+    CHURN            -| " + rpfIndex.getFileMetrics().getChurn());
                System.out.print("\n+    MAX_CHURN        -| " + rpfIndex.getFileMetrics().getMaxChurn());
                System.out.print("\n+    AVG_CHURN        -| " + rpfIndex.getFileMetrics().getAverageChurn());
            }
        }

        System.out.println("\n\nI FILE BUGGY SONO: " + counterYes + " SU " + counterFile + " CLASSI TOTALI TRA LE VARIE RELEASE");
    }

}
