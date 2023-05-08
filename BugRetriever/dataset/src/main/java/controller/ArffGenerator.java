package controller;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

import java.io.File;
import java.io.IOException;

public class ArffGenerator {

    public void buildArff(String csvPathFile) throws IOException {
        String fileName = csvPathFile.substring(0, csvPathFile.indexOf(']')+1) + ".arff";

        // Load CSV
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File(csvPathFile));

        // Get instances object
        Instances data = loader.getDataSet();

        // Save ARFF
        ArffSaver saver = new ArffSaver();

        // Set the dataset we want to convert
        saver.setInstances(data);

        // 'Save as' ARFF
        saver.setFile(new File(fileName));
        saver.writeBatch();
    }


}
