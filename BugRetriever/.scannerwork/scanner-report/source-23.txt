package controller;

import model.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GitController {
    private static final Logger LOGGER = Logger.getLogger( GitController.class.getName() );


    public List<ReleaseTag> retrieveAllGitDataSet(List<Release> released, List<ReleaseTag> finalListRelease, Repo repoToDoThinks, Boolean doOnALLRelease) throws Exception {

        // From Jira
        BugRetriever gtb = new BugRetriever();

        List<Bug> bugList = gtb.getBug(repoToDoThinks.getApacheProjectName(), false, released);
        // ------------------------------------

        // From Git
        ReleaseTagRetriever gttr = new ReleaseTagRetriever();

        List<ReleaseTag> tagRelesesToDoThinks = gttr.makeTagReleasesList(repoToDoThinks, released);
        // ------------------------------------


        // Set commit and metrics to all files
        CommitRetriever cmtr = new CommitRetriever();

        if(Boolean.TRUE.equals(doOnALLRelease)) {
           tagRelesesToDoThinks = doOnALLRelease(tagRelesesToDoThinks, repoToDoThinks, released);
        }


        // Set metrics to all files after the fist time
        if(Boolean.FALSE.equals(doOnALLRelease)) {
            tagRelesesToDoThinks = notDoOnALLRelease(tagRelesesToDoThinks, finalListRelease, released);
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
                        tagRelesesWithBugginess = gttr.setBugginess(tagRelesesWithBugginess, rpfInd.getNameFile(), cmmIndex.getCommitFromJira().getAffectedVersions(), released, doOnALLRelease);
                    }
                }
            }
        }

        LOGGER.log(Level.INFO, ("\n+ Settate tutte le bugginess\n"));



        // Print all dataset
        //printAllGitDataSet(tagRelesesWithBugginess);

        return tagRelesesWithBugginess;
    }

    private List<ReleaseTag> doOnALLRelease(List<ReleaseTag> tagRelesesToDoThinks, Repo repoToDoThinks, List<Release> released) throws Exception {
        // From Git
        FileRetriever gtf = new FileRetriever();

        // Set commit and metrics to all files
        CommitRetriever cmtr = new CommitRetriever();
        MetricsRetriever mtr = new MetricsRetriever();

        // Set referenced files for every tagged release
        for(ReleaseTag rlIndex: tagRelesesToDoThinks){rlIndex.setReferencedFilesList(gtf.getAllFilesOfTagRelease(rlIndex));}
        LOGGER.log(Level.INFO, ("+ Settati tutti i file\n"));

        // Retrieve commits map
        Map<String, List<RevCommit>> commitsMap = cmtr.mapCommitsByRelease(repoToDoThinks, released);
        // Set commit and return all list updated
        tagRelesesToDoThinks = cmtr.listCommitRetriever(tagRelesesToDoThinks, commitsMap, repoToDoThinks);
        LOGGER.log(Level.INFO, ("+ Settati tutti i commit per tutti i file\n"));

        // Set Metrics
        for (int k = 0; k < tagRelesesToDoThinks.size(); k++) {
            for (RepoFile rpFile : tagRelesesToDoThinks.get(k).getReferencedFilesList()) {
                if (k == 0) {
                    LOGGER.log(Level.INFO, ("\n+ Inserimento metriche per il file: " + rpFile.getPathOfFile() + "\n     release_tag: " + tagRelesesToDoThinks.get(k).getGitTag()));
                    rpFile.setFileMetrics(mtr.metricsHelper(tagRelesesToDoThinks.get(k), tagRelesesToDoThinks.get(k), true, rpFile.getPathOfFile(), rpFile.getRelatedCommits()));
                }
                else {
                    LOGGER.log(Level.INFO, ("\n+ Inserimento metriche per il file: " + rpFile.getPathOfFile() + "\n     release_tag: " + tagRelesesToDoThinks.get(k-1).getGitTag()));
                    rpFile.setFileMetrics(mtr.metricsHelper(tagRelesesToDoThinks.get(k), tagRelesesToDoThinks.get(k-1), false, rpFile.getPathOfFile(), rpFile.getRelatedCommits()));
                }
            }
        }

        LOGGER.log(Level.INFO, ("\n\n+ Settate tutte le metriche\n"));
        return tagRelesesToDoThinks;
    }

    private List<ReleaseTag> notDoOnALLRelease(List<ReleaseTag> tagRelesesToDoThinks, List<ReleaseTag> finalListRelease, List<Release> released){

        LOGGER.log(Level.INFO, () -> "\n\n+ Sto settando le release per Walk Forward - STEP: " + (released.size()-1));

        for(int i = 0; i < tagRelesesToDoThinks.size(); i++) {

            // Set File list
            tagRelesesToDoThinks.get(i).setReferencedFilesList(
                    finalListRelease.get(i).getReferencedFilesList()
            );
        }

        for(int j = 0; j < tagRelesesToDoThinks.size(); j++){
            for(int y=0; y<tagRelesesToDoThinks.get(j).getReferencedFilesList().size(); y++){

                // Reset Bugginess
                tagRelesesToDoThinks.get(j).getReferencedFilesList().get(y).setItsBuggy(false);

                // Set file Commits list
                tagRelesesToDoThinks.get(j).getReferencedFilesList().get(y).setRelatedCommits(
                        finalListRelease.get(j).getReferencedFilesList().get(y).getRelatedCommits()
                );

                // Set file Metrics list
                tagRelesesToDoThinks.get(j).getReferencedFilesList().get(y).setFileMetrics(
                        finalListRelease.get(j).getReferencedFilesList().get(y).getFileMetrics()
                );
            }
        }

        LOGGER.log(Level.INFO, () -> "+ Tutte le release impostate per il Walk Forward - STEP: " + (released.size()-1));
        return tagRelesesToDoThinks;
    }


    private void printAllGitDataSet(List<ReleaseTag> tagRelesesWithBugginess){
        // Print alla data inside the ReleaseTag objects list
        int counterYes=0;
        int counterFile=0;

        for(ReleaseTag rlstIndex : tagRelesesWithBugginess){
            LOGGER.log(Level.INFO, ("\n\n+----------------------------------------------------------------------------------------------------+\n" +
                    "+                             RELEASE REFERED BY TAG: " + rlstIndex.getGitTag()  +
                    "\n+----------------------------------------------------------------------------------------------------+\n\n"));

            for(RepoFile rpfIndex : rlstIndex.getReferencedFilesList()){
                counterFile = counterFile + 1;

                LOGGER.log(Level.INFO, ("\n\n+ FILE: " + rpfIndex.getPathOfFile()));
                LOGGER.log(Level.INFO, ("\n+ NELLA RELEASE CON TAG: " + rlstIndex.getGitTag()));
                LOGGER.log(Level.INFO, ("\n+ BUGGINESS: " + rpfIndex.getItsBuggy())); if(Boolean.TRUE.equals(rpfIndex.getItsBuggy())){counterYes = counterYes + 1;}
                LOGGER.log(Level.INFO, () -> "\n+ POSSIEDE I SEGUENTI [" + rpfIndex.getRelatedCommits().size() + "] COMMIT:");

                for(Commit comIndex : rpfIndex.getRelatedCommits()){
                    if(rpfIndex.getRelatedCommits().isEmpty()){ LOGGER.log(Level.INFO, ("  NESSUN COMMIT ASSEGNATO")); continue;}
                    if(comIndex.getCommitFromJira() != null){ LOGGER.log(Level.INFO, ("\n   JIRA-| " + comIndex.getCommitFromJira().getNameKey()));continue;}
                    LOGGER.log(Level.INFO, ("\n    GIT-| " + comIndex.getCommitFromGit().getShortMessage()));
                }

                LOGGER.log(Level.INFO, ("\n+ METRICHE:"));
                LOGGER.log(Level.INFO, ("\n+    LOC              -| " + rpfIndex.getFileMetrics().getLoc()));
                LOGGER.log(Level.INFO, ("\n+    LOC_ADDED        -| " + rpfIndex.getFileMetrics().getLocAdded()));
                LOGGER.log(Level.INFO, ("\n+    LOC_MAX_ADDED    -| " + rpfIndex.getFileMetrics().getLocMaxAdded()));
                LOGGER.log(Level.INFO, ("\n+    LOC_TOUCHED      -| " + rpfIndex.getFileMetrics().getLocTouched()));
                LOGGER.log(Level.INFO, ("\n+    N_REVISION       -| " + rpfIndex.getFileMetrics().getnRevision()));
                LOGGER.log(Level.INFO, ("\n+    AVG_LOC_ADDED    -| " + rpfIndex.getFileMetrics().getAverageLocAdded()));
                LOGGER.log(Level.INFO, ("\n+    N_AUTHORS        -| " + rpfIndex.getFileMetrics().getnAuth()));
                LOGGER.log(Level.INFO, ("\n+    CHURN            -| " + rpfIndex.getFileMetrics().getChurn()));
                LOGGER.log(Level.INFO, ("\n+    MAX_CHURN        -| " + rpfIndex.getFileMetrics().getMaxChurn()));
                LOGGER.log(Level.INFO, ("\n+    AVG_CHURN        -| " + rpfIndex.getFileMetrics().getAverageChurn()));
            }
        }

        LOGGER.log(Level.INFO, ("\n\nI FILE BUGGY SONO: " + counterYes + " SU " + counterFile + " CLASSI TOTALI TRA LE VARIE RELEASE"));
    }

}
