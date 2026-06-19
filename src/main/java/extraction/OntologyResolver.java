package extraction;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OntologyResolver {

    private final Map<String, String> ontologyMap;

    public OntologyResolver() {
        ontologyMap = new HashMap<>();

        // --- Ontologias BioGateway ---

        // Sequence Ontology (SO)
        ontologyMap.put(
                "http://purl.obolibrary.org/obo/SO",
                "ontologies/so.owl"
        );

        // NCI Thesaurus (NCIT)
        ontologyMap.put(
                "http://purl.obolibrary.org/obo/NCIT",
                "ontologies/ncit.owl"
        );

        // Cell Ontology (CL)
        ontologyMap.put(
                "http://purl.obolibrary.org/obo/CL",
                "ontologies/cl.owl"
        );

        // Cell Line Ontology (CLO)
        ontologyMap.put(
                "http://purl.obolibrary.org/obo/CLO",
                "ontologies/clo_merged.owl"
        );

        // UBERON (Uber Anatomy Ontology)
        ontologyMap.put(
                "http://purl.obolibrary.org/obo/UBERON",
                "ontologies/uberon.owl"
        );

        // BRENDA Tissue Ontology (BTO)
        ontologyMap.put(
                "http://purl.obolibrary.org/obo/BTO",
                "ontologies/bto.owl"
        );

        // Evidence & Conclusion Ontology (ECO)
        ontologyMap.put(
                "http://purl.obolibrary.org/obo/ECO",
                "ontologies/eco.owl"
        );

        // Molecular Interactions vocabulary (MI)
        ontologyMap.put(
                "http://purl.obolibrary.org/obo/MI",
                "ontologies/psi-mi.owl"
        );

        // Ontology for Biomedical Investigations (OBI)
        ontologyMap.put(
                "http://purl.obolibrary.org/obo/OBI",
                "ontologies/obi.owl"
        );

        // Basic Formal Ontology (BFO)
        ontologyMap.put(
                "http://purl.obolibrary.org/obo/BFO",
                "ontologies/bfo_classes_only.owl"
        );

        // Genotype Ontology (GENO)
        ontologyMap.put(
                "http://purl.obolibrary.org/obo/GENO",
                "ontologies/geno.owl"
        );

        // Relations Ontology (RO)
        ontologyMap.put(
                "http://purl.obolibrary.org/obo/RO",
                "ontologies/ro.owl"
        );

        // TOXic Process Ontology (TXPO)
        ontologyMap.put(
                "http://purl.obolibrary.org/obo/TXPO",
                "ontologies/txpo.owl"
        );

        // Experimental Factor Ontology (EFO)
        ontologyMap.put(
                "http://www.ebi.ac.uk/efo",
                "ontologies/efo.owl"
        );

        // Feature Annotation Location Description Ontology (FALDO)
        ontologyMap.put(
                "http://biohackathon.org/resource/faldo",
                "ontologies/faldo.owl"
        );

        // NCBITaxon (NCBI Organismal Classification)
        ontologyMap.put(
                "http://purl.obolibrary.org/obo/NCBITaxon",
                "ontologies/ncbitaxon.owl"
        );

        // BioAssay Ontology (BAO)
        ontologyMap.put(
                "http://www.bioassayontology.org/bao",
                "ontologies/bao_complete.owl"
        );
    }

    // Ontologia excluida por limitaciones de memoria
    private static final Set<String> EXCLUDED = Set.of(
            "http://purl.obolibrary.org/obo/NCBITaxon"
    );

    public String resolve(String ontologyURI) {

        // Normalize trailing slash
        String normalized = ontologyURI.endsWith("/")
                ? ontologyURI.substring(0, ontologyURI.length() - 1)
                : ontologyURI;

        if (EXCLUDED.contains(normalized)) {
            System.out.println("Saltamos ontologia excluida: " + normalized);
            return null;
        }

        if (ontologyMap.containsKey(normalized)) {
            return ontologyMap.get(normalized);
        }

        if (ontologyMap.containsKey(normalized + "/")) {
            return ontologyMap.get(normalized + "/");
        }

        return null;
    }

    public boolean isKnown(String ontologyURI) {
        return resolve(ontologyURI) != null;
    }
}