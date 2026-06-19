package extraction;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.modularity.locality.SyntacticLocalityModuleExtractor;
import org.semanticweb.owlapi.modularity.locality.LocalityClass;

import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

public class ModuleExtractionTask
        implements Callable<OWLOntology> {

    private final String ontologyURI;
    private final Set<OWLEntity> seedSignature;
    private final LocalityClass localityClass;
    private final OntologyResolver resolver;

    public ModuleExtractionTask(
            String ontologyURI,
            Set<OWLEntity> seedSignature,
            LocalityClass localityClass,
            OntologyResolver resolver) {

        this.ontologyURI = ontologyURI;
        this.seedSignature = seedSignature;
        this.localityClass = localityClass;
        this.resolver = resolver;
    }

    @Override
    public OWLOntology call() throws Exception {

        String resolvedURL = resolver.resolve(ontologyURI);

        if (resolvedURL == null) {
            throw new IllegalArgumentException(
                    "No encontrado mapeo de ontologia para: " + ontologyURI
            );
        }

        InputStream is = getClass()
                .getClassLoader()
                .getResourceAsStream(resolvedURL);

        if (is == null) {
            throw new RuntimeException(
                    "Ontologia no encontrada: " + resolvedURL
            );
        }

        OWLOntologyManager manager =
                OWLManager.createOWLOntologyManager();

        OWLOntology ontology =
                manager.loadOntologyFromOntologyDocument(is);

        SyntacticLocalityModuleExtractor extractor =
                new SyntacticLocalityModuleExtractor(
                        localityClass,
                        ontology.axioms()
                );

        Stream<OWLAxiom> moduleAxioms =
                extractor.extract(seedSignature.stream());

        return manager.createOntology(moduleAxioms);
    }
}