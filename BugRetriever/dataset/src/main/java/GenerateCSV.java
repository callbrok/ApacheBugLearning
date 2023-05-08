import controller.ArffGenerator;
import controller.CSVGenerator;
import controller.GitController;

public class GenerateCSV {

    private static final String PROJECT="BOOKKEEPER";

    public static void main(String[] args) throws Exception {
        GitController gtc = new GitController();
        CSVGenerator csv = new CSVGenerator();
        ArffGenerator arff = new ArffGenerator();

        arff.buildArff(csv.buildCSV(gtc.retrieveAllGitDataSet(PROJECT)));
    }
}
