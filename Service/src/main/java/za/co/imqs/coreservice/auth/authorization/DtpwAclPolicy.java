package za.co.imqs.coreservice.auth.authorization;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import za.co.imqs.coreservice.dataaccess.CoreAssetReader;
import za.co.imqs.coreservice.dataaccess.PermissionRepository;
import za.co.imqs.coreservice.dataaccess.exception.NotPermittedException;
import za.co.imqs.coreservice.dto.GroupDto;
import za.co.imqs.coreservice.model.CoreAsset;

import java.util.UUID;

@Slf4j
public class DtpwAclPolicy implements AssetACLPolicy {

    private final PermissionRepository perms;
    private final CoreAssetReader assetReader;

    public DtpwAclPolicy( PermissionRepository perms, CoreAssetReader assetReader) {
        this.perms = perms;
        this.assetReader = assetReader;
    }


    @Override
    // The policy is to give the group belonging to the responsible department access to the asset
    public void onCreateEntity(UUID grantor, UUID creator, CoreAsset asset) {
        final String dept = asset.getResponsible_dept_code();
        if (StringUtils.isEmpty(dept)) {
            log.warn("No Responsible Department set for asset {}. Asset will not be modifiable until value specified", asset);
            return;
        }

        final GroupDto group = perms.getGroupByName(dept);
        if (group == null) {
            throw new NotPermittedException(String.format("Responsible Department %s is not allowed to create this type of asset %s.", dept, asset.toString()));
        }

        final UUID type = assetReader.getAssetTypeUUIDs().get(asset.getAsset_type_code());
        if (type == null) throw new IllegalArgumentException();

        final int grantable = perms.getGrant(group.getGroup_id(), type);
        perms.grantPermissions(perms.getSystemPrincipal(), creator, grantable, type);
    }
}
