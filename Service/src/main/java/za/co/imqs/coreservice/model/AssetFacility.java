package za.co.imqs.coreservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class AssetFacility extends CoreAsset {
    private String description;
    private String facility_type_code;

    public void validate(boolean create) {
        if (facility_type_code != null) validateCode(facility_type_code);

        super.validate(create);
    }
}
