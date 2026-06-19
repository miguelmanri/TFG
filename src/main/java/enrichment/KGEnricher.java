package enrichment;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.semanticweb.owlapi.model.*;

import java.io.*;
import java.nio.file.Files;

public class KGEnricher {

    public void enrichOntology(
            OWLOntology targetOntology,
            OWLOntology moduleOntology,
            OWLOntologyManager manager) {

        manager.addAxioms(
                targetOntology,
                moduleOntology.getAxioms()
        );

        System.out.println(
                "Añadidos " +
                        moduleOntology.getAxiomCount() +
                        " axiomas.");
    }

    /**
     * Combina el grafo RDF original con la ontologia OWL enriquecida
     * y guarda el resultado como un Turtle
     */
    public void saveEnrichedRDF(
            Model originalModel,
            OWLOntology enrichedOntology,
            OWLOntologyManager manager,
            String outputPath,
            String strategyName) throws Exception {

        // 1. Guardar la ontología owl a un archivo temp como RDF/XML
        File tempFile = Files.createTempFile(
                "enriched_owl_", ".rdf").toFile();

        try (OutputStream out = new FileOutputStream(tempFile)) {
            manager.saveOntology(
                    enrichedOntology,
                    new org.semanticweb.owlapi.formats.RDFXMLDocumentFormat(),
                    out
            );
        }

        // 2. Cargar el temp a un modelo Jena
        Model owlModel = ModelFactory.createDefaultModel();
        try (InputStream in = new FileInputStream(tempFile)) {
            owlModel.read(in, null, "RDF/XML");
        }

        // 3. Mergear grafo original y axiomas extraidos
        Model mergedModel = ModelFactory.createDefaultModel();
        mergedModel.add(originalModel);
        mergedModel.add(owlModel);

        // 4. Guardar modelo mergeado como Turtle
        File outputFile = new File(
                outputPath
                        + "/enriched_"
                        + strategyName.toLowerCase()
                        + ".ttl");

        try (OutputStream out = new FileOutputStream(outputFile)) {
            RDFDataMgr.write(out, mergedModel, RDFFormat.TURTLE);
        }

        System.out.println(
                "Grafo RDF enriquecido guardado en: "
                        + outputFile.getAbsolutePath());

        // 5. Borrar temp
        tempFile.delete();
    }
}