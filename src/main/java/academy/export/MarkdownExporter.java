package academy.export;

import academy.stats.Stats;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.markdown.MarkdownRenderer;

public class MarkdownExporter implements ILogsExporter {

    private final MarkdownRenderer renderer;
    private final Parser parser;

    public MarkdownExporter() {
        List<Extension> extensions = List.of(TablesExtension.create());
        renderer = MarkdownRenderer.builder().extensions(extensions).build();
        parser = Parser.builder().extensions(extensions).build();
    }

    @Override
    public void export(String filename, Stats stats) throws IOException {
        Document document = new Document();

        addSection(document, "General Information", createGeneralInfoTable(stats));
        addSection(document, "Requested Resources", createResourcesTable(stats));
        addSection(document, "Response Codes", createResponseCodesTable(stats));

        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(filename))) {
            writer.write(renderer.render(document));
        }
    }

    private void addSection(Document document, String title, String table) {
        Heading heading = new Heading();
        heading.setLevel(4);
        heading.appendChild(new Text(title));
        document.appendChild(heading);

        document.appendChild(parser.parse(table));
        document.appendChild(new Paragraph());
    }

    private String createGeneralInfoTable(Stats stats) {
        return """
            | Metric                       | Value      |%n\
            |:-----------------------------|:-----------|%n\
            | Files                        | %s         |%n\
            | Total Requests               | %d         |%n\
            | Average Response Size        | %s         |%n\
            | 95th Percentile Response Size| %s         |%n\
            | Max Response Size            | %s         |%n\
            """
                .formatted(
                        String.join(", ", stats.getFiles()),
                        stats.getTotalRequestsCount(),
                        formatBytes(stats.getResponseSizeInBytes().average),
                        formatBytes(stats.getResponseSizeInBytes().p95),
                        formatBytes(stats.getResponseSizeInBytes().max));
    }

    private String createResourcesTable(Stats stats) {
        StringBuilder table = new StringBuilder(
                """
                | Resource | Request Count |
                |:---------|:--------------|
                """);

        stats.getResources()
                .forEach(r -> table.append("| %s | %d |%n".formatted(r.resource(), r.totalRequestsCount())));

        return table.toString();
    }

    private String createResponseCodesTable(Stats stats) {
        StringBuilder table = new StringBuilder(
                """
                | Code | Description | Count |
                |:----:|:------------|------:|
                """);

        stats.getResponseCodes()
                .forEach(c -> table.append("| %d | %s | %d |%n"
                        .formatted(c.code(), getHttpStatusDescription(c.code()), c.totalResponsesCount())));

        return table.toString();
    }

    private String formatBytes(double bytes) {
        if (bytes < 1024) {
            return String.format("%.0f B", bytes);
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024);
        } else {
            return String.format("%.2f MB", bytes / (1024 * 1024));
        }
    }

    private String getHttpStatusDescription(int code) {
        return switch (code) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 301 -> "Moved Permanently";
            case 302 -> "Found";
            case 304 -> "Not Modified";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 500 -> "Internal Server Error";
            case 502 -> "Bad Gateway";
            case 503 -> "Service Unavailable";
            case 504 -> "Gateway Timeout";
            default -> "Unknown";
        };
    }
}
