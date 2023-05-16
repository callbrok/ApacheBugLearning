import controller.BugRetriever;
import controller.ReleaseRetriever;
import model.Bug;
import model.Release;
import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainJira {
    private static final Logger LOGGER = Logger.getLogger( MainJira.class.getName() );

    private static final String PROJECT="BOOKKEEPER";

    public static void main(String[] args) throws IOException, JSONException, ParseException {
        ReleaseRetriever gtf = new ReleaseRetriever();
        BugRetriever gtb = new BugRetriever();

        List<Release> released = gtf.getReleaseFromProject(PROJECT, true, "ALL");
        gtf.printReleaseList(released);

        List<Bug> validBug = gtb.getBug(PROJECT, false, released);

        LOGGER.log(Level.INFO, ("\n\n ------------------------------------------ \n\n"));

        int bugIndex = 0;
        for(Bug bg : validBug){
            bugIndex=bugIndex+1;
            gtb.printBugInformation(bg,bugIndex);
        }

    }
}
