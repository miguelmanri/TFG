package app;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Main {

    public static void main(String[] args) {

        if (args.length < 3) {
            System.out.println("Uso: <rdfFile> <outputFile> <limit> [--s=STAR,BOT,TOP] [--p=type,subclass,definedby,relation]");
            return;
        }

        String rdfFile = args[0];
        String outputFile = args[1];
        int limit = Integer.parseInt(args[2]);

        // Default: todas las estrategias, todos los patrones
        Set<String> strategies = new HashSet<>(Arrays.asList("STAR", "BOT", "TOP"));
        Set<String> patterns = new HashSet<>(Arrays.asList("type", "subclass", "definedby", "relation"));

        for (int i = 3; i < args.length; i++) {

            String arg = args[i];

            if (arg.startsWith("--s=")) {
                strategies = new HashSet<>(
                        Arrays.asList(arg.substring(4).toUpperCase().split(",")));
            } else if (arg.startsWith("--p=")) {
                patterns = new HashSet<>(
                        Arrays.asList(arg.substring(4).toLowerCase().split(",")));
            } else {
                System.out.println(
                        "Warning: argumento desconocido '" + arg + "', se ignora.");
            }
        }

        try {
            Principal principal = new Principal(rdfFile, outputFile, limit, strategies, patterns);
            principal.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}