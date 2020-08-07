package za.co.imqs.coreservice.model;

import za.co.imqs.coreservice.dataaccess.exception.BusinessRuleViolationException;
import za.co.imqs.coreservice.dataaccess.exception.ValidationFailureException;
import za.co.imqs.coreservice.dto.asset.*;

import java.util.UUID;

import static za.co.imqs.coreservice.Validation.*;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/05
 *
 * Convert DTO to domain model
 */
public class AssetFactory {

    public <T extends CoreAsset, D extends CoreAssetDto> T create(UUID uuid, D dto) {
        return create(uuid, dto, createAsset(dto));
    }

    public <T extends CoreAsset, D extends CoreAssetDto> T update(UUID uuid, D dto) {
        return update(uuid, dto, createAsset(dto));
    }


    // TODO - this should be using a generic mapping implementation
    private static <T extends CoreAsset, D extends CoreAssetDto> T createAsset(D dto) {
        if (dto instanceof AssetLandparcelDto) {
            final AssetLandparcel a = new AssetLandparcel();
            a.setLpi(((AssetLandparcelDto) dto).getLpi());
            return (T) a;
        } else if (dto instanceof AssetBuildingDto) {
            final AssetBuilding a = new AssetBuilding();
            return (T) a;
        } else if (dto instanceof AssetComponentDto) {
            final AssetComponent a = new AssetComponent();
            return (T) a;
        } else if (dto instanceof AssetEnvelopeDto) {
            final AssetEnvelope a = new AssetEnvelope();
            return (T) a;
        } else if (dto instanceof AssetFacilityDto) {
            final AssetFacility a = new AssetFacility();
            a.setFacility_type_code(((AssetFacilityDto) dto).getFacility_type_code());
            return (T) a;
        } else if (dto instanceof AssetFloorDto) {
            final AssetFloor a = new AssetFloor();
            return (T) a;
        } else if (dto instanceof AssetRoomDto) {
            final AssetRoom a = new AssetRoom();
            return (T) a;
        } else if (dto instanceof AssetSiteDto) {
            final AssetRoom a = new AssetRoom();
            return (T) a;
        }
        throw new IllegalArgumentException("Unknown type " + dto.getClass().getCanonicalName());
    }


    private <T extends CoreAsset, D extends CoreAssetDto> T create(UUID uuid, D dto, T asset) {
        handleMandatoryCreateFields(dto, asset);
        asset.setAsset_id(uuid);

        if (dto.getAdm_path() != null) asset.setAdm_path(asPath(dto.getAdm_path()));
        if (dto.getCreation_date() != null) asset.setCreation_date(asTimestamp(dto.getCreation_date()));

        return handleOptionalFields(dto, asset);
    }

    private <T extends CoreAsset, D extends CoreAssetDto> T update(UUID uuid, D dto, T asset) {
        asset.setAsset_id(uuid);
        if (dto.getName() != null) asset.setName(dto.getName());

        // TODO
        // We may not want to allow update to these fields without some special process as they all determine the identity
        // of the asset to some degree
        // The type_code is especially problematic as it would now reflect on the tables in the asset schema as well i.e. envelope, site etc.

        //assertNotSet(dto.getFunc_loc_path(), "func_loc_path", "");
        //assertNotSet(dto.getAsset_type_code(), "asset_type_code", ""); type code must be set in message for unmarshalling to work, but we ignore it
        //assertNotSet(dto.getCode(), "code", dto);

        if (dto.getFunc_loc_path() != null) asset.setFunc_loc_path(dto.getFunc_loc_path());
        if (dto.getCode() != null) asset.setCode(dto.getCode());
        if (dto.getAsset_type_code() != null) asset.setAsset_type_code(dto.getAsset_type_code());

        if (dto.getAdm_path() != null) asset.setAdm_path(asPath(dto.getAdm_path()));
        if (dto.getCreation_date() != null) asset.setCreation_date(asTimestamp(dto.getCreation_date()));

        return handleOptionalFields(dto, asset);
    }

    private <T extends CoreAsset, D extends CoreAssetDto> T  handleMandatoryCreateFields(D dto, T asset) {
        asset.setFunc_loc_path(asPath(assertSet(dto.getFunc_loc_path(), "func_loc_path", dto)));
        asset.setName(assertSet(dto.getName(), "name", dto));
        asset.setCode(assertSet(dto.getCode(), "code", dto));
        asset.setAsset_type_code(assertSet(dto.getAsset_type_code(), "asset_type_code", dto));
        return asset;
    }

    private <T extends CoreAsset, D extends CoreAssetDto> T handleOptionalFields(D dto, T asset) {
        if (dto.getAdm_path() != null) asset.setAdm_path(asPath(dto.getAdm_path()));
        if (dto.getCreation_date() != null) asset.setCreation_date(asTimestamp(dto.getCreation_date()));

        if (dto.getGeom() != null) asset.setGeom(asGeom(dto.getGeom()));

        if (dto.getAddress() != null) asset.setAddress(dto.getAddress());
        if (dto.getLatitude() != null) asset.setLatitude(asBigDecimal(dto.getLatitude()));
        if (dto.getLongitude() != null) asset.setLongitude(asBigDecimal(dto.getLongitude()));

        if (dto.getBarcode() != null) asset.setBarcode(dto.getBarcode());
        if (dto.getSerial_number() != null) asset.setSerial_number(dto.getSerial_number());

        if (dto.getResponsible_dept_code() != null) asset.setResponsible_dept_code(dto.getResponsible_dept_code());
        if (dto.getIs_owned() != null) asset.setIs_owned(dto.getIs_owned());

        if (dto.getDescription() != null) asset.setDescription(dto.getDescription());

        if (dto.getDistrict_code() != null) asset.setDistrict_code(dto.getDistrict_code());
        if (dto.getMunicipality_code() != null) asset.setMunicipality_code(dto.getMunicipality_code());
        if (dto.getRegion_code() != null) asset.setRegion_code(dto.getRegion_code());
        if (dto.getSuburb_code() != null) asset.setSuburb_code(dto.getSuburb_code());
        if (dto.getTown_code() != null) asset.setTown_code(dto.getTown_code());
        if (dto.getWard_code() != null) asset.setWard_code(dto.getWard_code());

        return asset;
    }

    private <T, D> T assertSet(T value, String name, D dto) {
        if (value == null)
            throw new ValidationFailureException("No value set for "+name+" in "+dto.toString());
        return value;
    }

    private <T,D> T assertNotSet(T value, String name, D dto) {
        return assertNotSet(value, name, null, dto);
    }

    @SuppressWarnings("SameReturnValue")
    private <T,D> T assertNotSet(T value, String name, String hint, D dto) {
        if (value != null)
            throw new BusinessRuleViolationException("Change of "+name+" not allowed." +  ((hint != null) ? hint : "") + " in " + dto.toString());
        return null;
    }
}

