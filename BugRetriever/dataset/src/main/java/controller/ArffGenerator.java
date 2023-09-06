package controller;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ArffGenerator {

    public String buildArff(String csvPathFile) throws Exception {
        String fileName = csvPathFile.substring(0, csvPathFile.indexOf(']')+1) + ".arff";

        // Load CSV
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File(csvPathFile));

        // Get instances object
        Instances data = loader.getDataSet();

        // Remove @attribute Filename from .arff file
        String[] options = new String[]{"-R", "2"};
        Remove removeFilter = new Remove();
        removeFilter.setOptions(options);
        removeFilter.setInputFormat(data);
        Instances newData = Filter.useFilter(data, removeFilter);


        // Save ARFF
        ArffSaver saver = new ArffSaver();

        // Set the dataset we want to convert
        saver.setInstances(newData);

        // 'Save as' ARFF
        File f = new File(fileName);
        saver.setFile(f);
        saver.writeBatch();


        // Setting attribute BUGGY class to YES,NO
        Path path = Paths.get(f.getAbsolutePath());
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        lines.set(19 - 1, "@attribute BUGGY {YES,NO}");
        Files.write(path, lines, StandardCharsets.UTF_8);


        return f.getAbsolutePath();
    }


}
