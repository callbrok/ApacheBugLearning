package controller;

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
    private static final String FILENAME="output.csv";
    public String buildCSV(List<ReleaseTag> tagRelesesWithBugginess) throws IOException {

        String[] headers = {"Version", "Filename", "NR", "NAUTHORS" ,"LOC", "LOC_ADDED", "AVGLOCADDED"
                , "MAXLOCADDED", "LOCTOUCHED", "CHURN", "AVGCHURN", "MAXCHURN"
                , "NPBM", "NPVM", "NSM", "NAM", "LOCM"
                , "BUGGY"};

        List<List<String>> data = new ArrayList<>();

        DecimalFormat df = new DecimalFormat("#.##");

        for(ReleaseTag rlstIndex : tagRelesesWithBugginess){

            for(RepoFile rpfIndex : rlstIndex.getReferencedFilesList()){
                List<String> lineToAdd = new ArrayList<>();

                lineToAdd.add(rlstIndex.getReleaseFromJira().getName());
                lineToAdd.add(rpfIndex.getPathOfFile());
                lineToAdd.add(String.valueOf(rpfIndex.getFileMetrics().getnRevision()));
                lineToAdd.add(String.valueOf(rpfIndex.getFileMetrics().getnAuth()));
                lineToAdd.add(String.valueOf(rpfIndex.getFileMetrics().getLoc()));
                lineToAdd.add(String.valueOf(rpfIndex.getFileMetrics().getLocAdded()));
                lineToAdd.add(String.valueOf(df.format(rpfIndex.getFileMetrics().getAverageLocAdded())));
                lineToAdd.add(String.valueOf(rpfIndex.getFileMetrics().getLocMaxAdded()));
                lineToAdd.add(String.valueOf(rpfIndex.getFileMetrics().getLocTouched()));
                lineToAdd.add(String.valueOf(rpfIndex.getFileMetrics().getChurn()));
                lineToAdd.add(String.valueOf(df.format(rpfIndex.getFileMetrics().getAverageChurn())));
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

        FileWriter out = new FileWriter(FILENAME);
        CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(headers));

        //create a File linked to the same file using the name of this one;
        File f = new File(FILENAME);

        for (List<String> row : data) {
            printer.printRecord(row);
        }

        printer.close();
        out.close();

        return f.getAbsolutePath();
    }

}
