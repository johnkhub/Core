package za.co.imqs.coreservice.dataaccess;

import filter.FilterBuilder;
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
    CoreAsset getAssetByFuncLocPath(String path);
    CoreAsset getAssetByXXPath(String pathName, String value);
    CoreAsset getAssetByExternalId(String externalType, String externalId);
    List<CoreAsset> getAssetByFilter(FilterBuilder filter);

    List<String> getExternalLinks(UUID uuid, UUID external_id_type);
}
