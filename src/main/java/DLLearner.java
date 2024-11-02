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
public class DLLearner {
    private static final Logger logger = Logger.getLogger(DLLearner.class); //για να καταγράφει μηνύματα (logs) διαφόρων σταδίων της διαδικασίας
    private static final String kbPathStr = "biopax2/Homo_sapiens.owl";
    private static final List<String> posExampleUris = new ArrayList<>(Arrays.asList(
            "http://www.reactome.org/biopax/48887#unificationXref6971",
            "http://www.reactome.org/biopax/48887#unificationXref69710",
            "http://www.reactome.org/biopax/48887#unificationXref69711",
            "http://www.reactome.org/biopax/48887#unificationXref69712",
            "http://www.reactome.org/biopax/48887#unificationXref69713"
    ));
    private static final List<String> negExampleUris = new ArrayList<>(Arrays.asList(
            "http://www.reactome.org/biopax/48887#unificationXref9995",
            "http://www.reactome.org/biopax/48887#unificationXref9996",
            "http://www.reactome.org/biopax/48887#unificationXref9997",
            "http://www.reactome.org/biopax/48887#unificationXref9998",
            "http://www.reactome.org/biopax/48887#unificationXref9999"
    ));

    public static void main(String[] args) throws Exception {
        /*OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = man.loadOntologyFromOntologyDocument(new File(kbPathStr));
        for (OWLNamedIndividual individual : ontology.getIndividualsInSignature()) {
            System.out.println(individual.getIRI());
        }*/
        setUp();
        run();
    }

    private static void run() throws OWLOntologyCreationException, ComponentInitException {
        logger.debug("Starting...");

        logger.debug("creating positive and negative examples...");
        //για να μετατρέψει τα URIs των παραδειγμάτων σε αντικείμενα OWL
        Set<OWLIndividual> posExamples = makeIndividuals(posExampleUris);
        Set<OWLIndividual> negExamples = makeIndividuals(negExampleUris);
        logger.debug("finished creating positive and negative examples");

        logger.debug("reading ontology...");
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = man.loadOntologyFromOntologyDocument(new File(kbPathStr));
        logger.debug("read " + ontology.getAxiomCount() + " axioms");
        logger.debug("finished reading the ontology");

        //δημιουργεί υλικό για περιορισμούς υπαρξιακών συνθηκών.
        ExistentialRestrictionMaterialization mat = new ExistentialRestrictionMaterialization(ontology);
        System.out.println(mat.materialize("http://purl.obolibrary.org/obo/CHEBI_33560"));

        logger.debug("initializing knowledge source...");
        KnowledgeSource ks = new OWLAPIOntology(ontology);
        ks.init();
        logger.debug("finished initializing knowledge source");

        logger.debug("initializing reasoner...");
        OWLAPIReasoner baseReasoner = new OWLAPIReasoner(ks);
        baseReasoner.setUseFallbackReasoner(true);
        baseReasoner.init();
        Logger.getLogger(ElkReasoner.class).setLevel(Level.OFF);

        //Ο λόγος που χρησιμοποιείται είναι η καλύτερη διαχείριση των δεδομένων για την εκμάθηση.
        ClosedWorldReasoner cwReasoner = new ClosedWorldReasoner(ks);
        cwReasoner.setReasonerComponent(baseReasoner);
        cwReasoner.setHandlePunning(false);
        cwReasoner.setUseMaterializationCaching(false);
        cwReasoner.setMaterializeExistentialRestrictions(true);
        cwReasoner.init();
        logger.debug("finished initializing reasoner component");

        AbstractReasonerComponent rc = cwReasoner;

        //αναλαμβάνει να διαμορφώσει το πρόβλημα μάθησης
        logger.debug("initializing learning problem...");
        PosNegLPStandard lp = new PosNegLPStandard(rc);
        lp.setPositiveExamples(posExamples);
        lp.setNegativeExamples(negExamples);
        lp.init();
        logger.debug("finished initializing learning problem");


        logger.debug("initializing learning algorithm...");
        AbstractCELA la;

        OEHeuristicRuntime heuristic = new OEHeuristicRuntime();
        heuristic.setExpansionPenaltyFactor(0.1);

        //πραγματοποιήσει εκμάθηση εννοιών.
        CELOE celoe = new CELOE(lp, rc);
        celoe.setHeuristic(heuristic);
        celoe.setMaxExecutionTimeInSeconds(60*60*12);
        celoe.setNoisePercentage(80);
        celoe.setMaxNrOfResults(50);
        celoe.setSearchTreeFile("log/reactome-minimal.log");
//        celoe.setWriteSearchTree(true);
        celoe.setReplaceSearchTree(true);

//        ELLearningAlgorithm elLa = new ELLearningAlgorithm(lp, rc);
//        elLa.setNoisePercentage(1.0);
//        elLa.setWriteSearchTree(true);
//        elLa.setReplaceSearchTree(true);
//        la = elLa; // celoe;

        la = celoe;
//        Description startClass = new NamedClass("http://dl-learner.org/smallis/Allelic_info");
        logger.debug("finished initializing learning algorithm");
        logger.debug("initializing operator...");
        RhoDRDown op = new RhoDRDown();
        op.setInstanceBasedDisjoints(true);
        op.setUseNegation(false);
//        op.setStartClass(new NamedClass("http://dl-learner.org/smallis/Allelic_info"));
        op.setUseHasValueConstructor(false);
        op.setUseAllConstructor(false);
        op.setReasoner(rc);
        op.setSubHierarchy(rc.getClassHierarchy());
        op.setObjectPropertyHierarchy(rc.getObjectPropertyHierarchy());
        op.setDataPropertyHierarchy(rc.getDatatypePropertyHierarchy());
        op.init();
        logger.debug("finished initializing operator");
        if(la instanceof CELOE)
            ((CELOE) la).setOperator(op);

        la.init();
        la.start();

        logger.debug("Finished");
    }

    private static void setUp() {
        logger.setLevel(Level.DEBUG);
        Logger.getLogger(AbstractReasonerComponent.class).setLevel(Level.OFF);
        StringRenderer.setRenderer(Rendering.DL_SYNTAX);
    }

    private static Set<OWLIndividual> makeIndividuals(List<String> uris) {
        Set<OWLIndividual> individuals = new HashSet<>();
        for (String uri : uris) {
            individuals.add(new OWLNamedIndividualImpl(IRI.create(uri)));
        }

        return individuals;
    }
}