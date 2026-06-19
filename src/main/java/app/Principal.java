package app;

import enrichment.KGEnricher;
import extraction.JobExecutor;
import extraction.ModuleExtractionTask;
import extraction.OntologyResolver;
import metrics.MetricsManager;
import logger.LoggerManager;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.modularity.locality.LocalityClass;
import rdf.OntologyMapper;
import rdf.RDFLoader;
import rdf.RDFTypeExtractor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Principal {

    private static final Map<String, LocalityClass> STRATEGIES =
            new LinkedHashMap<>();

    static {
        STRATEGIES.put("STAR", LocalityClass.STAR);
        STRATEGIES.put("BOT",  LocalityClass.BOTTOM);
        STRATEGIES.put("TOP",  LocalityClass.TOP);
    }

    private final String rdfPath;
    private final String outputPath;
    private final int timeout;

    private final RDFLoader rdfLoader;
    private final RDFTypeExtractor rdfTypeExtractor;
    private final OntologyMapper ontologyMapper;
    private final OntologyResolver ontologyResolver;
    private final JobExecutor jobExecutor;
    private final KGEnricher kgEnricher;
    private final MetricsManager metricsManager;
    private final LoggerManager logger;

    public Principal(
            String rdfPath,
            String outputPath,
            int timeout) throws Exception {

        this.rdfPath = rdfPath;
        this.outputPath = outputPath;
        this.timeout = timeout;

        rdfLoader        = new RDFLoader();
        rdfTypeExtractor = new RDFTypeExtractor();
        ontologyMapper   = new OntologyMapper();
        ontologyResolver = new OntologyResolver();
        jobExecutor      = new JobExecutor();
        kgEnricher       = new KGEnricher();
        metricsManager   = new MetricsManager();
        logger           = new LoggerManager(outputPath);
    }

    public void execute() throws Exception {

        logger.log("--- Enriquecimiento RDF ---");

        // ---------------------------------
        // 1. Carga del RDF
        // ---------------------------------

        logger.log("Cargando RDF...");

        Model originalModel = rdfLoader.loadModel(rdfPath);

        logger.log("Tripletas cargadas: " + originalModel.size());

        // ---------------------------------
        // 2. Extraccion clases rdf:type
        // ---------------------------------

        logger.log("Extrayendo clases rdf:type...");

        Set<Resource> classes =
                rdfTypeExtractor.extractTypes(originalModel);

        logger.log("Clases detectadas: " + classes.size());

        // ---------------------------------
        // 3. Generación mapeo ontologias
        // ---------------------------------

        logger.log("Generado mapeo de ontologias...");

        Map<String, Set<OWLEntity>> mapping =
                ontologyMapper.createMapping(classes);

        logger.log("Ontologias detectadas: " + mapping.size());

        // ---------------------------------
        // 4. Ejecucion de las 3 estrategias
        // ---------------------------------

        for (Map.Entry<String, LocalityClass> strategyEntry
                : STRATEGIES.entrySet()) {

            String strategyName        = strategyEntry.getKey();
            LocalityClass localityClass = strategyEntry.getValue();

            logger.log("\n----------------------------");
            logger.log("Estrategia: " + strategyName);
            logger.log("----------------------------");

            OWLOntologyManager manager =
                    OWLManager.createOWLOntologyManager();

            OWLOntology enrichedOntology =
                    manager.createOntology();

            // ---------------------------------
            // 5. Extraccion modulo por ontologia
            // ---------------------------------

            for (String ontologyIRI : mapping.keySet()) {

                logger.log(
                        "\nProcesando ontologia: "
                                + ontologyIRI);

                Set<OWLEntity> signature =
                        mapping.get(ontologyIRI);

                logger.log(
                        "Tamaño conjunto semilla: "
                                + signature.size());

                ModuleExtractionTask task =
                        new ModuleExtractionTask(
                                ontologyIRI,
                                signature,
                                localityClass,
                                ontologyResolver
                        );

                metricsManager.startTimer();

                OWLOntology module = null;
                try {
                    module = jobExecutor.executeTask(task, timeout);
                } catch (Exception e) {
                    long time = metricsManager.stopTimer();
                    logger.log(
                            "Nos saltamos la ontología: "
                                    + ontologyIRI
                                    + " — motivo: "
                                    + e.getCause() != null
                                    ? e.getCause().getMessage()
                                    : e.getMessage());
                    continue;
                }

                long time = metricsManager.stopTimer();

                if (module == null) {
                    logger.log(
                            "Extraccion time out para: "
                                    + ontologyIRI
                                    + " — pasamos.");
                    continue;
                }

                // ---------------------------------
                // 6. Enriquecer ontologia
                // ---------------------------------

                kgEnricher.enrichOntology(
                        enrichedOntology,
                        module,
                        manager
                );

                // ---------------------------------
                // 7. Log metricas por ontologia
                // ---------------------------------

                logger.log(
                        "Tiempo de ejecucion: " + time + " ms");

                logger.log(
                        "Axiomas extraidos: "
                                + module.getAxiomCount());

                logger.log(
                        "Clases extraidas: "
                                + module.getClassesInSignature()
                                .size());
            }

            // ---------------------------------
            // 8. Guardar grafo RDF enriquecido como Turtle
            // ---------------------------------

            kgEnricher.saveEnrichedRDF(
                    originalModel,
                    enrichedOntology,
                    manager,
                    outputPath,
                    strategyName
            );

            logger.log(
                    "Grafo RDF enriquecido guardado: enriched_"
                            + strategyName.toLowerCase()
                            + ".ttl");
        }

        logger.log("\n--- Enriquecimiento completado ---");
        logger.close();
    }
}