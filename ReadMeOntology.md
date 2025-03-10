# Ontology

## Περιγραφή
Πρόκειται για ένα εργαλείο που βοηθάει να εκτελέσουμε SPARQL query σε ενα αρχείο με οντολογίες OWL.

---

## Λειτουργία
Ο κώδικας είναι σχεδιασμένος να λειτουργεί σε έξι βασικά βήματα:

1. **Φόρτωση Οντολογίας**:
    - Διαβάζει δεδομένα από ένα αρχείο OWL οντολογίας.
2. **Κατασκευή query**
    - Για να πάρουμε συγκεκριμένα δεδομένα απο την οντολογία.

---

## Δομή του Κώδικα
- **`main`**: Το σημείο εκκίνησης του προγράμματος. Καλεί τις βασικές λειτουργίες `loadOntology()` και `loadOntologyImplementedReasoner()`.

---

## Επεξήγηση Παραμέτρων
- **`query`**: Φτιάχνεις ενα query μέσα στην class Ontology και το βάζεις `QueryFactory.create()`
- **`loadOntology`**: Ορίζεις μέσα στην συνάρτηση την διαδρομή της οντολογίας.

---

## Πώς να το εκτελέσετε
1. Φορτώστε το αρχείο Maven.
2. Εκτελέστε το πρόγραμμα.

---