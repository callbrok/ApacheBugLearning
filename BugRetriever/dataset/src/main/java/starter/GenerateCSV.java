package starter;

import controller.*;
import model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GenerateCSV {
    private static final Logger LOGGER = Logger.getLogger( GenerateCSV.class.getName() );

    private static final String PROJECT="BOOKKEEPER";

    public static void main(String[] args) throws Exception {

        long startTime = System.currentTimeMillis();

        // From JIRA
        ReleaseRetriever rrt = new ReleaseRetriever();

        // From Git
        GitController gtc = new GitController();
        JGitHelper gtp = new JGitHelper();

        Repo repoToDoThinks = gtp.getJGitRepository(PROJECT);
        List<Release> released = rrt.getReleaseFromProject(PROJECT, true, "ALL");
        List<ReleaseTag> finalListRelease = gtc.retrieveAllGitDataSet(released, null, repoToDoThinks, true);

        // From DataSet
        CSVGenerator csv = new CSVGenerator();
        ArffGenerator arff = new ArffGenerator();
        WEKAHelper whp = new WEKAHelper();

        csv.buildCSVFromGit(finalListRelease, "ALL",  PROJECT +"_ALL");

        // From WEKA
        List<EvaluationWEKA> wekaRandomForestList = new ArrayList<>();
        List<EvaluationWEKA> wekaNativeBayesList = new ArrayList<>();
        List<EvaluationWEKA> wekaIBKList = new ArrayList<>();

        // String parameters combination
        String[] featureSelectionCombination = {WEKAHelper.NO_FEATURE_SELECTION, WEKAHelper.BEST_FIRST};
        String[] balancingCombination = {WEKAHelper.NO_BALANCING, WEKAHelper.OVERSAMPLING, WEKAHelper.UNDERSAMPLING, WEKAHelper.SMOTE};
        String[] costSensitiveCombination = {WEKAHelper.NO_COST_SENSITIVE, WEKAHelper.SENSITIVE_LEARNING, WEKAHelper.SENSITIVE_THRESHOLD};

        // Apply Walk Forward
        for(int k=0; k<finalListRelease.size()-1; k++){

            // Build Training Set Dataset, with release in range 0-k
            released = rrt.getReleaseFromProject(PROJECT, true, String.valueOf(k));
            String trainingArffPath = arff.buildArff(csv.buildCSVFromGit(gtc.retrieveAllGitDataSet(released, finalListRelease, repoToDoThinks, false), finalListRelease.get(k).getReleaseFromJira().getName(), k + "_" + PROJECT +"_TRAINING"));
            // Build Testing Set Dataset with k+1 Release
            String testingArffPath =  arff.buildArff(csv.buildCSVFromGit(List.of(finalListRelease.get(k+1)), finalListRelease.get(k+1).getReleaseFromJira().getName(), k + "_" + PROJECT + "_TESTING"));


            // -- WEKA API ------------------------------------------------------------- INIZIO --
            List<EvaluationWEKA> tempWeka;

            for(String featureSelection : featureSelectionCombination){
                for(String balancing : balancingCombination){
                    for(String costSensitive : costSensitiveCombination){
                        LOGGER.log(Level.INFO, () -> ("ANALIZZO CONFIGURAZIONE: " + featureSelection + " | " + balancing + " | " + costSensitive));

                        tempWeka = whp.evaluationWEKA(PROJECT, released, finalListRelease.get(k+1).getReleaseFromJira(), trainingArffPath, testingArffPath,
                                featureSelection, balancing, costSensitive);

                        wekaRandomForestList.add(tempWeka.get(0));
                        wekaNativeBayesList.add(tempWeka.get(1));
                        wekaIBKList.add(tempWeka.get(2));

                    }
                }
            }
            // -- WEKA API --------------------------------------------------------------- FINE --

        }

        csv.buildCSVFromWEKA(wekaRandomForestList);
        csv.buildCSVFromWEKA(wekaNativeBayesList);
        csv.buildCSVFromWEKA(wekaIBKList);


        long endTime = System.currentTimeMillis();
        LOGGER.log(Level.INFO, () -> ("\n\nThat took " + (endTime - startTime) + " milliseconds"));
    }

}
