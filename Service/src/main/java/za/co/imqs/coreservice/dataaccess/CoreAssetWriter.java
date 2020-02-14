package za.co.imqs.coreservice.dataaccess;

import org.springframework.web.bind.annotation.RequestParam;
import za.co.imqs.coreservice.model.CoreAsset;

import java.util.Collection;
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

    public void addExternalLink(UUID uuid, String externalIdType, String externalId);
    public void deleteExternalLink(UUID uuid, String externalIdType, String externalId);
}
