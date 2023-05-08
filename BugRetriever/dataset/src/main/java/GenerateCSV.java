import controller.*;
import model.Bug;
import model.Release;
import model.ReleaseTag;

import java.util.List;

public class GenerateCSV {

    private static final String PROJECT="BOOKKEEPER";

    public static void main(String[] args) throws Exception {

        // From Git
        GitController gtc = new GitController();
        List<ReleaseTag> finalListRelease = gtc.retrieveAllGitDataSet(PROJECT, "ALL");

        // From DataSet
        CSVGenerator csv = new CSVGenerator();
        ArffGenerator arff = new ArffGenerator();

        // Apply Walk Forward
        for(int k=0; k<finalListRelease.size()-1; k++){

            // Build Training Set Dataset, with release in range 0-k
            arff.buildArff(csv.buildCSV(gtc.retrieveAllGitDataSet(PROJECT, String.valueOf(k)), finalListRelease.get(k).getReleaseFromJira().getName(), String.valueOf(k) + "_TRAINING"));

            // Build Testing Set Dataset with k+1 Release
            arff.buildArff(csv.buildCSV(List.of(finalListRelease.get(k+1)), finalListRelease.get(k+1).getReleaseFromJira().getName(), String.valueOf(k) + "_TESTING"));
        }



    }
}
