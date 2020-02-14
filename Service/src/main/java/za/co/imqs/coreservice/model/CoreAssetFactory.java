package za.co.imqs.coreservice.model;

import za.co.imqs.coreservice.dto.CoreAssetDto;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/05
 */
public class CoreAssetFactory {

    public CoreAsset from(CoreAssetDto dto) {
        final CoreAsset asset = new CoreAsset();
        return asset;
    }
}
