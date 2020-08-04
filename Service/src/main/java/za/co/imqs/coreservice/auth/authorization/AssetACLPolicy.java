package za.co.imqs.coreservice.auth.authorization;

import za.co.imqs.coreservice.model.CoreAsset;

import java.util.UUID;

public interface AssetACLPolicy {

    void onCreateEntity(UUID grantor, UUID creator, CoreAsset asset);

    default void onDeleteEntity(UUID grantor, UUID creator, UUID entity) {
        // Do nothing
    }

    default void onTransferEntity(UUID grantor, UUID grantee, UUID entity) {
        throw new UnsupportedOperationException();
    }

    default void onShareEntity(UUID sharer, UUID sharedWith, UUID entity, int permissions) {
        throw new UnsupportedOperationException();
    }
}
