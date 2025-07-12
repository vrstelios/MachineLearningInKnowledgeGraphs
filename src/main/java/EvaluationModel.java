import org.deeplearning4j.models.word2vec.Word2Vec;
import smile.classification.RandomForest;
import smile.data.DataFrame;
import smile.data.Tuple;
import smile.data.formula.Formula;
import smile.data.type.DataTypes;
import smile.data.type.StructField;
import smile.data.type.StructType;
import smile.validation.metric.AUC;
import smile.validation.metric.Accuracy;
import smile.validation.metric.Precision;
import smile.validation.metric.Recall;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class EvaluationModel {
    private final Word2Vec model;

    public EvaluationModel(Word2Vec model) {
        this.model = model;
    }

    public ClassificationResults trainAndEvaluateClassifier(
            List<String> positiveProteins,
            List<String> negativeProteins
    ) {
        // 1. Προετοιμασία δεδομένων
        List<double[]> features = new ArrayList<>();
        List<Integer> labels = new ArrayList<>();

        int positiveCount = 0;
        int negativeCount = 0;

        for (String protein : positiveProteins) {
            if (model.hasWord(protein)) {
                features.add(model.getWordVector(protein));
                labels.add(1);
                positiveCount++;
            }
        }

        for (String protein : negativeProteins) {
            if (model.hasWord(protein)) {
                features.add(model.getWordVector(protein));
                labels.add(0);
                negativeCount++;
            }
        }

        // 2. Υπολογισμός class weights
        double[] classWeights = new double[2];
        classWeights[1] = 1.0 * (positiveCount + negativeCount) / (2.0 * positiveCount);
        classWeights[0] = 1.0 * (positiveCount + negativeCount) / (2.0 * negativeCount);

        final double[][] X = features.toArray(new double[0][]);
        final int[] y = labels.stream().mapToInt(i -> i).toArray();

        // 3. Stratified Split (70% train - 30% test)
        StratifiedSplit.TrainTestSplit split = StratifiedSplit.split(X, y, 0.3);
        double[][] X_train = split.X_train;
        double[][] X_test = split.X_test;
        int[] y_train = split.y_train;
        int[] y_test = split.y_test;

        // 4. Δημιουργία DataFrame
        int vectorSize = X_train[0].length;
        StructField[] fields = new StructField[vectorSize + 1];
        for (int i = 0; i < vectorSize; i++) {
            fields[i] = new StructField("feature_" + i, DataTypes.DoubleType);
        }
        fields[vectorSize] = new StructField("label", DataTypes.IntegerType);
        StructType schema = new StructType(fields);

        List<Tuple> trainRows = new ArrayList<>();
        for (int i = 0; i < X_train.length; i++) {
            Object[] values = new Object[vectorSize + 1];
            for (int j = 0; j < vectorSize; j++) {
                values[j] = X_train[i][j];
            }
            values[vectorSize] = y_train[i];
            trainRows.add(Tuple.of(values, schema));
        }
        DataFrame trainData = DataFrame.of(trainRows);

        // 5. Ρύθμιση Random Forest
        Properties params = new Properties();
        params.setProperty("smile.random.forest.trees", "200"); // Αύξηση δέντρων
        params.setProperty("smile.random.forest.node.size", "5"); // Μικρότερα leaves
        System.out.println("Class weights: " + Arrays.toString(classWeights));

        RandomForest forest = RandomForest.fit(Formula.lhs("label"), trainData, params);

        // 6. Πρόβλεψη και αξιολόγηση
        List<Tuple> testRows = new ArrayList<>();
        for (int i = 0; i < X_test.length; i++) {
            Object[] values = new Object[vectorSize + 1];
            for (int j = 0; j < vectorSize; j++) {
                values[j] = X_test[i][j];
            }
            values[vectorSize] = y_test[i];
            testRows.add(Tuple.of(values, schema));
        }
        DataFrame testData = DataFrame.of(testRows);


        int[] y_pred = forest.predict(testData);
        double[] y_proba = Arrays.stream(y_pred).mapToDouble(p -> p == 1 ? 1.0 : 0.0).toArray();

        double accuracy = Accuracy.of(y_test, y_pred);
        double precision = Precision.of(y_test, y_pred);
        double recall = Recall.of(y_test, y_pred);
        double f1 = 2 * (precision * recall) / (precision + recall);
        double auc = AUC.of(y_test, y_proba);

        return new ClassificationResults(accuracy, precision, recall, f1, auc);
    }

    public static class ClassificationResults {
        public final double accuracy;
        public final double precision;
        public final double recall;
        public final double f1;
        public final double auc;

        public ClassificationResults(double accuracy, double precision, double recall, double f1, double auc) {
            this.accuracy = accuracy;
            this.precision = precision;
            this.recall = recall;
            this.f1 = f1;
            this.auc = auc;
        }
    }
}