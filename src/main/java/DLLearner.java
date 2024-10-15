import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.reasoning.ClosedWorldReasoner;

public class DLLearner {
    public static void main(String[] args) {
        try {
            ClosedWorldReasoner reasoner = new ClosedWorldReasoner();

            KnowledgeSource knowledgeSource = new KnowledgeSource() {
                @Override
                public void init() throws ComponentInitException {}

                @Override
                public String toString() {
                    return "Dummy Knowledge Source";
                }
            };

            knowledgeSource.init();

            System.out.println(knowledgeSource.toString());
            System.out.println("Hello World from DL Learner!");

        } catch (ComponentInitException e) {
            System.err.println("Failed to initialize component: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}