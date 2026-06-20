package rdf;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.util.HashSet;
import java.util.Set;

public class RDFTypeExtractor {

    // Prefijos para identificar clases internas para excluir
    private static final String[] INTERNAL_PREFIXES = {
            "http://rdf.biogateway.eu/",
            "http://www.w3.org/2002/07/owl#",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
            "http://www.w3.org/2000/01/rdf-schema#",
            "http://www.w3.org/2004/02/skos/",
            "https://w3id.org/biolink/",
            "https://w3id.org/linkml/",
            "http://schema.org/",
            "http://semanticscience.org/",
            "http://www.ncbi.nlm.nih.gov/nuccore/",
            "https://www.ncbi.nlm.nih.gov/nuccore/",
            "https://www.ncbi.nlm.nih.gov/assembly/",
            "http://purl.org/dc/terms/"
    };

    // Predicados adicionales
    private static final String[] ADDITIONAL_PREDICATES = {

            // observed in (TXPO) : CL, CLO, UBERON, BTO
            "http://purl.obolibrary.org/obo/TXPO_0003500",

            // in taxon (RO) : NCBITaxon
            "http://purl.obolibrary.org/obo/RO_0002162",

            // is defined by (RDFS) : ECO, MI, OBI, NCIT
            "http://www.w3.org/2000/01/rdf-schema#isDefinedBy"
    };

    public Set<Resource> extractTypes(Model model, Set<String> patterns) {

        Set<Resource> classes = new HashSet<>();

        // ---------------------------------
        // Patron 1: rdf:type
        // ejemplo :juan rdf:type foaf:Person
        // ---------------------------------
        if (patterns.contains("type")) {
            extractFromPredicate(model, RDF.type.getURI(), classes, "rdf:type");
        }

        // ---------------------------------
        // Patron 2: rdfs:subClassOf
        // ejemplo <CRMHS> rdfs:subClassOf <SO_0000727>
        // ---------------------------------
        if (patterns.contains("subclass")) {
            extractFromPredicate(model, RDFS.subClassOf.getURI(), classes, "rdfs:subClassOf");
        }

        // ---------------------------------
        // Patron 3: predicados adicionales
        // ejemplo <TFBS_X> TXPO_0003500 <CL_0000084>
        // ---------------------------------
        if (patterns.contains("relation")) {
            for (String predicateURI : ADDITIONAL_PREDICATES) {
                extractFromPredicate(model, predicateURI, classes, predicateURI);
            }
        }

        return classes;
    }

    private void extractFromPredicate(
            Model model,
            String predicateURI,
            Set<Resource> classes,
            String label) {

        Property predicate = model.createProperty(predicateURI);

        StmtIterator stmts = model.listStatements(null, predicate, (RDFNode) null);

        while (stmts.hasNext()) {
            Statement stmt = stmts.next();
            RDFNode object = stmt.getObject();

            if (object.isURIResource()) {
                Resource cls = object.asResource();
                if (isExternalOntologyClass(cls)) {
                    classes.add(cls);
                    System.out.println("[" + label + "] Clase detectada: " + cls.getURI());
                }
            }
        }
    }

    private boolean isExternalOntologyClass(Resource resource) {
        String uri = resource.getURI();
        if (uri == null) return false;

        for (String prefix : INTERNAL_PREFIXES) {
            if (uri.startsWith(prefix)) {
                return false;
            }
        }
        return true;
    }
}