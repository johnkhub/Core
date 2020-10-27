package za.co.imqs.coreservice.dataaccess;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;

public class MetaImpl implements Meta {

    private final JdbcTemplate template;

    public MetaImpl(DataSource ds) {
        this.template = new JdbcTemplate(ds);
    }

    @Override
    public Set<String> userSchemas() {
        final Set<String> users = new HashSet<>(template.queryForList("SELECT schema_name FROM information_schema.schemata", String.class));
        users.removeAll(systemSchemas());
        return Collections.unmodifiableSet(users.stream().filter(s -> !s.startsWith("pg_")).collect(Collectors.toSet()));
    }

    @Override
    public Set<String> systemSchemas() {
        return new HashSet<>(Arrays.asList("access_control", "asset", "audit", "crud", "public"));
    }

    @Override
    public String getServerIP() {
        return template.queryForObject("SELECT inet_server_addr();", String.class);
    }

    @Override
    public String getDbName() {
        return template.queryForObject(" SELECT current_database()", String.class);
    }

    public List<String> listExtentions() {
        return template.queryForList("SELECT extname FROM pg_extension", String.class);
    }

    @Override
    public List<String> getTablesAndViewsForUser(String userName) {
        return template.query(
                "SELECT * FROM  information_schema.table_privileges WHERE grantee = ? AND privilege_type = 'SELECT'",
                (rs,i)-> {
                    return rs.getString("table_schema") + "." + rs.getString("table_name");
                },
                userName
                );
    }
}
