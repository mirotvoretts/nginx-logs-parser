package academy.export;

import academy.stats.Stats;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AdocExporter implements ILogsExporter {
    private void addGeneralInfo(StringBuilder document, Stats stats) {
        document.append("---- General Information ----\n")
                .append("Files: ")
                .append(String.join(", ", stats.getFiles()))
                .append("\n")
                .append("Total Requests: ")
                .append(stats.getTotalRequestsCount())
                .append("\n")
                .append("Average Response Size: ")
                .append(stats.getResponseSizeInBytes().average)
                .append("b\n")
                .append("Max Response Size: ")
                .append(stats.getResponseSizeInBytes().max)
                .append("b\n")
                .append("95th Percentile Size: ")
                .append(stats.getResponseSizeInBytes().p95)
                .append("b\n\n");
    }

    private void addResources(StringBuilder document, Stats stats) {
        if (!stats.getResources().isEmpty()) {
            document.append("---- Requested Resources ----\n");
            stats.getResources().forEach(r -> document.append("- ")
                    .append(r.resource())
                    .append(": ")
                    .append(r.totalRequestsCount())
                    .append(" requests\n"));
            document.append("\n");
        }
    }

    private void addResponseCodes(StringBuilder document, Stats stats) {
        if (!stats.getResponseCodes().isEmpty()) {
            document.append("---- Status Codes ----\n");
            stats.getResponseCodes().forEach(c -> document.append("- ")
                    .append(c.code())
                    .append(": ")
                    .append(c.totalResponsesCount())
                    .append(" responses\n"));
        }
    }

    @Override
    public void export(String filename, Stats stats) throws IOException {
        StringBuilder document = new StringBuilder();

        addGeneralInfo(document, stats);
        addResources(document, stats);
        addResponseCodes(document, stats);

        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(filename))) {
            writer.write(document.toString());
        }
    }
}
