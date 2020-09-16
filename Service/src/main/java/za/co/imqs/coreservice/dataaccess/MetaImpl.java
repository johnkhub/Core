package za.co.imqs.coreservice.dataaccess;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;

public class MetaImpl implements Meta {

    private JdbcTemplate template;

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
}
