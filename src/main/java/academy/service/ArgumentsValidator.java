package academy.service;

import static academy.model.ImportFormat.LOG;
import static academy.model.ImportFormat.TEXT;

import academy.model.ExportFormat;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;

public class ArgumentsValidator {
    private static final GlobFileSearcher searcher = new GlobFileSearcher();

    private ArgumentsValidator() {}

    private static boolean isLogFileExtensionsCorrect(List<File> logFiles) {
        return logFiles.stream()
                .map(File::getName)
                .map(String::toLowerCase)
                .allMatch(logFile -> logFile.endsWith(TEXT.getExtension()) || logFile.endsWith(LOG.getExtension()));
    }

    public static void validateOutput(String pathToOutputFile) {
        Path outputPath = Path.of(pathToOutputFile);

        if (Files.exists(outputPath)) {
            throw new IllegalArgumentException("Output file already exists: " + pathToOutputFile);
        }

        Path parentDirectory = outputPath.getParent();

        if (parentDirectory != null && !Files.exists(parentDirectory)) {
            throw new IllegalArgumentException("Parent directory does not exist: " + parentDirectory);
        }

        Path absoluteParentDirectory = (parentDirectory != null)
                ? parentDirectory
                : outputPath.toAbsolutePath().getParent();
        if (absoluteParentDirectory != null && !Files.isWritable(absoluteParentDirectory)) {
            throw new IllegalArgumentException("No write permission in directory: " + absoluteParentDirectory);
        }
    }

    public static List<File> getValidFiles(String pathToInputFile) {
        var files = searcher.searchFiles(pathToInputFile);

        if (files.isEmpty() || !isLogFileExtensionsCorrect(files)) {
            throw new IllegalArgumentException("Invalid path: " + pathToInputFile);
        }

        return files;
    }

    public static void validateFormat(String exportFormatName) {
        if (EnumSet.allOf(ExportFormat.class).stream()
                .map(ExportFormat::getName)
                .noneMatch(format -> format.equals(exportFormatName))) {
            throw new IllegalArgumentException("Invalid format: " + exportFormatName);
        }
    }

    public static void validateDates(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            return;
        }

        if (!from.isBefore(to)) {
            throw new IllegalArgumentException("From date (" + from + ") must be strictly before to date (" + to + ")");
        }
    }

    public static void validateFormatExtension(String formatInput, String output) {
        EnumSet.allOf(ExportFormat.class).forEach(exportFormat -> {
            String formatName = exportFormat.getName();
            String extension = exportFormat.getExtension();

            if (formatName.equalsIgnoreCase(formatInput) && !output.endsWith(extension)) {
                throw new IllegalArgumentException(
                        "Only %s files are supported for %s output format".formatted(extension, formatInput));
            }
        });
    }
}
