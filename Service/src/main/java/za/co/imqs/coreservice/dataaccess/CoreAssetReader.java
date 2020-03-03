package za.co.imqs.coreservice.dataaccess;

import za.co.imqs.coreservice.model.CoreAsset;

import java.util.List;
import java.util.UUID;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/03/06
 */
public interface CoreAssetReader {
    CoreAsset getAsset(UUID uuid);
    List<String> getExternalLinks(UUID uuid, UUID external_id_type);
}
