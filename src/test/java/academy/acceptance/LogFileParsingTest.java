package academy.acceptance;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

class LogFileParsingTest {
    private File tempLogFile;
    private File tempReportFile;
    private String reportPath;

    @BeforeEach
    void setUp() throws IOException {
        tempReportFile = File.createTempFile("report", ".json");
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
    @DisplayName("Should successfully process a valid local log file")
    void localFileProcessingTest() throws IOException {
        List<String> lines = List.of(
                "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 200 100 \"-\" \"Debian APT-HTTP/1.3\"",
                "93.180.71.3 - - [17/May/2015:08:05:23 +0000] \"GET /downloads/product_1 HTTP/1.1\" 200 200 \"-\" \"Debian APT-HTTP/1.3\"",
                "80.91.33.133 - - [17/May/2015:08:05:24 +0000] \"GET /downloads/product_2 HTTP/1.1\" 404 0 \"-\" \"Debian APT-HTTP/1.3\"");
        createTempLogFile(lines);

        String[] args = {"--path", tempLogFile.getAbsolutePath(), "--format", "json", "--output", reportPath};

        assertDoesNotThrow(() -> Application.execute(args));

        String jsonContent = Files.readString(tempReportFile.toPath());

        assertFalse(jsonContent.isEmpty());

        assertTrue(jsonContent.contains("downloads/product_1"));
        assertTrue(jsonContent.contains("downloads/product_2"));
        assertTrue(jsonContent.contains("200"));
        assertTrue(jsonContent.contains("404"));
    }

    @Test
    @DisplayName("Should successfully process a valid remote log file")
    void remoteFileProcessingTest() throws IOException {
        String remoteUrl =
                "https://raw.githubusercontent.com/elastic/examples/master/Common%20Data%20Formats/nginx_logs/nginx_logs";

        String[] args = {"--path", remoteUrl, "--format", "json", "--output", reportPath};

        assertDoesNotThrow(() -> Application.execute(args));

        assertTrue(tempReportFile.exists());
        String jsonContent = Files.readString(tempReportFile.toPath());
        assertFalse(jsonContent.isEmpty());
        assertTrue(jsonContent.contains("200"));
    }

    @Test
    @DisplayName("Should filter log entries by --from and --to date parameters")
    void localFileProcessingAndFilteringTest() throws IOException {
        List<String> lines = List.of(
                "93.180.71.3 - - [10/Aug/2024:08:05:32 +0000] \"GET /old HTTP/1.1\" 200 100 \"-\" \"-\"",
                "93.180.71.3 - - [22/Aug/2024:08:05:32 +0000] \"GET /target HTTP/1.1\" 200 100 \"-\" \"-\"",
                "93.180.71.3 - - [30/Aug/2024:08:05:32 +0000] \"GET /future HTTP/1.1\" 200 100 \"-\" \"-\"");
        createTempLogFile(lines);

        String[] args = {
            "--path", tempLogFile.getAbsolutePath(),
            "--format", "json",
            "--output", reportPath,
            "--from", "2024-08-20",
            "--to", "2024-08-25"
        };

        assertDoesNotThrow(() -> Application.execute(args));

        String jsonContent = Files.readString(tempReportFile.toPath());

        assertTrue(jsonContent.contains("/target"));
        assertFalse(jsonContent.contains("/old"));
        assertFalse(jsonContent.contains("/future"));
    }

    @Test
    @DisplayName("Should skip malformed log entries and process valid ones")
    void damagedLocalFileProcessingTest() throws IOException {
        List<String> lines = List.of(
                "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /valid HTTP/1.1\" 200 100 \"-\" \"-\"",
                "INVALID_GARBAGE_LINE_WITHOUT_STRUCTURE",
                "ANOTHER_INVALID_GARBAGE_LINE_WITHOUT_STRUCTURE_BEBEBEBE",
                "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /valid2 HTTP/1.1\" 404 100 \"-\" \"-\"");
        createTempLogFile(lines);

        String[] args = {"--path", tempLogFile.getAbsolutePath(), "--format", "json", "--output", reportPath};

        assertDoesNotThrow(() -> Application.execute(args));

        String jsonContent = Files.readString(tempReportFile.toPath());

        assertTrue(jsonContent.contains("/valid"));
        assertTrue(jsonContent.contains("/valid2"));
    }

    private void createTempLogFile(List<String> lines) throws IOException {
        tempLogFile = File.createTempFile("test", ".log");
        Files.write(tempLogFile.toPath(), lines);
    }
}
