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

class StatsCalculationTest {
    private File tempLogFile;
    private File tempReportFile;
    private String reportPath;

    @BeforeEach
    void setUp() throws IOException {
        tempReportFile = File.createTempFile("stats_report", ".json");
        reportPath = tempReportFile.getAbsolutePath();
        tempReportFile.delete();
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
    @DisplayName("Should correctly calculate statistics from a local log file")
    void happyPathTest() throws IOException {
        List<String> lines = List.of(
                "127.0.0.1 - - [10/Oct/2024:13:55:36 +0000] \"GET /index.html HTTP/1.1\" 200 100 \"-\" \"Mozilla\"",
                "127.0.0.1 - - [10/Oct/2024:13:55:37 +0000] \"GET /index.html HTTP/1.1\" 200 300 \"-\" \"Mozilla\"",
                "127.0.0.1 - - [10/Oct/2024:13:55:38 +0000] \"GET /image.png HTTP/1.1\" 404 50 \"-\" \"Mozilla\"");

        createTempLogFile(lines);
        String[] args = {"--path", tempLogFile.getAbsolutePath(), "--format", "json", "--output", reportPath};

        assertDoesNotThrow(() -> Application.execute(args));

        assertTrue(tempReportFile.exists(), "Report file should be created");

        String jsonContent = Files.readString(tempReportFile.toPath());

        assertAll(
                "Statistics validation",
                () -> assertTrue(jsonContent.contains("3"), "Total requests should be 3"),
                () -> assertTrue(jsonContent.contains("index.html"), "Should contain index.html"),
                () -> assertTrue(jsonContent.contains("image.png"), "Should contain image.png"),
                () -> assertTrue(jsonContent.contains("200"), "Should track 200 OK"),
                () -> assertTrue(jsonContent.contains("404"), "Should track 404 Not Found"),
                () -> assertTrue(jsonContent.contains("150"), "Average response size should be 150"));
    }

    private void createTempLogFile(List<String> lines) throws IOException {
        tempLogFile = File.createTempFile("test_stats", ".log");
        Files.write(tempLogFile.toPath(), lines);
    }
}
