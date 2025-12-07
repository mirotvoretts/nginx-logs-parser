package academy;

import academy.export.LogExporterFactory;
import academy.service.ArgumentsValidator;
import academy.service.ExitCode;
import academy.stats.StatsCollector;
import java.io.IOException;
import java.time.LocalDate;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "NGINX Logs Parser", version = "1.0", mixinStandardHelpOptions = true)
public class Application implements Callable<Integer> {
    private static final Logger LOGGER = LogManager.getLogger(Application.class);

    @Option(
            names = {"-p", "--path"},
            required = true)
    private String path;

    @Option(
            names = {"-f", "--format"},
            required = true)
    private String format;

    @Option(
            names = {"-o", "--output"},
            required = true)
    private String output;

    @Option(names = {"--from"})
    private LocalDate from;

    @Option(names = {"--to"})
    private LocalDate to;

    private StatsCollector statsCollector;

    private void validateUserInput() {
        ArgumentsValidator.validateOutput(output);
        ArgumentsValidator.validateFormat(format);
        ArgumentsValidator.validateDates(from, to);
        ArgumentsValidator.validateFormatExtension(format, output);
    }

    private boolean isUrl(String path) {
        return path.startsWith("http");
    }

    private void exportStats() {
        LogExporterFactory fabric = new LogExporterFactory();
        var exporter = fabric.createLogExporter(format);
        try {
            exporter.export(output, statsCollector.getStats());
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to export log stats - " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }

    /** Using only for testing */
    public static int execute(String[] args) {
        Application app = new Application();
        CommandLine cmd = new CommandLine(app);

        try {
            cmd.parseArgs(args);
        } catch (CommandLine.ParameterException e) {
            return ExitCode.INVALID_ARGUMENTS.getCode();
        }

        return app.call();
    }

    @Override
    public Integer call() {
        LOGGER.info("Application started");

        try {
            validateUserInput();
            statsCollector = new StatsCollector(from, to);
            LogsReader reader = new LogsReader(statsCollector);

            if (isUrl(path)) {
                LOGGER.info("Processing remote files");
                reader.processRemoteFiles(path);
            } else {
                LOGGER.info("Processing local files");
                reader.processLocalFiles(path);
            }

            exportStats();
        } catch (IllegalArgumentException e) {
            LOGGER.error("Got invalid arguments - {}", e.getMessage());
            return ExitCode.INVALID_ARGUMENTS.getCode();
        } catch (Exception e) {
            LOGGER.error("Unexpected error - {}", e.getMessage());
            return ExitCode.UNEXPECTED_ERROR.getCode();
        }

        LOGGER.info("Application exit");
        return ExitCode.OK.getCode();
    }
}
