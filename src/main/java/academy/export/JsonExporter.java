package academy.export;

import academy.stats.Stats;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;

public class JsonExporter implements ILogsExporter {
    private final ObjectMapper mapper;

    public JsonExporter() {
        mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public void export(String filename, Stats stats) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(Path.of(filename).toFile(), stats);
    }
}
