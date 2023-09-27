package controller;

import model.EvaluationWEKA;
import model.Release;
import model.ReleaseTag;
import model.RepoFile;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CSVGenerator {
    public String buildCSVFromGit(List<ReleaseTag> tagRelesesWithBugginess, String fileVersionName, String datasetType) throws IOException {
        String fileName = "WalkForward_" + datasetType + "-SET_R[" + fileVersionName + "].csv";

        String[] headers = {"Version", "Filename", "NR", "NAUTHORS" ,"LOC", "LOC_ADDED", "AVGLOCADDED"
                , "MAXLOCADDED", "LOCTOUCHED", "CHURN", "AVGCHURN", "MAXCHURN"
                , "NPBM", "NPVM", "NSM", "NAM", "LOCM"
                , "BUGGY"};

        List<List<String>> data = new ArrayList<>();

        for(ReleaseTag rlstIndex : tagRelesesWithBugginess){

            for(RepoFile rpfIndex : rlstIndex.getReferencedFilesList()){
                List<String> lineToAdd = new ArrayList<>();

                lineToAdd.add(rlstIndex.getReleaseFromJira().getName());
                lineToAdd.add(rpfIndex.getPathOfFile());
                lineToAdd.add(String.valueOf(rpfIndex.getFileMetrics().getnRevision()));
                lineToAdd.add(String.valueOf(rpfIndex.getFileMetrics().getnAuth()));
                lineToAdd.add(String.valueOf(rpfIndex.getFileMetrics().getLoc()));
                lineToAdd.add(String.valueOf(rpfIndex.getFileMetrics().getLocAdded()));
                lineToAdd.add(String.valueOf(rpfIndex.getFileMetrics().getAverageLocAdded()));
                lineToAdd.add(String.valueOf(rpfIndex.getFileMetrics().getLocMaxAdded()));
                lineToAdd.add(String.valueOf(rpfIndex.getFileMetrics().getLocTouched()));
                lineToAdd.add(String.valueOf(rpfIndex.getFileMetrics().getChurn()));
                lineToAdd.add(String.valueOf(rpfIndex.getFileMetrics().getAverageChurn()));
                lineToAdd.add(String.valueOf(rpfIndex.getFileMetrics().getMaxChurn()));

                lineToAdd.add(String.valueOf(rpfIndex.getFileMetrics().getnPublicMethods()));
                lineToAdd.add(String.valueOf(rpfIndex.getFileMetrics().getnPrivateMethods()));
                lineToAdd.add(String.valueOf(rpfIndex.getFileMetrics().getnStaticMethods()));
                lineToAdd.add(String.valueOf(rpfIndex.getFileMetrics().getnMethods()));
                lineToAdd.add(String.valueOf(rpfIndex.getFileMetrics().getnCommentedLines()));

                lineToAdd.add(rpfIndex.getItsBuggyCSV());

                data.add(lineToAdd);
            }
        }

        FileWriter out = new FileWriter(fileName);
        CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(headers));

        File f = new File(fileName);

        for (List<String> row : data) {
            printer.printRecord(row);
        }

        printer.close();
        out.close();

        return f.getAbsolutePath();
    }


    public void buildCSVFromWEKA(List<EvaluationWEKA> wekaClassifierOutput) throws IOException {
        String fileName = "WEKA-API_OUTPUT_" + wekaClassifierOutput.get(0).getProjectName() + "-" + wekaClassifierOutput.get(0).getClassifier() + ".csv";

        String[] headers = {"PROJECT", "TRAINING-RELEASES", "TEST-RELEASE", "CLASSIFIER" ,"FEATURE-SELECTION", "BALANCING", "COST-SENSITIVE",
                "%-TRAINING", "PRECISION", "RECALL", "AUC", "KAPPA", "ACCURACY", "TRUE-NEGATIVE", "TRUE-POSITIVE", "FALSE-POSITIVE", "FALSE-NEGATIVE"};

        List<List<String>> data = new ArrayList<>();

        for(EvaluationWEKA evalWekaIndex : wekaClassifierOutput){

            List<String> lineToAdd = new ArrayList<>();

            lineToAdd.add(evalWekaIndex.getProjectName());

            StringBuilder trainingReleases= new StringBuilder();
            for(Release rlsIndex : evalWekaIndex.getTrainingReleases()){trainingReleases.append(" ").append(rlsIndex.getName());}
            lineToAdd.add(trainingReleases.toString());

            lineToAdd.add(evalWekaIndex.getTestingRelease().getName());
            lineToAdd.add(evalWekaIndex.getClassifier());
            lineToAdd.add(evalWekaIndex.getFeatureSelection());
            lineToAdd.add(evalWekaIndex.getBalancing());
            lineToAdd.add(evalWekaIndex.getCostSensitive());
            lineToAdd.add(String.valueOf(evalWekaIndex.getTrainingPercentage()));
            lineToAdd.add(String.valueOf(evalWekaIndex.getPrecision()));
            lineToAdd.add(String.valueOf(evalWekaIndex.getRecall()));
            lineToAdd.add(String.valueOf(evalWekaIndex.getAuc()));
            lineToAdd.add(String.valueOf(evalWekaIndex.getKappa()));
            lineToAdd.add(String.valueOf(evalWekaIndex.getAccuracy()));
            lineToAdd.add(String.valueOf(evalWekaIndex.getTrueNegative()));
            lineToAdd.add(String.valueOf(evalWekaIndex.getTruePositive()));
            lineToAdd.add(String.valueOf(evalWekaIndex.getFalsePositive()));
            lineToAdd.add(String.valueOf(evalWekaIndex.getFalseNegative()));

            data.add(lineToAdd);

        }

        FileWriter out = new FileWriter(fileName);
        CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(headers));

        for (List<String> row : data) {
            printer.printRecord(row);
        }

        printer.close();
        out.close();
    }


}
