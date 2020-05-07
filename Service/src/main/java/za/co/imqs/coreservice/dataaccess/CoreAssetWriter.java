package za.co.imqs.coreservice.dataaccess;

import za.co.imqs.coreservice.model.CoreAsset;

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
    public enum AssetImportMode {
        INSERT,
        UPSERT,
        REPLACE
    }

    public void createAssets(List<CoreAsset> assets);
    public void updateAssets(List<CoreAsset> assets);
    public void deleteAssets(List<UUID> uuids);

    public void importAssets(List<CoreAsset> assets, AssetImportMode mode);
    default void importAsset(List<CoreAsset> assets) {
        importAssets(assets, AssetImportMode.UPSERT);
    }

    public void addExternalLink(UUID uuid, UUID externalIdType, String externalId);
    public void deleteExternalLink(UUID uuid, UUID externalIdType, String externalId);

    //
    // Extra methods for *Testing*
    // These will refuse to execute in production
    //
    public void obliterateAssets(List<UUID> uuids);
    default void obliterateAssets(UUID ...uuids) {
        obliterateAssets(Arrays.asList(uuids));
    }
}
