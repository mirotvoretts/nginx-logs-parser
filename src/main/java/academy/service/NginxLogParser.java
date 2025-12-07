package academy.service;

import academy.model.LogFields;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NginxLogParser {
    private static final Logger LOGGER = LogManager.getLogger(NginxLogParser.class);

    /**
     * Regular expression matching the pattern: {@code $remote_addr - $remote_user [$time_local] “$request”
     * $status $body_bytes_sent “$http_referer” “$http_user_agent”}
     */
    private static final Pattern PATTERN =
        Pattern.compile("^([\\d.]+) - (\\S+) \\[([^]]+)] \"([^\"]+)\" (\\d{3}) (\\d+) \"([^\"]*)\" \"([^\"]*)\"$");

    private LogFields getLogFields(Matcher matcher) {
        return new LogFields(
            matcher.group(1),
            matcher.group(2),
            parseDate(matcher.group(3)),
            matcher.group(4),
            Integer.parseInt(matcher.group(5)),
            Double.parseDouble(matcher.group(6)),
            matcher.group(7),
            matcher.group(8));
    }

    /**
     * Attempts to parse a line from the log file.
     *
     * @param line A line from the log file.
     * @return Optional with fields according to the template, or empty if parsing according to the template failed.
     */
    public Optional<LogFields> tryProcessLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            LOGGER.warn("Failed to parse line: {}", line);
            return Optional.empty();
        }

        Matcher matcher = PATTERN.matcher(line);
        return matcher.matches() ? Optional.of(getLogFields(matcher)) : Optional.empty();
    }

    private LocalDate parseDate(String timeLocal) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);
            return LocalDateTime.parse(timeLocal, formatter).toLocalDate();
        } catch (Exception e) {
            LOGGER.error("Failed to parse date {} - {}", timeLocal, e.getMessage());
            throw new IllegalArgumentException("Failed to parse date " + timeLocal);
        }
    }
}
