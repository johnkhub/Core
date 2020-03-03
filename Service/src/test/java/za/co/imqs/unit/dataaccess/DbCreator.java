package za.co.imqs.unit.dataaccess;

import za.co.imqs.libimqs.dbutils.DatabaseUtil;

import javax.sql.DataSource;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/25
 */
public class DbCreator {
    private static final String[] SCHEMAS = {
            "changelog_public.json",
            "changelog_audit.json",
            "changelog_asset.json",
            "changelog_access_control.json",
            "changelog_dtpw.json"
    };

    public static void create(DataSource ds) {
        for (String schema : SCHEMAS) {
            String name = schema.split("_")[1];
            name = name.split("\\.")[0];

            DatabaseUtil.updateDb(ds, schema, true);
        }
    }
}
