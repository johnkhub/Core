package za.co.imqs.coreservice.model;

import za.co.imqs.coreservice.dataaccess.exception.BusinessRuleViolationException;
import za.co.imqs.coreservice.dataaccess.exception.ValidationFailureException;
import za.co.imqs.coreservice.dto.CoreAssetDto;

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
public class CoreAssetFactory {

    public CoreAsset create(UUID uuid, CoreAssetDto dto) {
        final CoreAsset asset = handleMandatoryCreateFields(dto, new CoreAsset());
        asset.setAsset_id(uuid);

        if (dto.getAdm_path() != null) asset.setAdm_path(asPath(dto.getAdm_path()));
        if (dto.getCreation_date() != null) asset.setCreation_date(asTimestamp(dto.getCreation_date()));

        return handleOptionalFields(dto, asset);
    }

    public CoreAsset update(UUID uuid, CoreAssetDto dto) {
        final CoreAsset asset = new CoreAsset();
        asset.setAsset_id(uuid);
        if (dto.getName() != null) asset.setName(dto.getName());

        // We may not want to allow update to these fields without some special process as they all determine the identity
        // of the asset to some degree
        // The type_code is especially problematic as it would now reflect on the tables in the asset schema as well i.e. envelope, site etc.
        /*
        if (dto.getFunc_loc_path() != null) asset.setFunc_loc_path(dto.getFunc_loc_path());
        if (dto.getCode() != null) asset.setCode(dto.getCode());
        if (dto.getAsset_type_code() != null) asset.setAsset_type_code(dto.getAsset_type_code());
        */
        assertNotSet(dto.getFunc_loc_path(), "func_loc_path", "");
        //assertNotSet(dto.getAsset_type_code(), "asset_type_code", ""); type code must be set in message for unmarshaling to work, but we ignore it
        assertNotSet(dto.getCode(), "code");

        if (dto.getAdm_path() != null) asset.setAdm_path(asPath(dto.getAdm_path()));
        if (dto.getCreation_date() != null) asset.setCreation_date(asTimestamp(dto.getCreation_date()));

        return handleOptionalFields(dto, asset);
    }

    private CoreAsset handleMandatoryCreateFields(CoreAssetDto dto, CoreAsset asset) {
        asset.setFunc_loc_path(asPath(assertSet(dto.getFunc_loc_path(), "func_loc_path")));
        asset.setName(assertSet(dto.getName(), "name"));
        asset.setCode(assertSet(dto.getCode(), "code"));
        asset.setAsset_type_code(assertSet(dto.getAsset_type_code(), "asset_type_code"));
        return asset;
    }

    private CoreAsset handleOptionalFields(CoreAssetDto dto, CoreAsset asset) {
        if (dto.getAdm_path() != null) asset.setAdm_path(asPath(dto.getAdm_path()));
        if (dto.getCreation_date() != null) asset.setCreation_date(asTimestamp(dto.getCreation_date()));

        if (dto.getGeom() != null) asset.setGeometry(asGeom(dto.getGeom()));

        if (dto.getAddress() != null) asset.setAddress(dto.getAddress());
        if (dto.getLatitude() != null) asset.setLatitude(asBigDecimal(dto.getLatitude()));
        if (dto.getLongitude() != null) asset.setLongitude(asBigDecimal(dto.getLongitude()));

        if (dto.getBarcode() != null) asset.setBarcode(dto.getBarcode());
        if (dto.getSerial_number() != null) asset.setSerial_number(dto.getSerial_number());

        return asset;
    }

    private <T> T assertSet(T value, String name) {
        if (value == null)
            throw new ValidationFailureException("No value set for "+name);
        return value;
    }

    private <T> T assertNotSet(T value, String name) {
        return assertNotSet(value, name, null);
    }

    private <T> T assertNotSet(T value, String name, String hint) {
        if (value != null)
            throw new BusinessRuleViolationException("Change of "+name+" not allowed." +  ((hint != null) ? hint : ""));
        return value;
    }
}

