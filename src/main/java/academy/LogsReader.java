package academy;

import academy.service.ArgumentsValidator;
import academy.service.NginxLogParser;
import academy.service.RemoteLogReader;
import academy.stats.StatsCollector;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogsReader {
    private static final Logger LOGGER = LogManager.getLogger(LogsReader.class);

    private final NginxLogParser parser;
    private final StatsCollector statsCollector;

    public LogsReader(StatsCollector statsCollector) {
        this.statsCollector = statsCollector;
        parser = new NginxLogParser();
    }

    /**
     * Processes deleted log files with lazy reading.
     *
     * @param path path to the deleted resource.
     * @throws IllegalArgumentException any caught error is thrown as {@link IllegalArgumentException},
     *                                  including for testing convenience.
     */
    public void processRemoteFiles(String path) {
        RemoteLogReader reader = new RemoteLogReader();
        statsCollector.addFile(path);

        try (Stream<String> lines = reader.getStreamOfData(path)) {
            lines.forEach(line -> parser.tryProcessLine(line).ifPresent(statsCollector::collect));

        } catch (Exception e) {
            LOGGER.error("Failed to read remote resource {} because - {}", path, e.getMessage());
            throw new IllegalArgumentException("Failed to read remote resource: " + path, e);
        }
    }

    /**
     * Processes the input stream line by line using {@link Scanner}, allowing only one line to be stored in memory at
     * each step.
     *
     * @param path path/glob pattern to the file(s)
     * @throws IllegalArgumentException any caught error is thrown as {@link IllegalArgumentException},
     *                                  including for testing convenience
     */
    public void processLocalFiles(String path) {
        var inputFiles = ArgumentsValidator.getValidFiles(path);

        for (File inputFile : inputFiles) {
            statsCollector.addFile(inputFile.getName());

            try (var inputStream = Files.newInputStream(inputFile.toPath());
                 Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {

                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    parser.tryProcessLine(line).ifPresent(statsCollector::collect);
                }

                if (scanner.ioException() != null) {
                    throw scanner.ioException();
                }
            } catch (Exception e) {
                LOGGER.error("Failed to read all files - {}", e.getMessage());
                throw new IllegalArgumentException("Failed to read local files: " + path, e);
            }
        }
    }
}
