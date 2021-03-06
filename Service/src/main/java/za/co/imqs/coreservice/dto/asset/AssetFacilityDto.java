package za.co.imqs.coreservice.dto.asset;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.validators.PreAssignmentValidator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import za.co.imqs.coreservice.imports.Rules;

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
public class AssetFacilityDto extends CoreAssetDto  {
    @CsvBindByName(required = true)
    @PreAssignmentValidator(validator = Rules.MustNotBeNull.class)
    private String facility_type_code;
}
