package za.co.imqs.coreservice.audit;

import java.util.List;

public interface AuditLogger {
    public enum Operation {
        QUERY_USERS,
        QUERY_GROUPS,
        QUERY_ACL,
        QUERY_TAGS,

        ADD_ASSET,
        DELETE_ASSET,
        UPDATE_ASSET,

        ADD_ASSET_LINK,
        DELETE_ASSET_LINK,
        UPDATE_ASSET_LINK,

        ADD_KV_TYPE,

        ADD_KV_VALUE,
        DELETE_KV_VALUE,

        LOGIN,
        LOGOUT,

        ADD_USER,
        DELETE_USER,

        ADD_GROUP,
        DELETE_GROUP,

        JOIN_GROUP,
        LEAVE_GROUP,

        GRANT_ACL,
        REVOKE_ACL,

        ADD_TAGS,
        DELETE_TAGS
    }
    public enum Result {
        SUCCESS,FAILURE
    }

    public void log(AuditLogEntry entry);
    public void log(List<AuditLogEntry> entries);
}