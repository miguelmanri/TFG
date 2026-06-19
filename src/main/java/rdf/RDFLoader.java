package rdf;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

public class RDFLoader {

    public Model loadModel(String path) throws Exception {

        Lang lang = detectFormat(path);

        Model model = RDFDataMgr.loadModel(path, lang);

        System.out.println("RDF cargado.");
        System.out.println("Formato: " + lang.getName());
        System.out.println("Tripletas: " + model.size());

        return model;
    }

    private Lang detectFormat(String path) {

        String lower = path.toLowerCase();

        if (lower.endsWith(".ttl")) {
            return Lang.TURTLE;
        } else if (lower.endsWith(".nt")) {
            return Lang.NTRIPLES;
        } else if (lower.endsWith(".rdf") || lower.endsWith(".owl")) {
            return Lang.RDFXML;
        } else if (lower.endsWith(".jsonld")) {
            return Lang.JSONLD;
        } else {
            System.out.println("Warning: formato desconocido para " + path + ", default a Turtle.");
            return Lang.TURTLE;
        }
    }
}