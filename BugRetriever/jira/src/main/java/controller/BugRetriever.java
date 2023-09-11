package controller;

import model.Bug;
import model.Release;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BugRetriever {
    private static final Logger LOGGER = Logger.getLogger( BugRetriever.class.getName() );
    private static final String FIELDS = "fields";


    public List<Bug> getBug(String projectName, Boolean coldStartEnabler, List<Release> released) throws IOException, ParseException {
        List<Bug> validBug = new ArrayList<>();
        List<Bug> validBugNoProportion = new ArrayList<>();

        JSONHelper jsonhlp = new JSONHelper();

        int j;
        int totalBug;
        int i=0;

        do{
            j = i + 1000;

            // "%20ORDER%20BY%20key%20ASC" tag, order by the oldest Key first
            String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project%20%3D%20" + projectName +
                    "%20AND%20issuetype%20%3D%20Bug%20AND%20(%22status%22%20%3D%22resolved%22%20OR%20%22status" +
                    "%22%20%3D%20%22closed%22)%20AND%20%20%22resolution%22%20%3D%20%22fixed%22%20" + "%20ORDER%20BY%20key%20ASC" +
                    "&fields=key,resolutiondate,versions,created,fixVersions&startAt=" + i + "&maxResults=" + j;


            JSONObject json = jsonhlp.readJsonFromUrl(url);

            // Retrieve number of total BUG
            totalBug = json.getInt("total");

            // Getting all issues
            JSONArray jsonArrayIssues = json.getJSONArray("issues");

            // Scroll all issues
            for(; i < totalBug && i < j; i++){
                // Init new Bug object
                Bug bug = new Bug();

                // Get i-stance of issue field
                JSONObject currentJsonIssue = jsonArrayIssues.getJSONObject(i%1000);

                // Retrieve data for i-issue
                String nameKey = currentJsonIssue.getString("key");

                // Retrieve resolution ticket date
                Date resolutionTicketDate = new SimpleDateFormat("yyyy-MM-dd").parse(currentJsonIssue.getJSONObject(FIELDS).getString("resolutiondate"));

                // Retrieve creation ticket date
                Date creationTicketDate = new SimpleDateFormat("yyyy-MM-dd").parse(currentJsonIssue.getJSONObject(FIELDS).getString("created"));

                // Retrieve Fixed Version release
                Release fixedVersion = retrieveFixedANDOpeningVersion(resolutionTicketDate, released);

                // Retrieve Opening Version release
                Release openingVersion = retrieveFixedANDOpeningVersion(creationTicketDate, released);


                // Set all bug field except the Affected Version List
                bug.setBug(nameKey, resolutionTicketDate, creationTicketDate, openingVersion, fixedVersion);

                // If after setting the bug is not valid skip it
                if(Boolean.FALSE.equals(bug.getValid())){continue;}

                //  Retrieve Injected versions
                JSONArray jsonArrayAV = currentJsonIssue.getJSONObject(FIELDS).getJSONArray("versions");
                Release injectedVersion = injectedVersionsFromJsonFields(jsonArrayAV, released, bug);     // passo a proportion quindi ad affected version la lista dei bug creati fino ad adesso

                // Set affected version to current valid bug, there are three situation:
                //      1. There are valid affected version, and there was set
                //      2. Bug required proportion algorithm, so if has the first Affected Version Release name: "DOPROPORTION",
                //         don't set now
                //      3. I'm in cold start process, so I need to discard all bugs that need proportion and so bug with has the
                //         first Affected Version Release name: "DOPROPORTION"
                //      4. After setting the affected version the bug is found to be invalid
                //
                bug.setAffectedAndInjectedVersions(injectedVersion, released);

                if(Boolean.TRUE.equals(bug.getValid())){validBug.add(bug);}

            }


        }while(i<totalBug);


        // If I'm in Cold Start process pass buf that not required proportion algorithm
        if(Boolean.TRUE.equals(coldStartEnabler)){
            return coldStartValidBugNoProportion(validBug, validBugNoProportion);
        }

        // Else do proportion algorithms
        Proportion propAlgo = new Proportion();
        return propAlgo.proportionAlgoHelper(validBug, released);
    }


    private List<Bug> coldStartValidBugNoProportion( List<Bug> validBug, List<Bug> validBugNoProportion){
        // Scroll all stored bugs of current project
        for(Bug validBugIndex: validBug){
            // If I'm in cold start process skip bug that required proportiona lgorithm
            if(validBugIndex.getAffectedVersions().get(0).getName().equals("DOPROPORTION")){continue;}
            validBugNoProportion.add(validBugIndex);
        }
        return validBugNoProportion;
    }


    private Release injectedVersionsFromJsonFields(JSONArray jsonArray, List<Release> released, Bug currentBug) {
        List<Release> affectedVersionReleases = new ArrayList<>();

        // If there are at least one affected version, retrieve affected version release from their name
        // For each name take the release object related to JSON name field, and discard the not released tagged release
        for (int x = 0; x < jsonArray.length(); x++) {

            // Retrieve affected version name from JSON
            String currAffectedName = jsonArray.getJSONObject(x).getString("name");

            // Check if the name match with a released version
            for (Release releaseIndex : released) {
                if (releaseIndex.getName().equals(currAffectedName)) {
                    affectedVersionReleases.add(releaseIndex);

                    // Flag false proportion tag on the current bug
                    currentBug.setPropotionaled(false);
                }
            }

        }

        // If there aren't affected version in Jira ticket or the affected version indicated
        // is not "released" tagged (no release added to "releaseToReturn" list) do PROPORTION ALGORITHMS
        // and so tag the bug by set the first Affected Version with a Release named "DOPROPORTION"
        //
        // If I'm in Cold Start don't do proportion algorithm, store only the bugs that have all parameters
        // for pFormula, so also the Injected Version, cold start enabler it was set to 'true' for skip
        // proportion algorithm and return a default irrilevant list of release
        if(( jsonArray.isEmpty() || affectedVersionReleases.isEmpty() )){return new Release("DOPROPORTION");}

        // Order affected version list by date ASC
        Comparator<Release> comparatorAsc = Comparator.comparingLong(rls -> rls.getReleaseDate().getTime());
        affectedVersionReleases.sort(comparatorAsc);

        return affectedVersionReleases.get(0);
    }


    private Release retrieveFixedANDOpeningVersion(Date ticketDate, List<Release> released){
        // The fixed version is the first release after the closing ticket date
        // The opening version is the first release after the creation ticket date


        // Set a default Discarded release, for case which the fixed/opening version is not released
        Release releaseToReturn = new Release("NULL");

        // Return the first release tagged released after the resolution/creation ticket date
        for (Release releaseIndex : released) {
            if (ticketDate.before(releaseIndex.getReleaseDate())) {
                return releaseIndex;
            }
        }

        return releaseToReturn;
    }

    private static final String PASSATO_NULL = " PASSATO VALORE null";

    public void printBugInformation(Bug bug, int bugIndex){

        // Print Bug Number
        LOGGER.log(Level.INFO, () -> "\n+-- BUG N." + bugIndex);

        // Print propotionaled
        LOGGER.log(Level.INFO, ("\n+ PROPORTION: " + bug.getPropotionaled()));

        // Print valid
        LOGGER.log(Level.INFO, ("\n+ VALID: " + bug.getValid()));

        // Print namekey
        LOGGER.log(Level.INFO, ("\n+ NAME: " + bug.getNameKey()));

        // Print Affected Version
        List<Release> affectedReleases = bug.getAffectedVersions();
        LOGGER.log(Level.INFO, ("\n+ AFFECTED VERSION: "));

        if(affectedReleases == null){ LOGGER.log(Level.INFO, (PASSATO_NULL));}
        else{
            for (Release index : affectedReleases) {
                LOGGER.log(Level.INFO, (index.getName() + " "));
            }
        }


        // Print data creazione
        LOGGER.log(Level.INFO, ("\n+ DATA CREAZIONE: " + bug.getCreationTicketDate()));

        // Print data risoluzione
        LOGGER.log(Level.INFO, ("\n+ DATA RISOLUZIONE: " + bug.getResolutionTicketDate()));

        // Print fixedversion
        LOGGER.log(Level.INFO, ("\n+ FIXED VERSION: "));
        if(bug.getFixedVersions() == null){ LOGGER.log(Level.INFO, (PASSATO_NULL));}
        else {
            LOGGER.log(Level.INFO, (bug.getFixedVersions().getName()));
        }

        // Print openingversion
        LOGGER.log(Level.INFO, ("\n+ OPENING VERSION: "));
        if(bug.getOpeningVersion() == null){ LOGGER.log(Level.INFO, (PASSATO_NULL));}
        else {
            LOGGER.log(Level.INFO, (bug.getOpeningVersion().getName()));
        }

        // Print injected version
        LOGGER.log(Level.INFO, ("\n+ INJECTED VERSION: "));
        if(bug.getInjectedVersion() == null){ LOGGER.log(Level.INFO, (PASSATO_NULL));}
        else {
            LOGGER.log(Level.INFO, (bug.getInjectedVersion().getName()));
        }


        LOGGER.log(Level.INFO, ("\n+--------------------------\n"));

    }

}
