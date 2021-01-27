package za.co.imqs.coreservice.sanitychecks;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class CheckReadOnlyGrants implements Check {
    private static final String SQL_VIEWS = "SELECT all_views.* FROM\n" +
            "    (\n" +
            "        SELECT\n" +
            "            grantee, table_schema, table_name\n" +
            "        FROM information_schema.table_privileges\n" +
            "        WHERE\n" +
            "            privilege_type = 'SELECT' AND\n" +
            "            grantee = 'postgres' AND\n" +
            "            table_name IN\n" +
            "            (\n" +
            "                SELECT\n" +
            "                    table_name AS view_name\n" +
            "                FROM information_schema.views\n" +
            "                WHERE table_schema NOT IN ('information_schema', 'pg_catalog', 'public') AND table_name NOT IN ('asset_core_view_internal','import_report_view')\n"+
            "                ORDER BY view_name\n" +
            "            )\n" +
            "    )  AS all_views -- User postgres owns all views\n" +
            "LEFT JOIN\n" +
            "    (\n" +
            "        SELECT\n" +
            "            grantee, table_schema, table_name\n" +
            "        FROM information_schema.table_privileges\n" +
            "        WHERE\n" +
            "            privilege_type = 'SELECT' AND\n" +
            "            grantee = ? AND\n" +
            "            table_name IN\n" +
            "            (\n" +
            "                SELECT\n" +
            "                    table_name AS view_name\n" +
            "                FROM information_schema.views\n" +
            "                WHERE table_schema NOT IN ('information_schema', 'pg_catalog', 'public')\n" +
            "                ORDER BY view_name\n" +
            "            )\n" +
            "    ) AS reader -- User ? needs to be granted read access\n" +
            "ON all_views.table_name = reader.table_name\n" +
            "WHERE reader.table_name is null\n";

    private static final String SQL_REF_TABLES = " SELECT all_ref_tables.* FROM\n" +
            "                (\n" +
            "                    SELECT\n" +
            "                        grantee, table_schema, table_name\n" +
            "                    FROM information_schema.table_privileges\n" +
            "                    WHERE\n" +
            "                        privilege_type = 'SELECT' AND\n" +
            "                        grantee = 'postgres' AND\n" +
            "                        table_name IN\n" +
            "                        (\n" +
            "                            SELECT\n" +
            "                                table_name AS ref_table_name\n" +
            "                            FROM information_schema.tables\n" +
            "                            WHERE table_schema \n" +
            "\t\t\t\t\t\t\t\tNOT IN ('information_schema', 'pg_catalog', 'public') AND table_type = 'BASE TABLE'\n" +
            "\t\t\t\t\t\t\t\tAND table_name LIKE ('ref_%')\n" +
            "                            ORDER BY table_name\n" +
            "                        )\n" +
            "                )  AS all_ref_tables -- User postgres owns all views\n" +
            "            LEFT JOIN\n" +
            "                (\n" +
            "                    SELECT\n" +
            "                        grantee, table_schema, table_name\n" +
            "                    FROM information_schema.table_privileges\n" +
            "                    WHERE\n" +
            "                        privilege_type = 'SELECT' AND\n" +
            "                        grantee = ? AND\n" +
            "                        table_name IN\n" +
            "                        (\n" +
            "                            SELECT\n" +
            "                                table_name AS table_name\n" +
            "                            FROM information_schema.tables\n" +
            "                            WHERE table_schema NOT IN ('information_schema', 'pg_catalog')\n" +
            "                            ORDER BY table_name\n" +
            "                        )\n" +
            "                ) AS reader -- User ? needs to be granted read access\n" +
            "            ON all_ref_tables.table_name = reader.table_name\n" +
            "            WHERE reader.table_name is null;\n";

    private final JdbcTemplate jdbc;

    public CheckReadOnlyGrants(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void check() {
        checkViews();
        checkLookups();
    }

    private void checkViews() {
        final String user = "normal_reader";
        final List<String> res = jdbc.query(
                SQL_VIEWS,
                (rs, i)-> String.format("View %s is inaccessible to user %s", rs.getString("table_schema")+"."+rs.getString("table_name"), user),
                user
        );

        if (!res.isEmpty()) {
            throw new IllegalStateException("Found inaccessible views:\n"+String.join("\n\t", res));
        }
    }

    private void checkLookups() {
        final String user = "normal_reader";
        final List<String> res = jdbc.query(
                SQL_REF_TABLES,
                (rs, i)-> String.format("Ref table %s is inaccessible to user %s", rs.getString("table_schema")+"."+rs.getString("table_name"), user),
                user
        );

        if (!res.isEmpty()) {
            throw new IllegalStateException("Found inaccessible ref tables:\n"+String.join("\n\t", res));
        }
    }
}
