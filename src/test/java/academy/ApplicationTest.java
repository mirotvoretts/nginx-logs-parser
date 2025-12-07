package academy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ApplicationTest {
    private File tempLogFile;
    private File tempReportFile;

    @BeforeEach
    void setUp() throws IOException {
        String logLine =
                "127.0.0.1 - - [17/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 200 334 \"-\" \"Debian APT-HTTP/1.3\"";
        tempLogFile = File.createTempFile("app_test", ".log");
        Files.write(tempLogFile.toPath(), List.of(logLine));

        tempReportFile = File.createTempFile("app_report", ".md");
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
    @DisplayName("Basic program functionality check")
    void happyPathTest() {
        String[] args = {
            "--path", tempLogFile.getAbsolutePath(),
            "--format", "markdown",
            "--output", tempReportFile.getAbsolutePath()
        };

        assertDoesNotThrow(() -> Application.execute(args));
        assertTrue(tempReportFile.exists());
        assertTrue(tempReportFile.length() > 0);
    }
}
