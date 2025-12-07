package academy.acceptance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import academy.Application;
import academy.service.ExitCode;
import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ArgumentValidationTest {
    private File tempInputFile;
    private String tempOutputFilePath;
    private String tempInputFilePath;

    @BeforeEach
    void setUp() throws IOException {
        File tempOutputFile = File.createTempFile("output", ".json");
        tempOutputFilePath = tempOutputFile.getAbsolutePath();
        tempOutputFile.delete();

        tempInputFile = File.createTempFile("input", ".txt");
        tempInputFilePath = tempInputFile.getAbsolutePath();
    }

    @AfterEach
    void tearDown() {
        if (tempInputFile != null && tempInputFile.exists()) {
            tempInputFile.delete();
        }
    }

    @Test
    @DisplayName("Should fail when local file does not exist")
    void test1() throws IOException {
        File tempFile = File.createTempFile("nonexistentFile", ".txt");
        tempFile.delete();
        String[] args = {"--path", tempFile.getAbsolutePath(), "--format", "json", "--output", tempOutputFilePath};

        int exitCode = Application.execute(args);
        assertEquals(ExitCode.INVALID_ARGUMENTS.getCode(), exitCode);
    }

    @Test
    @DisplayName("Should fail when remote file does not exist")
    void test2() {
        String[] args = {"--path", "https://adsnasasd.xyz/logs.log", "--format", "json", "--output", tempOutputFilePath
        };

        int exitCode = Application.execute(args);
        assertEquals(ExitCode.INVALID_ARGUMENTS.getCode(), exitCode);
    }

    @ParameterizedTest
    @ValueSource(strings = ".docx")
    @DisplayName("Should fail when input file has unsupported extension")
    void test3(String extension) {
        String[] args = {"--path", "test" + extension, "--format", "json", "--output", tempOutputFilePath};

        int exitCode = Application.execute(args);
        assertEquals(ExitCode.INVALID_ARGUMENTS.getCode(), exitCode);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"2025.01.01 10:30", "today"})
    @DisplayName("Should fail when --from / --to parameters are invalid - {0}")
    void test4(String from) {
        String value = (from == null) ? "" : from;
        String[] args = {
            "--path", tempInputFilePath, "--format", "markdown", "--output", tempOutputFilePath, "--from", value
        };

        int exitCode = Application.execute(args);
        assertEquals(ExitCode.INVALID_ARGUMENTS.getCode(), exitCode);
    }

    @ParameterizedTest
    @ValueSource(strings = "txt")
    @DisplayName("Should fail when output format is unsupported: {0}")
    void test5(String format) {
        String[] args = {"--path", tempInputFilePath, "--format", format, "--output", tempOutputFilePath};

        int exitCode = Application.execute(args);
        assertEquals(ExitCode.INVALID_ARGUMENTS.getCode(), exitCode);
    }

    @ParameterizedTest
    @MethodSource("test6ArgumentsSource")
    @DisplayName("Should fail when --output file has incorrect extension for the specified format")
    void test6(String format, String output) {
        String[] args = {"--path", tempInputFilePath, "--format", format, "--output", output};

        int exitCode = Application.execute(args);
        assertEquals(ExitCode.INVALID_ARGUMENTS.getCode(), exitCode);
    }

    @Test
    @DisplayName("Should fail when --output file already exists")
    void test7() throws IOException {
        File existingFile = File.createTempFile("test_exist", ".json");
        existingFile.deleteOnExit();
        String[] args = {"--path", tempInputFilePath, "--format", "json", "--output", existingFile.getAbsolutePath()};

        int exitCode = Application.execute(args);
        assertEquals(ExitCode.INVALID_ARGUMENTS.getCode(), exitCode);
    }

    @ParameterizedTest
    @ValueSource(strings = {"--path", "--output", "--format", "-p", "-o", "-f"})
    @DisplayName("Should fail when required parameter is missing: \"{0}\"")
    void test8(String argument) {
        String[] args = {argument};

        int exitCode = Application.execute(args);
        assertEquals(ExitCode.INVALID_ARGUMENTS.getCode(), exitCode);
    }

    @ParameterizedTest
    @ValueSource(strings = {"--input", "--filter"})
    @DisplayName("Should fail when unsupported parameter is provided: \"{0}\"")
    void test9(String argument) {
        String[] args = {
            "--path", tempInputFilePath, "--format", "json", "--output", tempOutputFilePath, argument, "val"
        };

        int exitCode = Application.execute(args);
        assertEquals(ExitCode.INVALID_ARGUMENTS.getCode(), exitCode);
    }

    @Test
    @DisplayName("Should fail when --from is greater than --to")
    void test10() {
        String[] args = {
            "--path",
            tempInputFilePath,
            "--format",
            "json",
            "--output",
            tempOutputFilePath,
            "--from",
            "2024-02-01",
            "--to",
            "2024-01-01"
        };

        int exitCode = Application.execute(args);
        assertEquals(ExitCode.INVALID_ARGUMENTS.getCode(), exitCode);
    }

    @Test
    @DisplayName("Should succeed with valid parameters")
    void test11() throws IOException {
        File logFile = File.createTempFile("test_log", ".txt");
        logFile.deleteOnExit();
        java.nio.file.Files.write(
                logFile.toPath(),
                "127.0.0.1 - - [01/Jan/2024:00:00:00 +0000] \"GET /test HTTP/1.1\" 200 123".getBytes());

        File outputFile = File.createTempFile("output", ".json");
        outputFile.delete();

        String[] args = {
            "--path", logFile.getAbsolutePath(),
            "--format", "json",
            "--output", outputFile.getAbsolutePath()
        };

        int exitCode = Application.execute(args);
        assertEquals(ExitCode.OK.getCode(), exitCode);

        assertEquals(true, outputFile.exists());
        outputFile.delete();
    }

    private static Stream<Arguments> test6ArgumentsSource() {
        return Stream.of(
                Arguments.of("markdown", "./results.txt"),
                Arguments.of("json", "./results.md"),
                Arguments.of("adoc", "./results.ad1"));
    }
}
