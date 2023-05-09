import controller.*;
import model.Bug;
import model.Release;
import model.ReleaseTag;

import java.util.List;

public class GenerateCSV {

    private static final String PROJECT="SYNCOPE";

    public static void main(String[] args) throws Exception {

        long startTime = System.currentTimeMillis();

        // From Git
        GitController gtc = new GitController();
        List<ReleaseTag> finalListRelease = gtc.retrieveAllGitDataSet(PROJECT, "ALL", null);

        System.out.println("SIZE DEL FINAL: " + finalListRelease.size());

        // From DataSet
        CSVGenerator csv = new CSVGenerator();
        ArffGenerator arff = new ArffGenerator();

        csv.buildCSV(finalListRelease, "ALL",  PROJECT +"_ALL");

        // Apply Walk Forward
        for(int k=0; k<finalListRelease.size()-1; k++){
            // Build Training Set Dataset, with release in range 0-k
            arff.buildArff(csv.buildCSV(gtc.retrieveAllGitDataSet(PROJECT, String.valueOf(k), finalListRelease), finalListRelease.get(k).getReleaseFromJira().getName(), k + "_" + PROJECT +"_TRAINING"));

            // Build Testing Set Dataset with k+1 Release
            arff.buildArff(csv.buildCSV(List.of(finalListRelease.get(k+1)), finalListRelease.get(k+1).getReleaseFromJira().getName(), k + "_" + PROJECT + "_TESTING"));
        }



        long endTime = System.currentTimeMillis();

        System.out.println("\n\nThat took " + (endTime - startTime) + " milliseconds");
    }
}
