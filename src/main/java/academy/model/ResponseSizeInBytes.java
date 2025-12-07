package academy.model;

/**
 * Statistics on server response sizes in bytes
 *
 * <p>Contains the following metrics:
 *
 * <ul>
 *   <li><b>average</b> - average response size
 *   <li><b>max</b> - maximum response size
 *   <li><b>p95</b> - 95th percentile of response size
 * </ul>
 */
public class ResponseSizeInBytes {
    public double average;
    public double max;
    public double p95;
}
