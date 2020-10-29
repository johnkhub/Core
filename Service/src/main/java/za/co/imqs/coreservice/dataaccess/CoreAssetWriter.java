package za.co.imqs.coreservice.dataaccess;

import za.co.imqs.coreservice.dto.QuantityDto;
import za.co.imqs.coreservice.model.CoreAsset;
import za.co.imqs.coreservice.model.Quantity;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/05
 */
public interface CoreAssetWriter {
    public void createAssets(List<CoreAsset> assets);
    public void updateAssets(List<CoreAsset> assets);
    public void deleteAssets(List<UUID> uuids);

    public void addExternalLink(UUID uuid, UUID externalIdType, String externalId);
    public void deleteExternalLink(UUID uuid, UUID externalIdType, String externalId);
    public void updateExternalLink(UUID uuid, UUID externalIdType, String externalId);

    public void addToGrouping(UUID uuid, UUID externalIdType, String externalId);
    public void deleteFromGrouping(UUID uuid, UUID externalIdType, String externalId);
    public void updateGrouping(UUID uuid, UUID externalIdType, String externalId);

    public void linkAssetToLandParcel(UUID asset, UUID to);
    public void unlinkAssetFromLandParcel(UUID asset, UUID from);

    public void addLinkedData(String table, String field, UUID assetId, String value);
    public void updateLinkedData(String table, String field, UUID assetId, String value);
    public void deleteLinkedData(String table, String field, UUID assetId);

    public void addQuantity(Quantity quantity);
    public void updateQuantity(Quantity quantity);
    public void deleteQuantity(UUID uuid, String name);

    //
    // Extra methods for *Testing*
    // These will refuse to execute in production
    //
    public void obliterateAssets(List<UUID> uuids);
    default void obliterateAssets(UUID ...uuids) {
        obliterateAssets(Arrays.asList(uuids));
    }
}
