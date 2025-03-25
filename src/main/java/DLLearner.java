import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.algorithms.celoe.OEHeuristicRuntime;
import org.dllearner.core.*;
import org.dllearner.core.StringRenderer.Rendering;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.ExistentialRestrictionMaterialization;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.refinementoperators.RhoDRDown;
import org.semanticweb.elk.owlapi.ElkReasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DLLearner {
    private static final Logger logger = Logger.getLogger(DLLearner.class); // Για καταγραφή μηνυμάτων (logs)
    private static final String kbPathStr = "http://localhost:3030/bioPax2/sparql";

    // Αρνητικά παραδείγματα (πρωτεΐνες χωρίς θεραπευτική δράση)
    private static final String queryNegative =
            "PREFIX bp: <http://www.biopax.org/release/biopax-level2.owl#> SELECT DISTINCT ?protein WHERE { ?protein a bp:protein . FILTER NOT EXISTS { ?interaction a bp:catalysis ; bp:CONTROLLER ?protein } } LIMIT 100";

    // Θετικά παραδείγματα (πρωτεΐνες με θεραπευτική δράση)
    private static final String queryPositive =
            "PREFIX bp: <http://www.biopax.org/release/biopax-level2.owl#> SELECT DISTINCT ?protein WHERE { ?interaction a bp:catalysis ; bp:CONTROLLER ?protein ; bp:CONTROL-TYPE \"ACTIVATION\" . } LIMIT 100";


    public static void main(String[] args) throws Exception {
        setUp();
        run();
    }

    private static void run() throws OWLOntologyCreationException, ComponentInitException {
        logger.debug("Starting...");

        // Εκτέλεση των queries και εισαγωγή των παραδειγμάτων
        logger.debug("Creating positive and negative examples...");
        Set<OWLIndividual> posExamples = executeQueryAndGetIndividuals(queryPositive);
        Set<OWLIndividual> negExamples = executeQueryAndGetIndividuals(queryNegative);
        logger.debug("Finished creating positive and negative examples");

        // Φόρτωση της οντολογίας
        logger.debug("Reading ontology...");
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = man.loadOntologyFromOntologyDocument(new File(kbPathStr));
        logger.debug("Read " + ontology.getAxiomCount() + " axioms");
        logger.debug("Finished reading the ontology");

        // Δημιουργία υλικού για περιορισμούς υπαρξιακών συνθηκών
        ExistentialRestrictionMaterialization mat = new ExistentialRestrictionMaterialization(ontology);
        System.out.println(mat.materialize("http://purl.obolibrary.org/obo/CHEBI_33560"));

        // Αρχικοποίηση της πηγής γνώσης
        logger.debug("Initializing knowledge source...");
        KnowledgeSource ks = new OWLAPIOntology(ontology);
        ks.init();
        logger.debug("Finished initializing knowledge source");

        // Αρχικοποίηση του συλλογιστή
        logger.debug("Initializing reasoner...");
        OWLAPIReasoner baseReasoner = new OWLAPIReasoner(ks);
        baseReasoner.setUseFallbackReasoner(true);
        baseReasoner.init();
        Logger.getLogger(ElkReasoner.class).setLevel(Level.OFF);

        // Χρήση ClosedWorldReasoner για καλύτερη διαχείριση δεδομένων
        ClosedWorldReasoner cwReasoner = new ClosedWorldReasoner(ks);
        cwReasoner.setReasonerComponent(baseReasoner);
        cwReasoner.setHandlePunning(false);
        cwReasoner.setUseMaterializationCaching(false);
        cwReasoner.setMaterializeExistentialRestrictions(true);
        cwReasoner.init();
        logger.debug("Finished initializing reasoner component");

        AbstractReasonerComponent rc = cwReasoner;

        // Αρχικοποίηση του προβλήματος μάθησης
        logger.debug("Initializing learning problem...");
        PosNegLPStandard lp = new PosNegLPStandard(rc);
        lp.setPositiveExamples(posExamples);
        lp.setNegativeExamples(negExamples);
        lp.init();
        logger.debug("Finished initializing learning problem");

        // Αρχικοποίηση του αλγορίθμου μάθησης (CELOE)
        logger.debug("Initializing learning algorithm...");
        AbstractCELA la;

        OEHeuristicRuntime heuristic = new OEHeuristicRuntime();
        heuristic.setExpansionPenaltyFactor(0.1);

        CELOE celoe = new CELOE(lp, rc);
        celoe.setHeuristic(heuristic);
        celoe.setMaxExecutionTimeInSeconds(60 * 60 * 12);  // Μέγιστος χρόνος εκτέλεσης default(60 * 60 * 12)
        celoe.setNoisePercentage(80);  // Ανοχή σε θόρυβο default(80)
        celoe.setMaxNrOfResults(100);  // Μέγιστος αριθμός αποτελεσμάτων
        celoe.setSearchTreeFile("log/drug_discovery.log");  // Αρχείο για την καταγραφή της αναζήτησης
        celoe.setReplaceSearchTree(true);

        la = celoe;
        logger.debug("Finished initializing learning algorithm");

        // Αρχικοποίηση του τελεστή διεύρυνσης
        logger.debug("Initializing operator...");
        RhoDRDown op = new RhoDRDown();
        op.setInstanceBasedDisjoints(true);
        op.setUseNegation(false);
        op.setUseHasValueConstructor(false);
        op.setUseAllConstructor(false);
        op.setReasoner(rc);
        op.setSubHierarchy(rc.getClassHierarchy());
        op.setObjectPropertyHierarchy(rc.getObjectPropertyHierarchy());
        op.setDataPropertyHierarchy(rc.getDatatypePropertyHierarchy());
        op.init();
        logger.debug("Finished initializing operator");

        if (la instanceof CELOE) {
            ((CELOE) la).setOperator(op);
        }

        // Εκκίνηση του αλγορίθμου
        la.init();
        la.start();

        // Εκτύπωση όλων των μεθόδων της κλάσης CELOE
        /*for (Method method : CELOE.class.getDeclaredMethods()) {
            System.out.println("Διαθέσιμες μεθόδους της κλάσης" + method.getName());
        }*/

        // Εξαγωγή Όλων των Δεδομένων
        /*for (OWLAxiom axiom : ontology.getAxioms()) {
            System.out.println("Axiom: " + axiom);
        }*/

        // Επαλήθευση των Αποτελεσμάτων του Reasoner
        /*for (OWLNamedIndividual individual : ontology.getIndividualsInSignature()) {
            System.out.println("Individual: " + individual);
        }*/

        // Εξαγωγή και εμφάνιση των αποτελεσμάτων
        if (la instanceof CELOE) {
            CELOE celoeAlgorithm = (CELOE) la;
            EvaluatedDescription bestDescription = celoeAlgorithm.getCurrentlyBestEvaluatedDescription();
            if (bestDescription != null) {
                System.out.println("Best learned concept: " + bestDescription.getDescription());
                System.out.println("Accuracy: " + bestDescription.getAccuracy());
            }
        } else {
            // Αυτή η μέθοδος επιστρέφει την καλύτερη έκφραση που έχει βρεθεί μέχρι στιγμής.
            EvaluatedDescription bestDescription = la.getCurrentlyBestEvaluatedDescription();
            if (bestDescription != null) {
                System.out.println("Best learned concept: " + bestDescription.getDescription());
                System.out.println("Accuracy: " + bestDescription.getAccuracy());
            }
        }

        // Ανάλυση πρωτεϊνικών αλληλεπιδράσεων
        logger.debug("Analyzing protein interactions...");
        for (OWLAxiom axiom : ontology.getAxioms()) {
            if (axiom instanceof OWLObjectPropertyAssertionAxiom) {
                OWLObjectPropertyAssertionAxiom interactionAxiom = (OWLObjectPropertyAssertionAxiom) axiom;
                System.out.println("Interaction: " + interactionAxiom.getSubject() + " -> " + interactionAxiom.getObject());
            }
        }

        logger.debug("Finished");
    }

    // Ρύθμιση του περιβάλλοντος
    private static void setUp() {
        logger.setLevel(Level.DEBUG);
        Logger.getLogger(AbstractReasonerComponent.class).setLevel(Level.OFF);
        StringRenderer.setRenderer(Rendering.DL_SYNTAX);
    }

    // Μετατροπή URIs σε αντικείμενα OWLIndividual
    private static Set<OWLIndividual> makeIndividuals(List<String> uris) {
        Set<OWLIndividual> individuals = new HashSet<>();
        for (String uri : uris) {
            individuals.add(new OWLNamedIndividualImpl(IRI.create(uri)));
        }
        return individuals;
    }

    // Εκτέλεση SPARQL query και επιστροφή των αποτελεσμάτων ως OWLIndividuals
    private static Set<OWLIndividual> executeQueryAndGetIndividuals(String query) {
        Set<OWLIndividual> individuals = new HashSet<>();
        String endpoint = "http://localhost:3030/bioPax2/sparql";

        try (RDFConnection conn = RDFConnectionFactory.connect(endpoint)) {
            QueryExecution qExec = conn.query(query);
            ResultSet results = qExec.execSelect();

            while (results.hasNext()) {
                QuerySolution solution = results.next();
                String proteinUri = solution.get("protein").asResource().getURI();
                individuals.add(new OWLNamedIndividualImpl(IRI.create(proteinUri)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return individuals;
    }
}