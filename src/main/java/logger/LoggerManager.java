package logger;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

public class LoggerManager {

    private final PrintWriter writer;

    public LoggerManager(String outputPath)
            throws Exception {

        File dir = new File(outputPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        writer = new PrintWriter(
                new FileWriter(
                        outputPath + "/execution_log.txt",
                        true
                )
        );
    }

    public void log(String message) {

        System.out.println(message);

        writer.println(message);

        writer.flush();
    }

    public void close() {

        writer.close();
    }
}