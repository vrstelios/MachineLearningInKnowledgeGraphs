import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class W2VMODEL {
    private static final Logger logger = LoggerFactory.getLogger(W2VMODEL.class);

    public static void main(String[] args) {
        try {
            // 1. Φόρτωση οντολογίας
            OWLOntology ontology = loadOntology("Z:\\fucking program\\Msc\\Διπλωματική\\MachineLearningInKnowledgeGraphs\\biopax2\\Homo_sapiens.owl");

            // 2. Δημιουργία και αποθήκευση walks
            List<List<String>> walks = generateWalks(ontology, 13);
            saveWalks(walks, "walks.txt");

            // 3. Εκπαίδευση Word2Vec
            Word2Vec model = trainWord2Vec("walks.txt");

            // 4. Αποθήκευση μοντέλου
            saveModel(model, "java_w2v_model.zip");

            // 5. Αξιολόγηση
            evaluateModel(model);

        } catch (Exception e) {
            logger.error("Critical error: ", e);
            System.exit(1);
        }
    }

    private static OWLOntology loadOntology(String path) throws OWLOntologyCreationException {
        logger.info("Loading ontology from: ", path);
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        File ontologyFile = new File(path);

        if (!ontologyFile.exists()) {
            throw new IllegalArgumentException("The ontology file was not found: " + path);
        }

        return manager.loadOntologyFromOntologyDocument(ontologyFile);
    }

    private static List<List<String>> generateWalks(OWLOntology ontology, int walksPerEntity) {
        logger.info("Creation of random walks/per entity", walksPerEntity);
        RandomWalkGenerator rwGen = new RandomWalkGenerator(walksPerEntity);
        return rwGen.generateWalks(ontology);
    }

    private static void saveWalks(List<List<String>> walks, String outputPath) throws IOException {
        logger.info("Save walks to: ", outputPath);
        try (FileWriter writer = new FileWriter(outputPath)) {
            for (List<String> walk : walks) {
                writer.write(String.join(" ", walk) + System.lineSeparator());
            }
        }
    }

    private static Word2Vec trainWord2Vec(String walksFile) {
        logger.info("Word2Vec model training");
        try {
            SentenceIterator iter = new BasicLineIterator(walksFile);
            Word2Vec vec = new Word2Vec.Builder()
                    .minWordFrequency(1)
                    .iterations(15)
                    .layerSize(200)
                    .windowSize(5)
                    .negativeSample(10)
                    .iterate(iter)
                    .build();

            vec.fit();
            return vec;
        } catch (Exception e) {
            logger.error("Error during Word2Vec", e);
            throw new RuntimeException("Word2Vec failure", e);
        }
    }

    private static void saveModel(Word2Vec model, String outputPath) throws IOException {
        logger.info("Save model to:", outputPath);
        WordVectorSerializer.writeWord2VecModel(model, outputPath);
    }

    private static void evaluateModel(Word2Vec model) {
        logger.info("Model evaluation");
        EvaluationModel evaluator = new EvaluationModel(model);

        // Ορισμός πρωτεϊνών (προσαρμόστε ανάλογα)
        List<String> positive = Arrays.asList("http://www.reactome.org/biopax/48887#protein7832",
                "http://www.reactome.org/biopax/48887#protein1119",
                "http://www.reactome.org/biopax/48887#protein4268",
                "http://www.reactome.org/biopax/48887#protein8614",
                "http://www.reactome.org/biopax/48887#protein3070",
                "http://www.reactome.org/biopax/48887#protein7807",
                "http://www.reactome.org/biopax/48887#protein7872",
                "http://www.reactome.org/biopax/48887#protein199",
                "http://www.reactome.org/biopax/48887#protein3601",
                "http://www.reactome.org/biopax/48887#protein2147",
                "http://www.reactome.org/biopax/48887#protein227",
                "http://www.reactome.org/biopax/48887#protein323",
                "http://www.reactome.org/biopax/48887#protein18",
                "http://www.reactome.org/biopax/48887#protein4464",
                "http://www.reactome.org/biopax/48887#protein8889",
                "http://www.reactome.org/biopax/48887#protein1400",
                "http://www.reactome.org/biopax/48887#protein8665",
                "http://www.reactome.org/biopax/48887#protein1876",
                "http://www.reactome.org/biopax/48887#protein2631",
                "http://www.reactome.org/biopax/48887#protein1217",
                "http://www.reactome.org/biopax/48887#protein8852",
                "http://www.reactome.org/biopax/48887#protein2812",
                "http://www.reactome.org/biopax/48887#protein863",
                "http://www.reactome.org/biopax/48887#protein1911",
                "http://www.reactome.org/biopax/48887#protein1910",
                "http://www.reactome.org/biopax/48887#protein7748",
                "http://www.reactome.org/biopax/48887#protein1205",
                "http://www.reactome.org/biopax/48887#protein5401",
                "http://www.reactome.org/biopax/48887#protein9252",
                "http://www.reactome.org/biopax/48887#protein225",
                "http://www.reactome.org/biopax/48887#protein7335",
                "http://www.reactome.org/biopax/48887#protein1607",
                "http://www.reactome.org/biopax/48887#protein1350",
                "http://www.reactome.org/biopax/48887#protein339",
                "http://www.reactome.org/biopax/48887#protein875",
                "http://www.reactome.org/biopax/48887#protein63",
                "http://www.reactome.org/biopax/48887#protein5659",
                "http://www.reactome.org/biopax/48887#protein9016",
                "http://www.reactome.org/biopax/48887#protein4603",
                "http://www.reactome.org/biopax/48887#protein866",
                "http://www.reactome.org/biopax/48887#protein2699",
                "http://www.reactome.org/biopax/48887#protein5490",
                "http://www.reactome.org/biopax/48887#protein3602",
                "http://www.reactome.org/biopax/48887#protein3864",
                "http://www.reactome.org/biopax/48887#protein317",
                "http://www.reactome.org/biopax/48887#protein58",
                "http://www.reactome.org/biopax/48887#protein5019",
                "http://www.reactome.org/biopax/48887#protein59",
                "http://www.reactome.org/biopax/48887#protein746",
                "http://www.reactome.org/biopax/48887#protein7957");
        List<String> negative = Arrays.asList("http://www.reactome.org/biopax/48887#protein484",
                "http://www.reactome.org/biopax/48887#protein8427",
                "http://www.reactome.org/biopax/48887#protein1528",
                "http://www.reactome.org/biopax/48887#protein5958",
                "http://www.reactome.org/biopax/48887#protein4021",
                "http://www.reactome.org/biopax/48887#protein5406",
                "http://www.reactome.org/biopax/48887#protein1056",
                "http://www.reactome.org/biopax/48887#protein4458",
                "http://www.reactome.org/biopax/48887#protein8749",
                "http://www.reactome.org/biopax/48887#protein5269",
                "http://www.reactome.org/biopax/48887#protein1967",
                "http://www.reactome.org/biopax/48887#protein8827",
                "http://www.reactome.org/biopax/48887#protein8708",
                "http://www.reactome.org/biopax/48887#protein8982",
                "http://www.reactome.org/biopax/48887#protein5459",
                "http://www.reactome.org/biopax/48887#protein5981",
                "http://www.reactome.org/biopax/48887#protein5163",
                "http://www.reactome.org/biopax/48887#protein6384",
                "http://www.reactome.org/biopax/48887#protein5701",
                "http://www.reactome.org/biopax/48887#protein563",
                "http://www.reactome.org/biopax/48887#protein6195",
                "http://www.reactome.org/biopax/48887#protein3820",
                "http://www.reactome.org/biopax/48887#protein5938",
                "http://www.reactome.org/biopax/48887#protein852",
                "http://www.reactome.org/biopax/48887#protein5633",
                "http://www.reactome.org/biopax/48887#protein4872",
                "http://www.reactome.org/biopax/48887#protein7366",
                "http://www.reactome.org/biopax/48887#protein3266",
                "http://www.reactome.org/biopax/48887#protein1551",
                "http://www.reactome.org/biopax/48887#protein8388",
                "http://www.reactome.org/biopax/48887#protein8917",
                "http://www.reactome.org/biopax/48887#protein768",
                "http://www.reactome.org/biopax/48887#protein5749",
                "http://www.reactome.org/biopax/48887#protein5770",
                "http://www.reactome.org/biopax/48887#protein3910",
                "http://www.reactome.org/biopax/48887#protein4916",
                "http://www.reactome.org/biopax/48887#protein629",
                "http://www.reactome.org/biopax/48887#protein1293",
                "http://www.reactome.org/biopax/48887#protein2405",
                "http://www.reactome.org/biopax/48887#protein3860",
                "http://www.reactome.org/biopax/48887#protein2036",
                "http://www.reactome.org/biopax/48887#protein1279",
                "http://www.reactome.org/biopax/48887#protein1780",
                "http://www.reactome.org/biopax/48887#protein2789",
                "http://www.reactome.org/biopax/48887#protein2414",
                "http://www.reactome.org/biopax/48887#protein9173",
                "http://www.reactome.org/biopax/48887#protein4635",
                "http://www.reactome.org/biopax/48887#protein4535",
                "http://www.reactome.org/biopax/48887#protein6617",
                "http://www.reactome.org/biopax/48887#protein1220");

        EvaluationModel.ClassificationResults results = evaluator.trainAndEvaluateClassifier(positive, negative);

        logger.info("==========================================");
        logger.info(" FINAL CLASSIFICATION RESULTS");
        logger.info("==========================================");
        logger.info(String.format("%-10s: %.3f", "Accuracy", results.accuracy));
        logger.info(String.format("%-10s: %.3f", "Precision", results.precision));
        logger.info(String.format("%-10s: %.3f", "Recall", results.recall));
        logger.info(String.format("%-10s: %.3f", "F1-score", results.f1));
        logger.info(String.format("%-10s: %.3f", "AUC", results.auc));
    }
}