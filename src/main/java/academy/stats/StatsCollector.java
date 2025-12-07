package academy.stats;

import static academy.stats.MetricsCalculator.*;
import static java.lang.Math.max;

import academy.model.LogFields;
import academy.model.RequestData;
import academy.model.ResourceData;
import academy.model.ResponseCode;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Collects statistics by selecting log entries no earlier than {@param from} and no later than {@param to}
 */
public class StatsCollector {
    private static final Logger LOGGER = LogManager.getLogger(StatsCollector.class);

    private final Stats stats = new Stats();

    private double totalResponseSizeInBytes = 0;
    private final List<Double> allResponseSizes = new ArrayList<>();
    private final Map<String, Integer> resourceCounts = new HashMap<>();
    private final Map<Integer, Integer> responseCodeCounts = new HashMap<>();
    private final Map<LocalDate, Integer> requestsPerDate = new HashMap<>();
    private final Set<String> uniqueProtocols = new HashSet<>();

    private final LocalDate from;
    private final LocalDate to;

    /**
     * @param from lower time limit
     * @param to   upper time limit
     */
    public StatsCollector(LocalDate from, LocalDate to) {
        LOGGER.debug("Creating StatsCollector with date range: from={}, to={}", from, to);
        this.from = Objects.requireNonNullElse(from, LocalDate.MIN);
        this.to = Objects.requireNonNullElse(to, LocalDate.MAX);
        LOGGER.info("StatsCollector initialized with effective range: {} to {}", this.from, this.to);
    }

    private void aggregateStats() {
        stats.getResponseSizeInBytes().average =
            calculateAverage(totalResponseSizeInBytes, stats.getTotalRequestsCount());
        stats.getResponseSizeInBytes().p95 = calculatePercentile(allResponseSizes, 95);
        stats.setResources(getTopResources(10));
        stats.setResponseCodes(getResponseCodes());
        stats.setRequestsPerDate(getRequestsPerDate());
        stats.setUniqueProtocols(new ArrayList<>(uniqueProtocols));
    }

    private List<ResourceData> getTopResources(int limit) {
        return resourceCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(limit)
            .map(entry -> new ResourceData(entry.getKey(), entry.getValue()))
            .toList();
    }

    private List<ResponseCode> getResponseCodes() {
        return responseCodeCounts.entrySet().stream()
            .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
            .map(entry -> new ResponseCode(entry.getKey(), entry.getValue()))
            .toList();
    }

    private List<RequestData> getRequestsPerDate() {
        if (requestsPerDate.isEmpty() || stats.getTotalRequestsCount() == 0) {
            return Collections.emptyList();
        }

        return requestsPerDate.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> new RequestData(
                entry.getKey().toString(),
                entry.getKey().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                entry.getValue(),
                calculatePercent(entry.getValue(), stats.getTotalRequestsCount())))
            .toList();
    }

    private void updateProtocol(LogFields fields) {
        String protocol = fields.requestProtocol();
        if (protocol != null) {
            uniqueProtocols.add(protocol);
        }
    }

    private void updateRecoursesCount(LogFields fields) {
        String resource = fields.requestResource();
        resourceCounts.merge(resource, 1, Integer::sum);
    }

    private void updateStatusCodesCount(LogFields fields) {
        int statusCode = fields.status();
        responseCodeCounts.merge(statusCode, 1, Integer::sum);
    }

    private void updateResponsesSize(LogFields fields) {
        double responseSize = fields.bodyBytesSent();
        stats.getResponseSizeInBytes().max = max(stats.getResponseSizeInBytes().max, responseSize);
        totalResponseSizeInBytes += responseSize;
        allResponseSizes.add(responseSize);
    }

    /**
     * Include the log file string in statistics if the recording time falls within the [from, to) range.
     *
     * @param logFields fields obtained from the log file string.
     */
    public void collect(LogFields logFields) {
        if (logFields == null) {
            throw new IllegalArgumentException("logFields cannot be null");
        }

        LocalDate date = logFields.timeLocal();

        if (date == null || date.isAfter(to) || date.isBefore(from)) {
            return;
        }

        requestsPerDate.merge(date, 1, Integer::sum);
        stats.setTotalRequestsCount(stats.getTotalRequestsCount() + 1);
        updateResponsesSize(logFields);
        updateStatusCodesCount(logFields);
        updateRecoursesCount(logFields);
        updateProtocol(logFields);
    }

    public void addFile(String fileName) {
        stats.getFiles().add(fileName);
    }

    public Stats getStats() {
        LOGGER.info("Generating final statistics report");

        aggregateStats();

        LOGGER.info(
            "Final stats: {} requests, {} resources, {} dates, {} protocols",
            stats.getTotalRequestsCount(),
            stats.getResources().size(),
            stats.getRequestsPerDate().size(),
            stats.getUniqueProtocols().size());

        return stats;
    }
}
