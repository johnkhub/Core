package za.co.imqs.coreservice.dataaccess;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class FDW_Builder {
    private final String foreignServer;
    private final String foreignDb;

    private String localServerAlias;

    private final String foreignUser;
    private final String foreignPassword;
    private String localUser;

    private List<String> schemas;
    private String[] preamble;

    private final Meta meta;

    public FDW_Builder(
            String foreignUser,
            String foreignPassword,
            Meta meta
    ) {
        this(meta.getServerIP(), "5432", foreignUser, foreignPassword, meta.getDbName(), meta);
    }

    public FDW_Builder(String host, String port, String foreignUser, String foreignPassword, String foreignDb, Meta meta) {
        this.foreignServer = host;
        this.foreignPassword = foreignPassword;
        this.foreignUser = foreignUser;
        this.foreignDb = foreignDb;
        this.meta = meta;
    }

    public FDW_Builder createServer(String asServer) {
        this.localServerAlias = asServer;
        return this;
    }


    public FDW_Builder schemas(String ...schemas) {
        // Verify the existence of the requested schema.table list
        this.schemas = Arrays.asList(schemas);
        return this;
    }

    public FDW_Builder asUser(String localUser) {
        this.localUser = localUser;
        return this;
    }

    public FDW_Builder preamble(String ...strings) {
        this.preamble = strings;
        return this;
    }

    public String get() {
        final StringBuilder bob = new StringBuilder("\n\r");
        bob.append(String.join("\n\r", preamble));
        bob.append("\n\r");
        bob.append("\n\r");

        bob.append("-- Add the extensions in use by the foreign database to avoid missing data types etc.\n\r");
        for (String e : meta.listExtentions()) {
            bob.append("CREATE EXTENSION IF NOT EXISTS \"").append(e).append("\";\n\r");
        }
        bob.append("CREATE EXTENSION IF NOT EXISTS postgres_fdw;").append("\n\r");

        bob.
            append("DROP SERVER IF EXISTS ").append(localServerAlias).append(" CASCADE;").append("\n\r").
            append("CREATE SERVER IF NOT EXISTS ").append(localServerAlias).append(" FOREIGN DATA WRAPPER postgres_fdw ").append("\n\r\t").
            append("OPTIONS (host '").append(foreignServer).append("' ,dbname '").append(foreignDb).append("');").
            append("\n\r").
            append("-- NOTE: Needs to be retrieved via another channel.\n\r").
            append("CREATE USER MAPPING IF NOT EXISTS FOR ").append(localUser).append(" SERVER ").append(localServerAlias).append("\n\r\t").
            append("OPTIONS (user '").append(foreignUser).append("', password '").append(foreignPassword).append("');\n\r");

        if (CollectionUtils.isEmpty(schemas)) {
            schemas = new LinkedList<>(Arrays.asList("asset", "public"));
            schemas.addAll(meta.userSchemas());
        }

        bob.append("\n\r");
        for (String s : schemas) {
            bob.append("IMPORT FOREIGN SCHEMA ").append(s).append(" FROM SERVER ").append(localServerAlias).append(" INTO ").append("public").append(";\n\r");
        }

        return bob.toString();
    }
}