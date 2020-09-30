package za.co.imqs.coreservice.dto.asset;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssetExternalLinkTypeDto {
    private UUID type_id;
    private String name;
    private String description;

    public static AssetExternalLinkTypeDto of (UUID uuid, String name, String description) {
        AssetExternalLinkTypeDto a = new AssetExternalLinkTypeDto();
        a.setType_id(uuid);
        a.setName(name);
        a.setDescription(description);
        return a;
    }
}
