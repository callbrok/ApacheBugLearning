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


    public EvaluationWEKA() {
        // Blank constructor
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



    // Setter
    public void setProjectName(String projectName) {this.projectName = projectName;}
    public void setTrainingReleases(List<Release> trainingReleases) {this.trainingReleases = trainingReleases;}
    public void setTestingRelease(Release testingRelease) {this.testingRelease = testingRelease;}
    public void setClassifier(String classifier) {this.classifier = classifier;}
    public void setFeatureSelection(String featureSelection) {this.featureSelection = featureSelection;}
    public void setBalancing(String balancing) {this.balancing = balancing;}
    public void setCostSensitive(String costSensitive) {this.costSensitive = costSensitive;}
    public void setTrainingPercentage(Double trainingPercentage) {this.trainingPercentage = trainingPercentage;}
    public void setPrecision(Double precision) {this.precision = precision;}
    public void setRecall(Double recall) {this.recall = recall;}
    public void setAuc(Double auc) {this.auc = auc;}
    public void setKappa(Double kappa) {this.kappa = kappa;}
    public void setAccuracy(Double accuracy) {this.accuracy = accuracy;}
    public void setTrueNegative(Double trueNegative) {this.trueNegative = trueNegative;}
    public void setTruePositive(Double truePositive) {this.truePositive = truePositive;}
    public void setFalsePositive(Double falsePositive) {this.falsePositive = falsePositive;}
    public void setFalseNegative(Double falseNegative) {this.falseNegative = falseNegative;}
}
