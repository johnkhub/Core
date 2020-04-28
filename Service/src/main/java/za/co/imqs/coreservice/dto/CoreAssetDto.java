package za.co.imqs.coreservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import com.opencsv.bean.processor.PreAssignmentProcessor;
import com.opencsv.bean.validators.MustMatchRegexExpression;
import com.opencsv.bean.validators.PreAssignmentValidator;
import lombok.Data;
import za.co.imqs.coreservice.dto.imports.Rules;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/05
 */
@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "asset_type_code",
        visible =  true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AssetEnvelopeDto.class, name = "ENVELOPE"),
        @JsonSubTypes.Type(value = AssetFacilityDto.class, name = "FACILITY"),
        @JsonSubTypes.Type(value = AssetBuildingDto.class, name = "BUILDING"),
        @JsonSubTypes.Type(value = AssetSiteDto.class, name = "SITE"),
        @JsonSubTypes.Type(value = AssetFloorDto.class, name = "FLOOR"),
        @JsonSubTypes.Type(value = AssetRoomDto.class, name = "ROOM"),
        @JsonSubTypes.Type(value = AssetComponentDto.class, name = "COMPONENT"),
        @JsonSubTypes.Type(value = AssetLandParcelDto.class, name = "LANDPARCEL")
})


@JsonIgnoreProperties(ignoreUnknown = true)
public class CoreAssetDto implements Rules {

    @CsvBindByName(required = true)
    @PreAssignmentProcessor(processor = ConvertToUppercase.class)
    @PreAssignmentValidator(validator = MustBeInSet.class, paramString = "ASSET,BUILDING,ROOM,FLOOR,SITE,FACILITY")
    String asset_type_code;

    @CsvCustomBindByName(required = true, column="func_loc_path", converter = TerminalNode.class)
    @PreAssignmentValidator(validator = MustMatchRegexExpression.class, paramString = VALID_CODE)
    String code;

    @CsvBindByName(required = true) String name;
    @CsvBindByName(required = false) String adm_path;

    @CsvBindByName(required = true)
    @PreAssignmentValidator(validator = MustMatchRegexExpression.class, paramString = VALID_PATH)
    String func_loc_path;

    @CsvBindByName(required = false) String creation_date;
    @CsvBindByName(required = false) String deactivated_at;

    @CsvBindByName(required = false) String address;

    @CsvBindByName(required = false) String geom;

    @CsvBindByName(required = false)
    @PreAssignmentValidator(validator = MustBeCoordinate.class)
    String latitude;

    @CsvBindByName(required = false)
    @PreAssignmentValidator(validator = MustBeCoordinate.class)
    String longitude;

    @CsvBindByName(required = false) String barcode;
    @CsvBindByName(required = false) String serial_number;
}

//
//  Useful info for sub-classes: https://stackoverflow.com/questions/38572566/warning-equals-hashcode-on-data-annotation-lombok-with-inheritance
//