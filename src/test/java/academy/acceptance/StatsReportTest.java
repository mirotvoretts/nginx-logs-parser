package academy.acceptance;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import academy.Application;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StatsReportTest {
    private File tempLogFile;
    private File tempReportFile;
    private String reportPath;

    @BeforeEach
    void setUp() throws IOException {
        List<String> lines = List.of(
                "127.0.0.1 - - [10/Oct/2024:13:55:36 +0000] \"GET /index.html HTTP/1.1\" 200 100 \"-\" \"Mozilla\"");
        tempLogFile = File.createTempFile("testLogs", ".log");
        Files.write(tempLogFile.toPath(), lines);
    }

    @AfterEach
    void tearDown() {
        if (tempLogFile != null && tempLogFile.exists()) {
            tempLogFile.delete();
        }
        if (tempReportFile != null && tempReportFile.exists()) {
            tempReportFile.delete();
        }
    }

    @Test
    @DisplayName("Should export statistics in JSON format")
    void jsonTest() throws IOException {
        setupReportFile(".json");
        String[] args = {"--path", tempLogFile.getAbsolutePath(), "--format", "json", "--output", reportPath};

        assertDoesNotThrow(() -> Application.execute(args));

        String content = Files.readString(tempReportFile.toPath());

        assertAll(
                "JSON format validation",
                () -> assertTrue(content.trim().startsWith("{")),
                () -> assertTrue(content.trim().endsWith("}")),
                () -> assertTrue(content.contains("\"resources\"") && content.contains("\"files\"")));
    }

    @Test
    @DisplayName("Should export statistics in Markdown format")
    void markdownTest() throws IOException {
        setupReportFile(".md");
        String[] args = {"--path", tempLogFile.getAbsolutePath(), "--format", "markdown", "--output", reportPath};

        assertDoesNotThrow(() -> Application.execute(args));

        String content = Files.readString(tempReportFile.toPath());

        assertAll(
                "Markdown format validation",
                () -> assertTrue(content.contains("####")),
                () -> assertTrue(content.contains("|")),
                () -> assertTrue(content.contains("---")));
    }

    @Test
    @DisplayName("Should export statistics in AsciiDoc format")
    void adocTest() throws IOException {
        setupReportFile(".ad");
        String[] args = {"--path", tempLogFile.getAbsolutePath(), "--format", "adoc", "--output", reportPath};

        assertDoesNotThrow(() -> Application.execute(args));

        String content = Files.readString(tempReportFile.toPath());

        assertTrue(content.contains("----"));
    }

    private void setupReportFile(String extension) throws IOException {
        tempReportFile = File.createTempFile("report", extension);
        reportPath = tempReportFile.getAbsolutePath();
        tempReportFile.delete();
    }
}
