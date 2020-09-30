package za.co.imqs.coreservice.dto.asset;

import com.fasterxml.jackson.annotation.*;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.processor.PreAssignmentProcessor;
import com.opencsv.bean.validators.MustMatchRegexExpression;
import com.opencsv.bean.validators.PreAssignmentValidator;
import lombok.Data;
import za.co.imqs.coreservice.dto.ErrorProvider;
import za.co.imqs.coreservice.imports.Rules;

import static za.co.imqs.coreservice.imports.Rules.VALID_FREE_TEXT;
import static za.co.imqs.coreservice.imports.Rules.VALID_PATH;

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
        @JsonSubTypes.Type(value = AssetLandparcelDto.class, name = "LANDPARCEL")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoreAssetDto implements ErrorProvider {

    @CsvBindByName(required = false)
    @PreAssignmentProcessor(processor = Rules.Trim.class)
    private String asset_id;


    @CsvBindByName(required = true)
    @PreAssignmentProcessor(processor = Rules.ConvertToUppercase.class)
    @PreAssignmentValidator(validator = Rules.MustBeInSet.class, paramString = "ENVELOPE,BUILDING,ROOM,FLOOR,SITE,FACILITY,COMPONENT,LANDPARCEL")
    private String asset_type_code;

    private String code;

    @CsvBindByName(required = true)
    @PreAssignmentProcessor(processor = Rules.Trim.class)
    @PreAssignmentValidator(validator = MustMatchRegexExpression.class, paramString = VALID_FREE_TEXT)
    private String name;

    @CsvBindByName(required = false)
    @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
    private String adm_path;

    @CsvBindByName(required = true)
    @PreAssignmentProcessor(processor = Rules.Replace.class, paramString = "-,.")
    @PreAssignmentValidator(validator = MustMatchRegexExpression.class, paramString = VALID_PATH)
    private String func_loc_path;

    @CsvBindByName(required = false)
    @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
    private String creation_date;

    @CsvBindByName(required = false)
    @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
    private String deactivated_at;

    @CsvBindByName(required = false)
    @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
    @PreAssignmentValidator(validator = Rules.OptionallyMatchRegex.class, paramString = VALID_FREE_TEXT)
    private String address;

    @CsvBindByName(required = false)
    @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
    private String geom;

    @CsvBindByName(required = false)
    @PreAssignmentValidator(validator = Rules.MustBeCoordinate.class)
    @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
    private String latitude;

    @CsvBindByName(required = false)
    @PreAssignmentValidator(validator = Rules.MustBeCoordinate.class)
    @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
    private String longitude;

    @CsvBindByName(required = false)
    @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
    private String barcode;

    @CsvBindByName(required = false)
    @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
    private String serial_number;

    @CsvBindByName(required = false)
    @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
    private String responsible_dept_code;

    @CsvBindByName(required = false)
    @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
    private Boolean is_owned;


    @CsvBindByName(required = false)
    private String description;

    @CsvBindByName(required = false)
    @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
    private String municipality_code;

    @CsvBindByName(required = false)
    @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
    private String town_code;

    @CsvBindByName(required = false)
    @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
    private String suburb_code;

    @CsvBindByName(required = false)
    @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
    private String district_code;

    @CsvBindByName(required = false)
    @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
    private String region_code;

    @CsvBindByName(required = false)
    @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
    private String ward_code;


    // This is so we have a field to write errors to in the exception files
    @CsvBindByName(required = false)
    @JsonIgnore
    private String error;
}

//
//  Useful info for sub-classes: https://stackoverflow.com/questions/38572566/warning-equals-hashcode-on-data-annotation-lombok-with-inheritance
//