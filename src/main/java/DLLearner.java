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
import java.util.*;
import java.lang.reflect.Method;

public class DLLearner {
    private static final Logger logger = Logger.getLogger(DLLearner.class); // Για καταγραφή μηνυμάτων (logs)
    private static final String kbPathStr = "biopax2/Homo_sapiens.owl";
    // Θετικά παραδείγματα (φάρμακα ή πρωτεΐνες με θεραπευτική δράση)
    private static final List<String> posExampleUris = new ArrayList<>(Arrays.asList(
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant46853",
            "http://www.reactome.org/biopax/48887#sequenceParticipant35130",
            "http://www.reactome.org/biopax/48887#sequenceParticipant27340",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant5344",
            "http://www.reactome.org/biopax/48887#sequenceParticipant18737",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant49793",
            "http://www.reactome.org/biopax/48887#sequenceParticipant27819",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant43794",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant36171",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant18577",
            "http://www.reactome.org/biopax/48887#sequenceParticipant18231",
            "http://www.reactome.org/biopax/48887#sequenceParticipant29088",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant11587",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant49020",
            "http://www.reactome.org/biopax/48887#sequenceParticipant27604",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant36481",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant46034",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant11098",
            "http://www.reactome.org/biopax/48887#sequenceParticipant25885",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant50809",
            "http://www.reactome.org/biopax/48887#sequenceParticipant27478",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant32335",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant41519",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant21521",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant47154",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant5890",
            "http://www.reactome.org/biopax/48887#sequenceParticipant33025",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant29513",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant55750",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant16364",
            "http://www.reactome.org/biopax/48887#sequenceParticipant30367",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant24213",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant2189",
            "http://www.reactome.org/biopax/48887#sequenceParticipant28901",
            "http://www.reactome.org/biopax/48887#sequenceParticipant22188",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant23732",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant35633",
            "http://www.reactome.org/biopax/48887#sequenceParticipant5902",
            "http://www.reactome.org/biopax/48887#sequenceParticipant27435",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant55130",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant53353",
            "http://www.reactome.org/biopax/48887#sequenceParticipant28950",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant51802",
            "http://www.reactome.org/biopax/48887#sequenceParticipant23140",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant3119",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant43769",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant16660",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant39337",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant8368",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant25161",
            "http://www.reactome.org/biopax/48887#sequenceParticipant1523",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant11980",
            "http://www.reactome.org/biopax/48887#sequenceParticipant1435",
            "http://www.reactome.org/biopax/48887#sequenceParticipant17138",
            "http://www.reactome.org/biopax/48887#sequenceParticipant23456",
            "http://www.reactome.org/biopax/48887#sequenceParticipant16419",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant42027",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant32665",
            "http://www.reactome.org/biopax/48887#sequenceParticipant26313",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant12932",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant15800",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant10039",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant53055",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant55975",
            "http://www.reactome.org/biopax/48887#sequenceParticipant27344",
            "http://www.reactome.org/biopax/48887#sequenceParticipant28708",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant14944",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant33112",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant30552",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant19529",
            "http://www.reactome.org/biopax/48887#sequenceParticipant28645",
            "http://www.reactome.org/biopax/48887#sequenceParticipant27988",
            "http://www.reactome.org/biopax/48887#sequenceParticipant18516",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant48251",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant13965",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant47688",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant35939",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant27919",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant55115",
            "http://www.reactome.org/biopax/48887#sequenceParticipant27611",
            "http://www.reactome.org/biopax/48887#sequenceParticipant8921",
            "http://www.reactome.org/biopax/48887#sequenceParticipant27684",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant16961",
            "http://www.reactome.org/biopax/48887#sequenceParticipant7448",
            "http://www.reactome.org/biopax/48887#sequenceParticipant26512",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant37471",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant54142",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant51577",
            "http://www.reactome.org/biopax/48887#sequenceParticipant29175",
            "http://www.reactome.org/biopax/48887#sequenceParticipant18286",
            "http://www.reactome.org/biopax/48887#sequenceParticipant31945",
            "http://www.reactome.org/biopax/48887#sequenceParticipant27199",
            "http://www.reactome.org/biopax/48887#sequenceParticipant17134",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant55802",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant40835",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant42585",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant36366",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant10826",
            "http://www.reactome.org/biopax/48887#physicalEntityParticipant5930",
            "http://www.reactome.org/biopax/48887#sequenceParticipant1611"
    ));
    // Αρνητικά παραδείγματα (φάρμακα ή πρωτεΐνες χωρίς θεραπευτική δράση)
    private static final List<String> negExampleUris = new ArrayList<>(Arrays.asList(
            "http://www.reactome.org/biopax/48887#protein7672",
            "http://www.reactome.org/biopax/48887#protein4276",
            "http://www.reactome.org/biopax/48887#protein7148",
            "http://www.reactome.org/biopax/48887#protein2625",
            "http://www.reactome.org/biopax/48887#protein3991",
            "http://www.reactome.org/biopax/48887#protein1823",
            "http://www.reactome.org/biopax/48887#protein3492",
            "http://www.reactome.org/biopax/48887#protein1301",
            "http://www.reactome.org/biopax/48887#protein4788",
            "http://www.reactome.org/biopax/48887#protein627",
            "http://www.reactome.org/biopax/48887#protein8117",
            "http://www.reactome.org/biopax/48887#protein2485",
            "http://www.reactome.org/biopax/48887#protein5182",
            "http://www.reactome.org/biopax/48887#protein5443",
            "http://www.reactome.org/biopax/48887#protein1996",
            "http://www.reactome.org/biopax/48887#protein8815",
            "http://www.reactome.org/biopax/48887#protein8562",
            "http://www.reactome.org/biopax/48887#protein6268",
            "http://www.reactome.org/biopax/48887#protein4686",
            "http://www.reactome.org/biopax/48887#protein6411",
            "http://www.reactome.org/biopax/48887#protein1382",
            "http://www.reactome.org/biopax/48887#protein5480",
            "http://www.reactome.org/biopax/48887#protein9342",
            "http://www.reactome.org/biopax/48887#protein5654",
            "http://www.reactome.org/biopax/48887#protein4879",
            "http://www.reactome.org/biopax/48887#protein6865",
            "http://www.reactome.org/biopax/48887#protein8629",
            "http://www.reactome.org/biopax/48887#protein2577",
            "http://www.reactome.org/biopax/48887#protein4461",
            "http://www.reactome.org/biopax/48887#protein6225",
            "http://www.reactome.org/biopax/48887#protein8392",
            "http://www.reactome.org/biopax/48887#protein7833",
            "http://www.reactome.org/biopax/48887#protein5938",
            "http://www.reactome.org/biopax/48887#protein2042",
            "http://www.reactome.org/biopax/48887#protein6890",
            "http://www.reactome.org/biopax/48887#protein855",
            "http://www.reactome.org/biopax/48887#protein4564",
            "http://www.reactome.org/biopax/48887#protein8654",
            "http://www.reactome.org/biopax/48887#protein9108",
            "http://www.reactome.org/biopax/48887#protein5325",
            "http://www.reactome.org/biopax/48887#protein2802",
            "http://www.reactome.org/biopax/48887#protein2018",
            "http://www.reactome.org/biopax/48887#protein7381",
            "http://www.reactome.org/biopax/48887#protein3010",
            "http://www.reactome.org/biopax/48887#protein6957",
            "http://www.reactome.org/biopax/48887#protein5848",
            "http://www.reactome.org/biopax/48887#protein578",
            "http://www.reactome.org/biopax/48887#protein435",
            "http://www.reactome.org/biopax/48887#protein3942",
            "http://www.reactome.org/biopax/48887#protein3261",
            "http://www.reactome.org/biopax/48887#protein7925",
            "http://www.reactome.org/biopax/48887#protein2975",
            "http://www.reactome.org/biopax/48887#protein1866",
            "http://www.reactome.org/biopax/48887#protein3182",
            "http://www.reactome.org/biopax/48887#protein4204",
            "http://www.reactome.org/biopax/48887#protein2109",
            "http://www.reactome.org/biopax/48887#protein2210",
            "http://www.reactome.org/biopax/48887#protein4121",
            "http://www.reactome.org/biopax/48887#protein8882",
            "http://www.reactome.org/biopax/48887#protein6429",
            "http://www.reactome.org/biopax/48887#protein1388",
            "http://www.reactome.org/biopax/48887#protein2698",
            "http://www.reactome.org/biopax/48887#protein8097",
            "http://www.reactome.org/biopax/48887#protein5486",
            "http://www.reactome.org/biopax/48887#protein500",
            "http://www.reactome.org/biopax/48887#protein3092",
            "http://www.reactome.org/biopax/48887#protein7204",
            "http://www.reactome.org/biopax/48887#protein8858",
            "http://www.reactome.org/biopax/48887#protein7562",
            "http://www.reactome.org/biopax/48887#protein6454",
            "http://www.reactome.org/biopax/48887#protein9385",
            "http://www.reactome.org/biopax/48887#protein5181",
            "http://www.reactome.org/biopax/48887#protein5510",
            "http://www.reactome.org/biopax/48887#protein7899",
            "http://www.reactome.org/biopax/48887#protein8161",
            "http://www.reactome.org/biopax/48887#protein7285",
            "http://www.reactome.org/biopax/48887#protein6177",
            "http://www.reactome.org/biopax/48887#protein7376",
            "http://www.reactome.org/biopax/48887#protein4741",
            "http://www.reactome.org/biopax/48887#protein2371",
            "http://www.reactome.org/biopax/48887#protein8949",
            "http://www.reactome.org/biopax/48887#protein8344",
            "http://www.reactome.org/biopax/48887#protein3848",
            "http://www.reactome.org/biopax/48887#protein4468",
            "http://www.reactome.org/biopax/48887#protein1164",
            "http://www.reactome.org/biopax/48887#protein3894",
            "http://www.reactome.org/biopax/48887#protein764",
            "http://www.reactome.org/biopax/48887#protein8254",
            "http://www.reactome.org/biopax/48887#protein7468",
            "http://www.reactome.org/biopax/48887#protein4923",
            "http://www.reactome.org/biopax/48887#protein3119",
            "http://www.reactome.org/biopax/48887#protein2216",
            "http://www.reactome.org/biopax/48887#protein4139",
            "http://www.reactome.org/biopax/48887#protein2883",
            "http://www.reactome.org/biopax/48887#protein1763",
            "http://www.reactome.org/biopax/48887#protein1620",
            "http://www.reactome.org/biopax/48887#protein8436",
            "http://www.reactome.org/biopax/48887#protein5845",
            "http://www.reactome.org/biopax/48887#protein5107",
            "http://www.reactome.org/biopax/48887#protein3851",
            "http://www.reactome.org/biopax/48887#protein5882"
    ));

    public static void main(String[] args) throws Exception {
        setUp();
        run();
    }

    private static void run() throws OWLOntologyCreationException, ComponentInitException {
        logger.debug("Starting...");

        // Μετατροπή των URIs των παραδειγμάτων σε αντικείμενα OWL
        logger.debug("Creating positive and negative examples...");
        Set<OWLIndividual> posExamples = makeIndividuals(posExampleUris);
        Set<OWLIndividual> negExamples = makeIndividuals(negExampleUris);
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
        celoe.setMaxExecutionTimeInSeconds(60 * 60);  // Μέγιστος χρόνος εκτέλεσης default(60 * 60 * 12)
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
        for (Method method : CELOE.class.getDeclaredMethods()) {
            System.out.println("Διαθέσιμες μεθόδους της κλάσης" + method.getName());
        }

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
}