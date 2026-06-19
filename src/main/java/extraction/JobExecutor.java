package extraction;

import org.semanticweb.owlapi.model.OWLOntology;

import java.util.concurrent.*;

public class JobExecutor {

    public OWLOntology executeTask(
            ModuleExtractionTask task,
            int timeoutSeconds)
            throws Exception {

        ExecutorService executor =
                Executors.newSingleThreadExecutor();

        Future<OWLOntology> future =
                executor.submit(task);

        try {
            return future.get(
                    timeoutSeconds,
                    TimeUnit.SECONDS);

        } catch (TimeoutException e) {
            System.out.println(
                    "Extraccion time out despues de "
                            + timeoutSeconds + " segundos."
            );
            return null;

        } finally {
            executor.shutdownNow();
        }
    }
}