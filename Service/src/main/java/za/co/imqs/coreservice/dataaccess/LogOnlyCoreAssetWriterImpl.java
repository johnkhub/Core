package za.co.imqs.coreservice.dataaccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import za.co.imqs.coreservice.model.CoreAsset;
import za.co.imqs.coreservice.model.Quantity;

import java.util.List;
import java.util.UUID;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/25
 */
@Slf4j
public class LogOnlyCoreAssetWriterImpl implements CoreAssetWriter {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void createAssets(List<CoreAsset> assets) {
        try {
            log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(assets));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateAssets(List<CoreAsset> assets) {
        try {
            log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(assets));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAssets(List<UUID> uuids) {
        try {
            log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(uuids));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addExternalLink(UUID uuid, UUID externalIdType, String externalId) {
        log.info("Link {} of type {} to {}", externalId, externalIdType, uuid );
    }

    @Override
    public void deleteExternalLink(UUID uuid, UUID externalIdType, String externalId) {
        log.info("Unlink {} of type {} to {}", externalId, externalIdType, uuid );
    }

    @Override
    public void updateExternalLink(UUID uuid, UUID externalIdType, String externalId) {
        log.info("Update link {} of type {} to {}", externalId, externalIdType, uuid );
    }

    @Override
    public void linkAssetToLandParcel(UUID asset, UUID to) {

    }

    @Override
    public void unlinkAssetFromLandParcel(UUID asset, UUID from) {

    }


    @Override
    public void addLinkedData(String table, String field, UUID assetId, String value) {

    }

    @Override
    public void updateLinkedData(String table, String field, UUID assetId, String value) {

    }

    @Override
    public void deleteLinkedData(String table, String field, UUID assetId) {

    }

    @Override
    public void obliterateAssets(List<UUID> uuids) {

    }

    @Override
    public void addToGrouping(UUID uuid, UUID externalIdType, String externalId) {

    }

    @Override
    public void deleteFromGrouping(UUID uuid, UUID externalIdType, String externalId) {

    }

    @Override
    public void updateGrouping(UUID uuid, UUID externalIdType, String externalId) {

    }

    @Override
    public void addQuantity(Quantity quantity) {

    }

    @Override
    public void updateQuantity(Quantity quantity) {

    }

    @Override
    public void deleteQuantity(UUID uuid, String name) {

    }
}
