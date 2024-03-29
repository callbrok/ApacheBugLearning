package controller;

import model.EvaluationWEKA;
import model.Release;
import weka.classifiers.CostMatrix;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SpreadSubsample;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WEKAHelper {
    private static final Logger LOGGER = Logger.getLogger( WEKAHelper.class.getName() );


    // COST MATRIX
    private static final Double COSTFALSEPOSITIVE = 10.0;
    private static final Double COSTFALSENEGATIVE = 1.0;

    // WEKA PARAMETERS
    //    Classifiers
    public static final String RANDOM_FOREST = "RANDOM_FOREST";
    public static final String NATIVE_BAYES = "NATIVE_BAYES";
    public static final String IBK = "IBK";

    //    Feature Selection
    public static final String NO_FEATURE_SELECTION = "NO_FEATURE_SELECTION";
    public static final String BEST_FIRST = "BEST_FIRST";

    //    Balancing
    public static final String NO_BALANCING = "NO_BALANCING";
    public static final String UNDERSAMPLING = "UNDERSAMPLING";
    public static final String OVERSAMPLING = "OVERSAMPLING";
    public static final String SMOTE = "SMOTE";

    //    Cost Sensitive
    public static final String NO_COST_SENSITIVE = "NO_COST_SENSITIVE";
    public static final String SENSITIVE_LEARNING = "SENSITIVE_LEARNING";
    public static final String SENSITIVE_THRESHOLD = "SENSITIVE_THRESHOLD";


    public List<EvaluationWEKA> evaluationWEKA(String projectName, List<Release> trainingReleases, Release testingRelease, String trainingSourcePath,
                                                           String testingSourcePath, String featureSelectionIndicator, String balancingIndicator,
                                                           String costSensitiveIndicator) throws Exception {

        DataSource trainingSource = new DataSource(trainingSourcePath);
        Instances training = trainingSource.getDataSet();

        DataSource testingSource = new DataSource(testingSourcePath);
        Instances testing = testingSource.getDataSet();

        int numAttr = training.numAttributes();
        training.setClassIndex(numAttr - 1);
        testing.setClassIndex(numAttr - 1);

        Evaluation eval = new Evaluation(testing);


        // FILTERS ------------------------------------------------------------
        //    Apply Feature Selection
        List<Instances> tt = applyFeatureSelection(training, testing, featureSelectionIndicator);
        training = tt.get(0);
        testing = tt.get(1);

        //    Apply Balancing to the Training Set
        training = applyBalancing(training, balancingIndicator);

        // ---------------------------------------------------------------------


        //     - Random Forest
        if(costSensitiveIndicator.equals(NO_COST_SENSITIVE)){
            RandomForest randomForestClassifier = new RandomForest();
            randomForestClassifier.buildClassifier(training);
            eval.evaluateModel(randomForestClassifier, testing);
        }
        else{
            //    Apply Cost Sensitive
            CostSensitiveClassifier costSensitiveClassifier = applyCostSensitive(training, costSensitiveIndicator, RANDOM_FOREST);
            eval = new Evaluation(testing, costSensitiveClassifier.getCostMatrix());
            eval.evaluateModel(costSensitiveClassifier, testing);
        }
        LOGGER.log(Level.INFO, ("   + fatto  - RANDOM FOREST"));
        EvaluationWEKA randomForestEvaluationWeka = new EvaluationWEKA();

        randomForestEvaluationWeka.setProjectName(projectName);
        randomForestEvaluationWeka.setTrainingReleases(trainingReleases);
        randomForestEvaluationWeka.setTestingRelease(testingRelease);
        randomForestEvaluationWeka.setClassifier(RANDOM_FOREST);
        randomForestEvaluationWeka.setFeatureSelection(featureSelectionIndicator);
        randomForestEvaluationWeka.setBalancing(balancingIndicator);
        randomForestEvaluationWeka.setCostSensitive(costSensitiveIndicator);
        randomForestEvaluationWeka.setTrainingPercentage(100.0*training.numInstances()/(training.numInstances()+testing.numInstances()));
        randomForestEvaluationWeka.setPrecision(eval.precision(0));
        randomForestEvaluationWeka.setRecall(eval.recall(0));
        randomForestEvaluationWeka.setAuc(eval.areaUnderROC(0));
        randomForestEvaluationWeka.setKappa(eval.kappa());
        randomForestEvaluationWeka.setAccuracy(eval.pctCorrect());
        randomForestEvaluationWeka.setTrueNegative(eval.numTrueNegatives(0));
        randomForestEvaluationWeka.setTruePositive(eval.numTruePositives(0));
        randomForestEvaluationWeka.setFalsePositive(eval.numFalsePositives(0));
        randomForestEvaluationWeka.setFalseNegative(eval.numFalseNegatives(0));


        //     - Native Bayes
        eval = new Evaluation(testing);  // Reset eval

        if(costSensitiveIndicator.equals(NO_COST_SENSITIVE)){
            NaiveBayes naiveBayesClassifier = new NaiveBayes();
            naiveBayesClassifier.buildClassifier(training);
            eval.evaluateModel(naiveBayesClassifier, testing);
        }
        else{
            CostSensitiveClassifier costSensitiveClassifier = applyCostSensitive(training, costSensitiveIndicator, NATIVE_BAYES);
            eval = new Evaluation(testing, costSensitiveClassifier.getCostMatrix());
            eval.evaluateModel(costSensitiveClassifier, testing);
        }
        LOGGER.log(Level.INFO, ("   + fatto  - NATIVE BAYES"));
        EvaluationWEKA nativeBayesEvaluationWeka = new EvaluationWEKA();

        nativeBayesEvaluationWeka.setProjectName(projectName);
        nativeBayesEvaluationWeka.setTrainingReleases(trainingReleases);
        nativeBayesEvaluationWeka.setTestingRelease(testingRelease);
        nativeBayesEvaluationWeka.setClassifier(NATIVE_BAYES);
        nativeBayesEvaluationWeka.setFeatureSelection(featureSelectionIndicator);
        nativeBayesEvaluationWeka.setBalancing(balancingIndicator);
        nativeBayesEvaluationWeka.setCostSensitive(costSensitiveIndicator);
        nativeBayesEvaluationWeka.setTrainingPercentage(100.0*training.numInstances()/(training.numInstances()+testing.numInstances()));
        nativeBayesEvaluationWeka.setPrecision(eval.precision(0));
        nativeBayesEvaluationWeka.setRecall(eval.recall(0));
        nativeBayesEvaluationWeka.setAuc(eval.areaUnderROC(0));
        nativeBayesEvaluationWeka.setKappa(eval.kappa());
        nativeBayesEvaluationWeka.setAccuracy(eval.pctCorrect());
        nativeBayesEvaluationWeka.setTrueNegative(eval.numTrueNegatives(0));
        nativeBayesEvaluationWeka.setTruePositive(eval.numTruePositives(0));
        nativeBayesEvaluationWeka.setFalsePositive(eval.numFalsePositives(0));
        nativeBayesEvaluationWeka.setFalseNegative(eval.numFalseNegatives(0));


        //     - IBK
        eval = new Evaluation(testing);  // Reset eval

        if(costSensitiveIndicator.equals(NO_COST_SENSITIVE)){
            IBk ibkClassifier = new IBk();
            ibkClassifier.buildClassifier(training);
            eval.evaluateModel(ibkClassifier, testing);
        }
        else{
            CostSensitiveClassifier costSensitiveClassifier = applyCostSensitive(training, costSensitiveIndicator, IBK);
            eval = new Evaluation(testing, costSensitiveClassifier.getCostMatrix());
            eval.evaluateModel(costSensitiveClassifier, testing);
        }
        LOGGER.log(Level.INFO, ("   + fatto  - IBK"));
        EvaluationWEKA IBKEvaluationWeka = new EvaluationWEKA();

        IBKEvaluationWeka.setProjectName(projectName);
        IBKEvaluationWeka.setTrainingReleases(trainingReleases);
        IBKEvaluationWeka.setTestingRelease(testingRelease);
        IBKEvaluationWeka.setClassifier(IBK);
        IBKEvaluationWeka.setFeatureSelection(featureSelectionIndicator);
        IBKEvaluationWeka.setBalancing(balancingIndicator);
        IBKEvaluationWeka.setCostSensitive(costSensitiveIndicator);
        IBKEvaluationWeka.setTrainingPercentage(100.0*training.numInstances()/(training.numInstances()+testing.numInstances()));
        IBKEvaluationWeka.setPrecision(eval.precision(0));
        IBKEvaluationWeka.setRecall(eval.recall(0));
        IBKEvaluationWeka.setAuc(eval.areaUnderROC(0));
        IBKEvaluationWeka.setKappa(eval.kappa());
        IBKEvaluationWeka.setAccuracy(eval.pctCorrect());
        IBKEvaluationWeka.setTrueNegative(eval.numTrueNegatives(0));
        IBKEvaluationWeka.setTruePositive(eval.numTruePositives(0));
        IBKEvaluationWeka.setFalsePositive(eval.numFalsePositives(0));
        IBKEvaluationWeka.setFalseNegative(eval.numFalseNegatives(0));


        return List.of(randomForestEvaluationWeka, nativeBayesEvaluationWeka, IBKEvaluationWeka);
    }


    private CostSensitiveClassifier applyCostSensitive(Instances training, String costSensitiveIndicator, String technique) throws Exception {
        boolean minimize;

        switch (costSensitiveIndicator){
            case SENSITIVE_LEARNING -> minimize = false;
            case SENSITIVE_THRESHOLD -> minimize = true;

            default -> throw new IllegalArgumentException("IMPOSSIBILE APPLICARE UNA TECNICA DI COST SENSITIVE");
        }

        // Init Matrix Costs
        CostMatrix cm = new CostMatrix(2);
        cm.setCell(0,0,0.0);
        cm.setCell(0,1, COSTFALSEPOSITIVE);
        cm.setCell(1,0, COSTFALSENEGATIVE);
        cm.setCell(1,1,0.0);

        CostSensitiveClassifier costSensitiveClassifier = new CostSensitiveClassifier();

        switch (technique){
            case RANDOM_FOREST -> {
                RandomForest randomForestClassifier = new RandomForest();
                costSensitiveClassifier.setClassifier(randomForestClassifier);
            }
            case NATIVE_BAYES -> {
                NaiveBayes naiveBayesClassifier = new NaiveBayes();
                costSensitiveClassifier.setClassifier(naiveBayesClassifier);
            }
            case IBK -> {
                IBk ibkClassifier = new IBk();
                costSensitiveClassifier.setClassifier(ibkClassifier);
            }

            default -> throw new IllegalArgumentException("IMPOSSIBILE APPLICARE AL COST SENSITIVE UNA TECNICA DI LABELING");
        }

        costSensitiveClassifier.setCostMatrix(cm);
        costSensitiveClassifier.setMinimizeExpectedCost(minimize);
        costSensitiveClassifier.buildClassifier(training);

        return costSensitiveClassifier;
    }



    private List<Instances> applyFeatureSelection(Instances training, Instances testing, String featureSelectionIndicator) throws Exception {

        switch (featureSelectionIndicator){
            case NO_FEATURE_SELECTION -> {
                return List.of(training, testing);
            }
            case BEST_FIRST -> {
                BestFirst bestFirst = new BestFirst();
                //-D <0 = backward | 1 = forward | 2 = bi-directional>
                //        Direction of search. (default = 1)
                bestFirst.setOptions(new String[] {"-D", "0"});

                AttributeSelection filter = new AttributeSelection();
                CfsSubsetEval eval = new CfsSubsetEval();

                filter.setEvaluator(eval);
                filter.setSearch(bestFirst);
                filter.setInputFormat(training);

                Instances filteredTraining = Filter.useFilter(training, filter);
                Instances testingFiltered = Filter.useFilter(testing, filter);

                int numAttrFiltered = filteredTraining.numAttributes();

                filteredTraining.setClassIndex(numAttrFiltered - 1);
                testingFiltered.setClassIndex(numAttrFiltered - 1);

                return List.of(filteredTraining, testingFiltered);
            }

            default -> throw new IllegalArgumentException("IMPOSSIBILE APPLICARE UNA TECNICA DI FEATURE SELECTION");
        }
    }


    private Instances applyBalancing(Instances training, String balancingIndicator) throws Exception {
        int numberOfYES = 0;
        int numberOfNO = 0;

        for (Instance row : training) {
            if (row.stringValue(row.classIndex()).equals("YES")) {numberOfYES+=1;}
            else {numberOfNO+=1;}
        }

        int minoritySize = Math.min(numberOfYES, numberOfNO);
        int majoritySize = Math.max(numberOfYES, numberOfNO);


        switch (balancingIndicator) {
            case NO_BALANCING -> {
                return training;
            }
            case UNDERSAMPLING -> {
                SpreadSubsample spreadSubsample = new SpreadSubsample();

                // Choose uniform distribution for spread
                // (see: https://weka.sourceforge.io/doc.dev/weka/filters/supervised/instance/SpreadSubsample.html)
                spreadSubsample.setOptions(new String[]{"-M", "1.0"});
                spreadSubsample.setInputFormat(training);
                return Filter.useFilter(training, spreadSubsample);
            }
            case OVERSAMPLING -> {
                Resample resample = new Resample();

                // -B -> Choose uniform distribution
                // (see: https://weka.sourceforge.io/doc.dev/weka/filters/supervised/instance/Resample.html)
                // -Z -> From https://waikato.github.io/weka-blog/posts/2019-01-30-sampling/
                // "where Y/2 is (approximately) the percentage of data that belongs to the majority class"
                String z = Double.toString(2 * ((double) majoritySize / training.size()) * 100);
                resample.setOptions(new String[]{"-B", "1.0", "-Z", z});
                resample.setInputFormat(training);
                return Filter.useFilter(training, resample);
            }
            case SMOTE -> {
                SMOTE smote = new SMOTE();

                // Percentage of SMOTE instances to create
                // (see: https://weka.sourceforge.io/doc.packages/SMOTE/weka/filters/supervised/instance/SMOTE.html)
                String p = (minoritySize > 0) ?
                        Double.toString(100.0 * (majoritySize - minoritySize) / minoritySize) : "100.0";
                smote.setOptions(new String[]{"-P", p});
                smote.setInputFormat(training);
                return Filter.useFilter(training, smote);
            }

            default -> throw new IllegalArgumentException("IMPOSSIBILE APPLICARE UNA TECNICA DI BALANCING SUL TRAINING SET");
        }

    }


}
