package za.co.imqs.coreservice.imports;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {
    private String authUrl;
    private String serviceUrl;
    private String loginUsername;
    private String loginPassword;

    public Config() {}
}
