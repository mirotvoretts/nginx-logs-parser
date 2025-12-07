package academy.model;

/**
 * Information about a specific resource
 *
 * @param resource resource name
 * @param totalRequestsCount total number of requests to the resource
 */
public record ResourceData(String resource, int totalRequestsCount) {}
