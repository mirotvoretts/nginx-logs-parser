package academy.stats;

import java.util.Collections;
import java.util.List;

public class MetricsCalculator {
    private MetricsCalculator() {}

    /**
     * Calculates the percentile using the linear interpolation method.
     *
     * @param values list of values, data for calculating the metric.
     * @param percentile percentile number, can take values from 0 to 100.
     * @return calculated percentile.
     */
    public static double calculatePercentile(List<Double> values, int percentile) {
        if (percentile < 0 || percentile > 100) {
            throw new IllegalArgumentException("Percentile must be between 0 and 100");
        }

        if (values.isEmpty()) {
            return 0;
        }

        Collections.sort(values);

        double rank = percentile / 100.0 * (values.size() - 1);
        int lowerIndex = (int) Math.floor(rank);
        int upperIndex = (int) Math.ceil(rank);

        if (lowerIndex == upperIndex) {
            return values.get(lowerIndex);
        }

        double lowerValue = values.get(lowerIndex);
        double upperValue = values.get(upperIndex);
        double weight = rank - lowerIndex;

        double result = lowerValue + (upperValue - lowerValue) * weight;

        return roundToTwoDecimalPlaces(result);
    }

    public static double calculateAverage(double sumOfValues, int valuesCount) {
        if (valuesCount > 0) {
            return roundToTwoDecimalPlaces(sumOfValues / valuesCount);
        }
        return 0;
    }

    public static double calculatePercent(int count, int total) {
        if (total == 0) return 0.0;
        double result = count * 100.0 / total;
        return roundToTwoDecimalPlaces(result);
    }

    private static double roundToTwoDecimalPlaces(double value) {
        return Math.round(value * 100) / 100.0;
    }
}
