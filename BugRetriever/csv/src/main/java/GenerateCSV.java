import controller.CSVGenerator;
import controller.GitController;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.text.ParseException;

public class GenerateCSV {

    private static final String PROJECT="BOOKKEEPER";

    public static void main(String[] args) throws Exception {
        GitController gtc = new GitController();
        CSVGenerator csv = new CSVGenerator();

        csv.buildCSV(gtc.retrieveAllGitDataSet(PROJECT));
    }
}
