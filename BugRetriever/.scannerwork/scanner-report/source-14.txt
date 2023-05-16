package model;

import java.util.List;

public class EvaluationWEKA {
    private String projectName;
    private List<Release> trainingReleases;
    private Release testingRelease;


    private String classifier;
    private String featureSelection;
    private String balancing;
    private String costSensitive;


    private Double trainingPercentage;
    private Double precision;
    private Double recall;
    private Double auc;
    private Double kappa;
    private Double accuracy;
    private Double trueNegative;
    private Double truePositive;
    private Double falsePositive;
    private Double falseNegative;


    public EvaluationWEKA(String projectName, List<Release> trainingReleases, Release testingRelease, String classifier, String featureSelection, String balancing, String costSensitive, Double trainingPercentage, Double precision, Double recall, Double auc, Double kappa, Double accuracy, Double trueNegative, Double truePositive, Double falsePositive, Double falseNegative) {
        this.projectName = projectName;
        this.trainingReleases = trainingReleases;
        this.testingRelease = testingRelease;
        this.classifier = classifier;
        this.featureSelection = featureSelection;
        this.balancing = balancing;
        this.costSensitive = costSensitive;
        this.trainingPercentage = trainingPercentage;
        this.precision = precision;
        this.recall = recall;
        this.auc = auc;
        this.kappa = kappa;
        this.accuracy = accuracy;
        this.trueNegative = trueNegative;
        this.truePositive = truePositive;
        this.falsePositive = falsePositive;
        this.falseNegative = falseNegative;
    }


    // Getter
    public String getProjectName() {return projectName;}
    public List<Release> getTrainingReleases() {return trainingReleases;}
    public Release getTestingRelease() {return testingRelease;}
    public String getClassifier() {return classifier;}
    public String getFeatureSelection() {return featureSelection;}
    public String getBalancing() {return balancing;}
    public String getCostSensitive() {return costSensitive;}
    public Double getTrainingPercentage() {return trainingPercentage;}
    public Double getPrecision() {return precision;}
    public Double getRecall() {return recall;}
    public Double getAuc() {return auc;}
    public Double getKappa() {return kappa;}
    public Double getTrueNegative() {return trueNegative;}
    public Double getTruePositive() {return truePositive;}
    public Double getFalsePositive() {return falsePositive;}
    public Double getFalseNegative() {return falseNegative;}
    public Double getAccuracy() {return accuracy;}
}
