package app;

public class Main {

    public static void main(String[] args) {

        if (args.length < 3) {
            System.out.println("Uso: <rdfFile> <outputFile> <limit>");
            return;
        }

        String rdfFile = args[0];
        String outputFile = args[1];
        int limit = Integer.parseInt(args[2]);

        try {
            Principal principal = new Principal(rdfFile, outputFile, limit);
            principal.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}