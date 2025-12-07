package academy.model;

import java.time.LocalDate;
import org.jetbrains.annotations.Nullable;

/**
 * Represents log fields in Nginx format
 *
 * <p>Corresponds to the template: {@code $remote_addr - $remote_user [$time_local] “$request” $status $body_bytes_sent
 * “$http_referer” “$http_user_agent”}
 *
 * @param remoteAddress Client IP address
 * @param remoteUser Username for authentication
 * @param timeLocal Request time in local server time
 * @param request HTTP request (method, URI, protocol)
 * @param status HTTP response status
 * @param bodyBytesSent Response body size in bytes
 * @param httpReferer Request source URL
 * @param httpUserAgent Client User-Agent
 */
public record LogFields(
        String remoteAddress,
        String remoteUser,
        LocalDate timeLocal,
        String request,
        int status,
        double bodyBytesSent,
        String httpReferer,
        String httpUserAgent,
        String requestResource,
        @Nullable String requestProtocol) {

    public LogFields(
            String remoteAddress,
            String remoteUser,
            LocalDate timeLocal,
            String request,
            int status,
            double bodyBytesSent,
            String httpReferer,
            String httpUserAgent) {
        this(
                remoteAddress,
                remoteUser,
                timeLocal,
                request,
                status,
                bodyBytesSent,
                httpReferer,
                httpUserAgent,
                getRequestResource(request),
                getRequestProtocol(request));
    }

    private static String getRequestResource(String request) {
        if (request == null || request.isBlank()) {
            return "";
        }
        String[] parts = request.split(" ");
        return parts.length >= 2 ? parts[1] : request;
    }

    private static @Nullable String getRequestProtocol(String request) {
        if (request == null || request.isBlank()) {
            return null;
        }
        String[] parts = request.split(" ");
        return parts.length >= 3 ? parts[2] : null;
    }
}
