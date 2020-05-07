package za.co.imqs.coreservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class AssetEnvelope extends CoreAsset {
    private String description;
    private String municipality_code;
    private String town_code;
    private String suburb_code;
    private String district_code;
    private String region_code;
    private String ward_code;

    public void validate(boolean create) {
        if (municipality_code != null) validateCode(municipality_code);
        if (town_code != null) validateCode(town_code);
        if (suburb_code != null) validateCode(suburb_code);
        if (district_code != null) validateCode(district_code);
        if (region_code != null) validateCode(region_code);
        if (ward_code != null) validateCode(ward_code);

        super.validate(create);
    }
}