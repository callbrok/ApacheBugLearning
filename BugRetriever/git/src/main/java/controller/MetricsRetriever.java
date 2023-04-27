package controller;

import model.Metrics;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MetricsRetriever {

    public Metrics metricsHelper(Repository repository, TreeWalk treeWalk) throws IOException {
        Metrics metricsToReturn = new Metrics();


        // Retrieve LOC Metric
        int locMetric = locMetric(repository, treeWalk);

        



        metricsToReturn.setLoc(locMetric);

        return metricsToReturn;
    }


    private int locMetric(Repository repository, TreeWalk treeWalk) throws IOException {
        // Size (LOC):
        //      Lines of code of current file on the current release.

        ObjectLoader loader = repository.open(treeWalk.getObjectId(0));
        int lineNumber = 0;

        // 'loader.openstream' pass an inputstream object to loader
        BufferedReader reader = new BufferedReader(new InputStreamReader(loader.openStream()));

        while(reader.ready()) {
            String line = reader.readLine();
            lineNumber = lineNumber + 1;
        }

        System.out.println("loc: " + lineNumber);

        return lineNumber;
    }

}
