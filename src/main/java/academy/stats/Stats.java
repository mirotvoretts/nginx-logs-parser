package academy.stats;

import academy.model.RequestData;
import academy.model.ResourceData;
import academy.model.ResponseCode;
import academy.model.ResponseSizeInBytes;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** Statistics on requests. Serializable object */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Stats {
    private List<String> files = new ArrayList<>();
    private int totalRequestsCount = 0;
    private ResponseSizeInBytes responseSizeInBytes = new ResponseSizeInBytes();
    private List<ResourceData> resources = new ArrayList<>();
    private List<ResponseCode> responseCodes = new ArrayList<>();
    private List<RequestData> requestsPerDate = new ArrayList<>();
    private List<String> uniqueProtocols = new ArrayList<>();
}
