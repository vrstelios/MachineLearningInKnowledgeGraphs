import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;

public class Ontology {

    private static final String query1 = "SELECT DISTINCT ?class WHERE { ?s a ?class . }";

    private static final String query2 =
            "PREFIX bp: <http://www.biopax.org/release/biopax-level3.owl#> " +
                    "SELECT ?reaction ?id WHERE { ?reaction a bp:UnificationXref . ?reaction bp:id ?id . } LIMIT 10";

    private static final String query3 =
            "PREFIX bp: <http://www.biopax.org/release/biopax-level2.owl#> " +
                    "SELECT (COUNT(*) AS ?tripletCount)" +
                    "WHERE { ?s ?p ?o }";

    private static final String queryNegative =
            "PREFIX bp: <http://www.biopax.org/release/biopax-level2.owl#> SELECT DISTINCT ?protein WHERE { ?protein a bp:protein . FILTER NOT EXISTS { ?interaction a bp:catalysis ; bp:CONTROLLER ?protein } } LIMIT 100";


    private static final String queryPositive =
            "PREFIX bp: <http://www.biopax.org/release/biopax-level2.owl#> SELECT DISTINCT ?protein WHERE { ?interaction a bp:catalysis ; bp:CONTROLLER ?protein ; bp:CONTROL-TYPE \"ACTIVATION\" . } LIMIT 100"
            ;

    public static void main(String[] args) {

        //loadOntology("OpenPVSignal/WHO_UMC_Pharmaceuticals_Newsletter_2017_3_Ibrutinib_and_pneumonitis.owl");
        loadOntology("biopax2/Homo_sapiens.owl");
        //test("biopax2/Homo_sapiens.owl");
        //loadOntologyImplementedReasoner("biopax2/Homo_sapiens.owl");
    }

    public static void loadOntology(String path) {
        try {
            // Load ontology
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

            File file = new File(path);
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);

            // Use ByteArrayOutputStream for save the RDF/XML string
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            manager.saveOntology(ontology, new RDFXMLDocumentFormat(), baos);
            String rdfString = baos.toString("UTF-8");

            // Read ontology with jena from RDF String
            Model model = ModelFactory.createDefaultModel();
            model.read(new StringReader(rdfString), null, "RDF/XML");

            // Execute SPARQL query
            Query query = QueryFactory.create(queryNegative);
            QueryExecution qexec = QueryExecutionFactory.create(query, model);

            ResultSet results = qexec.execSelect();
            ResultSetFormatter.out(System.out, results, query);

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadOntologyImplementedReasoner(String path) {
        try {
            // Load ontology
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

            File file = new File(path);
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);

            // Add reasoner Hermit
            OWLReasonerFactory reasonerFactory = new ReasonerFactory();
            OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);

            if (!reasoner.isConsistent()) {
                System.out.println("Ontology is inconsistent!");
                return;
            }

            // New ontology with the conclusions first ontology
            OWLOntology inferredOntology = manager.createOntology();

            // Add new relationship inferred in ontology from reasoner
            for (OWLAxiom axiom : reasoner.getRootOntology().getAxioms()) {
                manager.addAxiom(inferredOntology, axiom);
            }

            // Save and update ontology with new relationship
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            RDFXMLDocumentFormat rdfxmlFormat = new RDFXMLDocumentFormat();
            manager.saveOntology(inferredOntology, rdfxmlFormat, outputStream);

            // Execute SPARQL query
            Model model = ModelFactory.createDefaultModel();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            model.read(inputStream, null, "RDF/XML");

            Query query = QueryFactory.create(query1);
            try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
                ResultSet results = qexec.execSelect();
                ResultSetFormatter.out(System.out, results, query);
            }

        } catch (OWLOntologyCreationException | OWLOntologyStorageException e) {
            e.printStackTrace();
        }
    }

    public static void test(String path) {
        try {
            // Load the ontology
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(path));

            // Create and use the reasoner
            OWLReasonerFactory reasonerFactory = new ReasonerFactory();
            OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);

            boolean consistent = reasoner.isConsistent();
            System.out.println("The ontology is consistent: " + consistent);

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
    }

}

