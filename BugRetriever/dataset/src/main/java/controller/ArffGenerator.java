package controller;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

import java.io.File;
import java.io.IOException;

public class ArffGenerator {
    private static final String FILENAME="output.arff";

    public void buildArff(String csvPathFile) throws IOException {

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
        saver.setFile(new File(FILENAME));
        saver.writeBatch();
    }


}
