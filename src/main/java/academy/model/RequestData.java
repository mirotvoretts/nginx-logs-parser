package academy.model;

/**
 * Information about requests on a specific day
 *
 * @param date date of request
 * @param weekday day of the week of request
 * @param totalRequestsCount total number of requests
 * @param totalRequestsPercentage distribution of requests by date as a percentage of the total number
 */
public record RequestData(String date, String weekday, int totalRequestsCount, double totalRequestsPercentage) {}
