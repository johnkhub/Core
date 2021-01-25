package za.co.imqs.coreservice.dataaccess;

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

    // TODO : HACK HACK HACK this is a partial duplicate of the init0.sql script
    public List<String> getUserTypes() {
        return Collections.singletonList("CREATE TYPE public.unit_type AS ENUM\n" +
                "    (\n" +
                "        'T_TIME', 'T_LENGTH', 'T_MASS', 'T_CURRENT', 'T_TEMPERATURE', 'T_LUMINOSITY', 'T_VOLTAGE', 'T_POWER', 'T_VOLUME',\n" +
                "        'T_AREA', 'T_CURRENCY', 'T_VELOCITY', 'T_DENSITY', 'T_PRESSURE', 'T_SCALAR'\n" +
                "    );");
    }

    @Override
    public List<String> getTablesAndViewsForUser(String userName) {
        return template.query(
                "SELECT * FROM  information_schema.table_privileges WHERE grantee = ? AND privilege_type = 'SELECT'",
                (rs,i)-> rs.getString("table_schema") + "." + rs.getString("table_name"),
                userName
                );
    }
}
