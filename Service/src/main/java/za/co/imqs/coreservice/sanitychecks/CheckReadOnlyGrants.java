package za.co.imqs.coreservice.sanitychecks;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class CheckReadOnlyGrants {
    private static final String SQL = "SELECT all_views.* FROM\n" +
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
            "                WHERE table_schema NOT IN ('information_schema', 'pg_catalog', 'public')\n" +
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

    private final JdbcTemplate jdbc;

    public CheckReadOnlyGrants(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void check() {
        final String user = "normal_reader";
        final List<String> res = jdbc.query(
                SQL,
                (rs, i)-> String.format("View %s is inaccessible to user %s", rs.getString("table_schema")+"."+rs.getString("table_name"), user),
                user
        );

        if (!res.isEmpty()) {
            throw new IllegalStateException("Found inaccessible views:\n"+String.join("\n\t", res));
        }
    }
}
