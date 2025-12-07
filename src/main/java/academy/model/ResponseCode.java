package academy.model;

/**
 * Information about a specific response code
 *
 * @param code status code
 * @param totalResponsesCount total number of such status codes in records
 */
public record ResponseCode(int code, int totalResponsesCount) {}
