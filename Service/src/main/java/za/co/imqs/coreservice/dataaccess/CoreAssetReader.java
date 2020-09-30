package za.co.imqs.coreservice.dataaccess;

import filter.FilterBuilder;
import za.co.imqs.coreservice.dto.asset.AssetExternalLinkTypeDto;
import za.co.imqs.coreservice.model.CoreAsset;
import za.co.imqs.coreservice.model.Quantity;

import java.util.List;
import java.util.Map;
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
    CoreAsset getAssetByExternalId(UUID externalType, String externalId);
    List<CoreAsset> getAssetsByGroupingId(UUID groupingType, String groupingId);
    List<CoreAsset> getAssetByFilter(FilterBuilder filter);

    List<AssetExternalLinkTypeDto> getExternalLinkTypes();
    String getExternalLink(UUID uuid, UUID external_id_type);

    List<AssetExternalLinkTypeDto> getGroupingTypes();
    String getGrouping(UUID uuid, UUID grouping_id_type);

    List<UUID> getAssetsLinkedToLandParcel(UUID landparcel);

    Map<String,UUID> getAssetTypeUUIDs();

    Quantity getQuantity(UUID asset_id, String name);
}
