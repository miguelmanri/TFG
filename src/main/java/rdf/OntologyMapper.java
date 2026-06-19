package rdf;

import org.apache.jena.rdf.model.Resource;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.util.*;

public class OntologyMapper {

    private static final String OBO_PREFIX =
            "http://purl.obolibrary.org/obo/";

    public Map<String, Set<OWLEntity>>
    createMapping(Set<Resource> resources) {

        Map<String, Set<OWLEntity>> mapping = new HashMap<>();

        OWLDataFactory factory = OWLManager.getOWLDataFactory();

        for (Resource resource : resources) {

            String uri = resource.getURI();
            if (uri == null) continue;

            String ontologyIRI = extractOntologyIRI(uri);

            if (ontologyIRI == null) {
                System.out.println("Warning: no se puede extraer ontologia para: " + uri);
                continue;
            }

            OWLClass owlClass = factory.getOWLClass(IRI.create(uri));

            mapping.computeIfAbsent(ontologyIRI, k -> new HashSet<>()).add(owlClass);

            System.out.println("Mapeada " + uri + " : " + ontologyIRI);
        }

        return mapping;
    }

    private String extractOntologyIRI(String classURI) {

        // Patron 1: OBO Foundry URIs
        if (classURI.startsWith(OBO_PREFIX)) {
            String localPart = classURI.substring(OBO_PREFIX.length());
            int underscoreIndex = localPart.indexOf('_');
            if (underscoreIndex > 0) {
                String ontologyPrefix =
                        localPart.substring(0, underscoreIndex);
                return OBO_PREFIX + ontologyPrefix;
            }
        }

        // Patron 2: URIs con #
        // ejemplo http://xmlns.com/foaf/0.1/Person -> http://xmlns.com/foaf/0.1/
        int hashIndex = classURI.lastIndexOf('#');
        if (hashIndex > 0) {
            return classURI.substring(0, hashIndex);
        }

        // Patron 3: URIs con /
        // ejemplo http://dbpedia.org/ontology/City -> http://dbpedia.org/ontology
        int slashIndex = classURI.lastIndexOf('/');
        if (slashIndex > 0) {
            return classURI.substring(0, slashIndex);
        }

        return null;
    }
}