import java.util.*;

public class StratifiedSplit {

    public static TrainTestSplit split(double[][] X, int[] y, double testSize) {
        // 1. Ομαδοποίηση δεδομένων ανά κλάση
        Map<Integer, List<Integer>> classIndices = new HashMap<>();
        for (int i = 0; i < y.length; i++) {
            classIndices.computeIfAbsent(y[i], k -> new ArrayList<>()).add(i);
        }

        // 2. Δημιουργία split για κάθε κλάση ξεχωριστά
        List<Integer> trainIndices = new ArrayList<>();
        List<Integer> testIndices = new ArrayList<>();

        for (Map.Entry<Integer, List<Integer>> entry : classIndices.entrySet()) {
            List<Integer> indices = entry.getValue();
            Collections.shuffle(indices);
            int splitPoint = (int) (indices.size() * (1 - testSize));
            trainIndices.addAll(indices.subList(0, splitPoint));
            testIndices.addAll(indices.subList(splitPoint, indices.size()));
        }

        // 3. Ανακατέματτο των δεικτών
        Collections.shuffle(trainIndices);
        Collections.shuffle(testIndices);

        // 4. Δημιουργία των splits (διορθωμένη έκδοση)
        double[][] X_train = trainIndices.stream()
                .map(i -> X[i])
                .toArray(double[][]::new);

        int[] y_train = trainIndices.stream()
                .mapToInt(i -> y[i])
                .toArray();

        double[][] X_test = testIndices.stream()
                .map(i -> X[i])
                .toArray(double[][]::new);

        int[] y_test = testIndices.stream()
                .mapToInt(i -> y[i])
                .toArray();

        return new TrainTestSplit(X_train, X_test, y_train, y_test);
    }

    public static class TrainTestSplit {
        public final double[][] X_train;
        public final double[][] X_test;
        public final int[] y_train;
        public final int[] y_test;

        public TrainTestSplit(double[][] X_train, double[][] X_test,
                              int[] y_train, int[] y_test) {
            this.X_train = X_train;
            this.X_test = X_test;
            this.y_train = y_train;
            this.y_test = y_test;
        }
    }
}