package za.co.imqs.coreservice.imports;

import lombok.Data;

@Data
public class Config {
    private String authUrl;
    private String serviceUrl;
    private String loginUsername;
    private String loginPassword;

    public Config() {}
}
