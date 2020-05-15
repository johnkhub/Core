package za.co.imqs.coreservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/05
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true, includeFieldNames=true)
public class AssetEnvelopeDto extends CoreAssetDto {
    @CsvBindByName(required = false) private String description;
    @CsvBindByName(required = false) private String municipality_code;
    @CsvBindByName(required = false) private String town_code;
    @CsvBindByName(required = false) private String suburb_code;
    @CsvBindByName(required = false) private String district_code;
    @CsvBindByName(required = false) private String region_code;
    @CsvBindByName(required = false) private String ward_code;
}
