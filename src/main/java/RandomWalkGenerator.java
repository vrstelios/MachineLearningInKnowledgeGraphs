import org.semanticweb.owlapi.model.*;
import java.util.*;

public class RandomWalkGenerator {
    private final int walksPerEntity;
    private final Random random;

    public RandomWalkGenerator(int walksPerEntity) {
        this.walksPerEntity = walksPerEntity;
        this.random = new Random();
    }

    // Επιστρέφει το πλήρες URI μιας οντότητας.
    private String getFullURI(OWLEntity entity) {
        return entity.getIRI().toString();
    }

    // Δημιουργία walks για όλες τις οντότητες (Individuals, Classes)
    public List<List<String>> generateWalks(OWLOntology ontology) {
        List<List<String>> walks = new ArrayList<>();

        // 1. Walks από Individuals.
        Set<OWLNamedIndividual> individuals = ontology.getIndividualsInSignature();
        for (OWLNamedIndividual individual : individuals) {
            for (int i = 0; i < walksPerEntity; i++) {
                List<String> walk = performWalk(individual, ontology, 10);
                if (!walk.isEmpty()) walks.add(walk);
            }
        }

        // 2. Walks από Classes.
        Set<OWLClass> classes = ontology.getClassesInSignature();
        for (OWLClass owlClass : classes) {
            for (int i = 0; i < walksPerEntity; i++) {
                List<String> walk = performWalk(owlClass, ontology, 5);
                if (!walk.isEmpty()) walks.add(walk);
            }
        }

        return walks;
    }

    // Walk για Individuals.
    private List<String> performWalk(OWLNamedIndividual start, OWLOntology ontology, int depth) {
        List<String> walk = new ArrayList<>();
        OWLNamedIndividual current = start;
        walk.add(getFullURI(current));

        for (int d = 0; d < depth; d++) {
            List<OWLObjectPropertyAssertionAxiom> assertions =
                    new ArrayList<>(ontology.getObjectPropertyAssertionAxioms(current));
            if (assertions.isEmpty()) break;

            // Τυχαία επιλογή μιας σχέσης
            OWLObjectPropertyAssertionAxiom chosen = assertions.get(random.nextInt(assertions.size()));

            // Πλήρες URI της ιδιότητας.
            String propertyURI = getFullURI(chosen.getProperty().asOWLObjectProperty());

            // Πλήρες URI του αντικειμένου.
            String objectURI = getFullURI((OWLNamedIndividual) chosen.getObject());

            walk.add(propertyURI);
            walk.add(objectURI);
            current = (OWLNamedIndividual) chosen.getObject();
        }

        return walk;
    }

    // Walk για Classes.
    private List<String> performWalk(OWLClass start, OWLOntology ontology, int depth) {
        List<String> walk = new ArrayList<>();
        walk.add(getFullURI(start));

        OWLClass current = start;
        for (int d = 0; d < depth; d++) {
            Set<OWLSubClassOfAxiom> axioms = ontology.getSubClassAxiomsForSubClass(current);
            if (axioms.isEmpty()) break;

            // Τυχαία επιλογή μιας σχέσης
            OWLSubClassOfAxiom chosen = new ArrayList<>(axioms).get(random.nextInt(axioms.size()));
            OWLClassExpression superClass = chosen.getSuperClass();

            // Αν η superClass είναι named, προσθέτουμε το URI της
            if (!superClass.isAnonymous()) {
                String superClassURI = getFullURI(superClass.asOWLClass());
                walk.add(superClassURI);
                current = superClass.asOWLClass();
            } else {
                break;
            }
        }

        return walk;
    }
}