package za.co.imqs.coreservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class AssetRoom extends CoreAsset {
    private String description;

    public void validate(boolean create) {
        super.validate(create);
    }
}
