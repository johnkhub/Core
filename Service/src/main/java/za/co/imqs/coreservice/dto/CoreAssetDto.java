package za.co.imqs.coreservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

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
        @JsonSubTypes.Type(value = AssetComponentDto.class, name = "COMPONENT")
})


@JsonIgnoreProperties(ignoreUnknown = true)
public class CoreAssetDto {
    String asset_type_code;
    String code;
    String name;
    String adm_path;
    String func_loc_path;
    String creation_date;
    String  deactivated_at;

    String address;
    String geom;
    String latitude;
    String longitude;
    String barcode;
    String serial_number;
}