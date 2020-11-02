package za.co.imqs.coreservice.dataaccess;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Repository;
import za.co.imqs.coreservice.model.CoreAsset;
import za.co.imqs.coreservice.model.Quantity;

import java.util.List;
import java.util.UUID;

import static za.co.imqs.coreservice.WebMvcConfiguration.PROFILE_ADMIN;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_PRODUCTION;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_TEST;

@Profile({PROFILE_PRODUCTION, PROFILE_TEST, PROFILE_ADMIN})
@Repository
@Qualifier("retrying_core_writer")
public class RetryingWriterProxy implements CoreAssetWriter {

    private final RetryTemplate retry;
    private final CoreAssetWriter delegate;

    @Autowired
    public RetryingWriterProxy(
            @Qualifier("core_retry") RetryTemplate template,
            @Qualifier("core_writer") CoreAssetWriter delegate) {
        this.retry = template;
        this.delegate = delegate;
    }

    @Override
    public void createAssets(List<CoreAsset> assets) {
        retry.execute(
                (c) -> {
                    delegate.createAssets(assets);
                    return null;
                }
        );
    }

    @Override
    public void updateAssets(List<CoreAsset> assets) {
        retry.execute(
                (c) -> {
                    delegate.updateAssets(assets);
                    return null;
                }
        );
    }

    @Override
    public void deleteAssets(List<UUID> uuids) {
        retry.execute(
                (c) -> {
                    delegate.deleteAssets(uuids);
                    return null;
                }
        );
    }

    @Override
    public void addExternalLink(UUID uuid, UUID externalIdType, String externalId) {
        retry.execute(
                (c) -> {
                    delegate.addExternalLink(uuid, externalIdType, externalId);
                    return null;
                }
        );
    }

    @Override
    public void deleteExternalLink(UUID uuid, UUID externalIdType, String externalId) {
        retry.execute(
                (c) -> {
                    delegate.deleteExternalLink(uuid, externalIdType, externalId);
                    return null;
                }
        );
    }

    @Override
    public void updateExternalLink(UUID uuid, UUID externalIdType, String externalId) {
        retry.execute(
                (c) -> {
                    delegate.updateExternalLink(uuid, externalIdType, externalId);
                    return null;
                }
        );
    }

    @Override
    public void addToGrouping(UUID uuid, UUID externalIdType, String externalId) {
        retry.execute(
                (c) -> {
                    delegate.addToGrouping(uuid, externalIdType, externalId);
                    return null;
                }
        );
    }

    @Override
    public void deleteFromGrouping(UUID uuid, UUID externalIdType, String externalId) {
        retry.execute(
                (c) -> {
                    delegate.deleteFromGrouping(uuid, externalIdType, externalId);
                    return null;
                }
        );
    }

    @Override
    public void updateGrouping(UUID uuid, UUID externalIdType, String externalId) {
        retry.execute(
                (c) -> {
                    delegate.updateGrouping(uuid, externalIdType, externalId);
                    return null;
                }
        );
    }

    @Override
    public void linkAssetToLandParcel(UUID asset, UUID to) {
        retry.execute(
                (c) -> {
                    delegate.linkAssetToLandParcel(asset, to);
                    return null;
                }
        );
    }

    @Override
    public void unlinkAssetFromLandParcel(UUID asset, UUID from) {
        retry.execute(
                (c) -> {
                    delegate.unlinkAssetFromLandParcel(asset, from);
                    return null;
                }
        );
    }

    @Override
    public void addLinkedData(String table, String field, UUID assetId, String value) {
        retry.execute(
                (c) -> {
                    delegate.addLinkedData(table, field, assetId, value);
                    return null;
                }
        );
    }

    @Override
    public void updateLinkedData(String table, String field, UUID assetId, String value) {
        retry.execute(
                (c) -> {
                    delegate.updateLinkedData(table,field,assetId,value);
                    return null;
                }
        );
    }

    @Override
    public void deleteLinkedData(String table, String field, UUID assetId) {
        retry.execute(
                (c) -> {
                    delegate.deleteLinkedData(table,field,assetId);
                    return null;
                }
        );
    }

    @Override
    public void addQuantity(Quantity quantity) {
        retry.execute(
                (c) -> {
                    delegate.addQuantity(quantity);
                    return null;
                }
        );
    }

    @Override
    public void updateQuantity(Quantity quantity) {
        retry.execute(
                (c) -> {
                    delegate.updateQuantity(quantity);
                    return null;
                }
        );
    }

    @Override
    public void deleteQuantity(UUID uuid, String name) {
        retry.execute(
                (c) -> {
                    delegate.deleteQuantity(uuid, name);
                    return null;
                }
        );
    }

    @Override
    public void obliterateAssets(List<UUID> uuids) {
        retry.execute(
                (c) -> {
                    delegate.obliterateAssets(uuids);
                    return null;
                }
        );
    }

    @Override
    public void obliterateAssets(UUID... uuids) {
        retry.execute(
                (c) -> {
                    delegate.obliterateAssets(uuids);
                    return null;
                }
        );
    }
}
