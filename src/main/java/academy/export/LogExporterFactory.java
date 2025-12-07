package academy.export;

import academy.model.ExportFormat;
import java.util.Arrays;

public class LogExporterFactory {

    public ILogsExporter createLogExporter(String formatString) {
        ExportFormat format = Arrays.stream(ExportFormat.values())
                .filter(e -> e.getName().equalsIgnoreCase(formatString))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown format: " + formatString));

        return switch (format) {
            case JSON -> new JsonExporter();
            case MARKDOWN -> new MarkdownExporter();
            case ADOC -> new AdocExporter();
        };
    }
}
