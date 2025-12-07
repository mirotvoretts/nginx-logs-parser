package academy.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Just a wrapper over the HTTP client to get the input stream from a remote resource */
public class RemoteLogReader {
    private static final Logger LOGGER = LogManager.getLogger(RemoteLogReader.class);

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public Stream<String> getStreamOfData(String uri) throws IOException, InterruptedException {
        HttpRequest request =
                HttpRequest.newBuilder().uri(URI.create(uri)).GET().build();

        HttpResponse<Stream<String>> response = httpClient.send(request, HttpResponse.BodyHandlers.ofLines());

        LOGGER.info("Response status code: {}", response.statusCode());

        if (response.statusCode() >= 400) {
            throw new IllegalArgumentException(
                    "Error occured with GET %s, status code: %s".formatted(uri, response.statusCode()));
        }

        return response.body();
    }
}
