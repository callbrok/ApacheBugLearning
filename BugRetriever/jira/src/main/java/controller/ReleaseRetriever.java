package controller;

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

public class ReleaseRetriever {

    public List<Release> getReleaseFromProject(String projectName, Boolean getHalfReleases) throws IOException, ParseException {
        String url = "https://issues.apache.org/jira/rest/api/2/project//" + projectName;

        JSONObject json = JSONHelper.readJsonFromUrl(url);

        // Get locations array from the JSON Object and store it into JSONArray
        JSONArray jsonArray = json.getJSONArray("versions");

        // Init Release object list
        List<Release> releases = new ArrayList<>();

        // Iterate jsonArray using for loop
        for (int i = 0; i < jsonArray.length(); i++) {

            // Store each object in JSONObject
            JSONObject explrObject = jsonArray.getJSONObject(i);

            // Check if not released, so skip
            if(!explrObject.getBoolean("released")){ continue; }

            // Get field value from JSONObject using get() method
            int releaseId = explrObject.getInt("id");
            String releaseName = explrObject.getString("name");

            // Get release date and check it
            // Check if "releaseDate" field exist in Json, (like STORM field n.5)
            if (!explrObject.has("releaseDate")) {continue;}
            Date releaseDate = new SimpleDateFormat("yyyy-MM-dd").parse(explrObject.getString("releaseDate"));

            // Init Release object
            Release release = new Release(releaseId, releaseName, releaseDate);

            // Add to releases list
            releases.add(release);
        }

        // Sorting list by date -----------------------------------------
        //     1. get Comparator using Lambda expression
        Comparator<Release> comparatorAsc = Comparator.comparingLong(rls -> rls.getReleaseDate().getTime());

        //     2. pass above Comparator and sort in ascending order
        releases.sort(comparatorAsc);


        if(getHalfReleases){return setReleaseIndex(releases.subList(0, (releases.size()-1)/2));}
        return setReleaseIndex(releases);
    }

    private List<Release> setReleaseIndex(List<Release> releases){
        // Set indexes to sorted releases list elements
        int x=1;

        for (Release index : releases) {
            index.setIndex(x);
            x = x+1;
        }

        return releases;
    }


    public void printReleaseList(List<Release> released){
        // Print release objects list

        System.err.print("\n\n+--------- LISTA RELEASE 'RELEASED' ---------\n\n");

        for (Release index : released) {
            System.err.print(index.getIndex() + " | " + index.getId() + " | " + index.getName() + " | " + index.getReleaseDate() + "\n");
        }

        System.err.print("\n+ LA LISTA E'COMPOSTA DA " + released.size() + " RELEASE");
        System.err.print("\n+--------------------------------------------\n\n");
    }


}
