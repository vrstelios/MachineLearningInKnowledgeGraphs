# Machine Learning In Knowledge Graphs

## Περιγραφή

---

## Prerequisites
- **Java** 17 ή νεότερη έκδοση.
- **Maven** (για τη διαχείριση των εξαρτήσεων και την εκτέλεση του project).
- **Apache Jena Fuseki** Για να κάνεις τα SPARQL query στα δεδομένα σού.
---

## Data
Το project χρησιμοποιεί τα παρακάτω αρχεία δεδομένων:
- **Biopax2**: Περιλαμβάνει δεδομένα από το αρχείο `Homo_sapiens.owl`.
- **OpenPVSignal**

---

## Libraries
Το project βασίζεται στις παρακάτω βιβλιοθήκες:
- **DL-Learner**: Βιβλιοθήκη που υποστηρίζει την εκμάθηση εννοιών σε δεδομένα OWL.
- **Jena**: Για την επεξεργασία και ανάλυση RDF/OWL δεδομένων.
- **Hermit Reasoner**: Για λογική συλλογιστική και έλεγχο συνέπειας.
- **Logback**: Για καταγραφή (logging).

---

## Περιέχει δυο εκτελέσιμα αρχεία
- **Ontology**: Για να μπορούμε να κάνουμε query και να παίρνουμε συγκεκριμένα δεδομένα απο την Οντολογία μας.
- **DLLearner**: Για να μπορούμε να εντοπίζουμε λογικές εκφράσεις που περιγράφουν τις έννοιες που αντιπροσωπεύουν
τα θετικά παραδείγματα και να κάνουμε εκμάθησης εννοιών για την συγκεκριμένη οντολογία που φορτώνουμε στο DLLearner.


---

## Εγκατάσταση
Run: `mvn clean install`
Download: `Apache jena fuseki`
  Run: `cd C:\path\apache-jena-fuseki-5.3.0` and `fuseki-server --update --mem /dataset` 
  Πήγαινε στο `http://localhost:3030/dataset/sparql` και πρόσθεσε το `BioPax2/Homo_sapiens.owl`

---






