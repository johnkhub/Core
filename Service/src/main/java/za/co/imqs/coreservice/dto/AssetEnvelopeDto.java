package za.co.imqs.coreservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/05
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode(callSuper=true)
public class AssetEnvelopeDto extends CoreAssetDto {
    private String description;
    private String municipality_code;
    private String town_code;
    private String suburb_code;
    private String district_code;
    private String region_code;
    private String ward_code;
}
