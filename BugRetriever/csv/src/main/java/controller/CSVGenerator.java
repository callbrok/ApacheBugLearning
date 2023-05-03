package controller;

import model.ReleaseTag;
import model.RepoFile;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVGenerator {
    private static final String FILENAME="output.csv";
    public void buildCSV(List<ReleaseTag> tagRelesesWithBugginess) throws IOException {

        String[] headers = {"Version", "Filename", "NR", "NAUTHORS" ,"LOC", "LOC_ADDED", "AVGLOCADDED"
                , "MAXLOCADDED", "LOCTOUCHED", "CHURN", "AVGCHURN", "MAXCHURN", "BUGGY"};

        List<List<String>> data = new ArrayList<>();


        for(ReleaseTag rlstIndex : tagRelesesWithBugginess){

            for(RepoFile rpfIndex : rlstIndex.getReferencedFilesList()){
                List<String> lineToAdd = new ArrayList<>();

                lineToAdd.add(rlstIndex.getReleaseFromJira().getName());
                lineToAdd.add(rpfIndex.getPathOfFile().toString());
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
                lineToAdd.add(rpfIndex.getItsBuggyCSV());

                data.add(lineToAdd);
            }
        }

        FileWriter out = new FileWriter(FILENAME);
        CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(headers));

        for (List<String> row : data) {
            printer.printRecord(row);
        }

        printer.close();
        out.close();

    }

}
