package za.co.imqs.coreservice.dataaccess;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import za.co.imqs.configuration.client.ConfigClient;
import za.co.imqs.coreservice.dataaccess.exception.NotFoundException;
import za.co.imqs.coreservice.dataaccess.exception.ResubmitException;
import za.co.imqs.coreservice.dataaccess.exception.ValidationFailureException;
import za.co.imqs.libimqs.dbutils.HikariCPClientConfigDatasourceHelper;
import za.co.imqs.libimqs.utils.ConfigClientExt;
import za.co.imqs.libimqs.utils.SimpleConfigClient;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_PRODUCTION;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/28
 */
@Slf4j
@Repository
public class LookupProviderImpl implements LookupProvider {
    private static final RowMapper<KvDef> KV_TYPE_MAPPER = (ResultSet rs, int num) -> {
        final KvDef d = new KvDef();
        d.setCode(rs.getString("code"));
        d.setName(rs.getString("name"));
        d.setDescription(rs.getString("description"));
        d.setOwner(rs.getString("owner"));
        d.setTable(rs.getString("table"));
        return d;
    };

    private static final Set<String> VALID = new HashSet<>(Arrays.asList(">", "<", "=", "!="));

    private final ConfigClientExt config;
    private final ConnectionFactory cFact;
    private final Environment env;

    @Autowired
    public LookupProviderImpl(

            ConfigClient config,
            Environment env
    ){
        this.config = new SimpleConfigClient(config);
        this.cFact = new ConnectionFactory();
        this.env = env;
    }


    @Override
    public List<Map<String,Object>> get(String viewName, Map<String, String> parameters) {
        try {
            viewName = fixQuoting(viewName);

            final StringBuilder query = new StringBuilder("SELECT * FROM " + viewName);
            final List<Object> values = new ArrayList<>(parameters.size());


            if (!parameters.isEmpty()) {
                query.append(" WHERE ");

                int i = 1;
                for (Map.Entry<String, String> p : parameters.entrySet()) {
                    query.append("\"").append(p.getKey()).append("\"").append("= ? ").append(i < parameters.size() ? " and " : "");
                    values.add(p.getValue());
                    i++;
                }
            }

            return cFact.get(viewName).queryForList(query.toString(), values.toArray());
        } catch (TransientDataAccessException e) {
            throw new ResubmitException(e.getMessage());
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }


    public List<Map<String,Object>> getWithOperators(String viewName, Map<String, Field> parameters) {
        try {
            viewName = fixQuoting(viewName);

            final StringBuilder query = new StringBuilder("SELECT * FROM " + viewName);
            final List<Object> values = new ArrayList<>(parameters.size());

            if (!parameters.isEmpty()) {
                query.append(" WHERE ");

                int i = 1;
                for (Map.Entry<String, Field> p : parameters.entrySet()) {
                    final String operator = p.getValue().getOperator();
                    if (!VALID.contains(operator)) throw new ValidationFailureException(operator + " is not a valid selection operator");
                    query.append("\"").append(p.getKey()).append("\"").append(operator).append(" ? ").append(i < parameters.size() ? " and " : "");
                    values.add("'"+p.getValue().getValue()+"'");
                    i++;
                }
            }

            return cFact.get(viewName).queryForList(query.toString(), values.toArray());
        } catch (TransientDataAccessException e) {
            throw new ResubmitException(e.getMessage());
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<KvDef> getKvTypes() {
        try {
            return cFact.get("kv").query("SELECT * FROM kv_type", KV_TYPE_MAPPER);
        } catch (TransientDataAccessException e) {
            throw new ResubmitException(e.getMessage());
        } catch (DataIntegrityViolationException d) { // TODO Can't remember why I did this?
            if (d.getMessage() != null && d.getMessage().contains("No results were returned by the query")) {
                return Collections.emptyList();
            }
            throw d;
        }
    }

    @Override
    public String getKv(String target, String key) {
        final String fqn = resolveTarget(target);
        try {
            return cFact.get("kv").queryForObject("SELECT v FROM " + fqn +" WHERE k = ?", String.class,  key);
        } catch (TransientDataAccessException e) {
            throw new ResubmitException(e.getMessage());
        }  catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    @Transactional("lookup_tx_mgr")
    public void acceptKv(String target, List<Kv> kvs) {
        final String fqn = resolveTarget(target);

        try {
            int[] updateCounts = cFact.get("kv").batchUpdate(
                    String.format("INSERT INTO %s (k,v,creation_date,activated_at,deactivated_at,allow_delete) VALUES (?,?,?,?,?,?) ON CONFLICT DO NOTHING", fqn),
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ps.setString(1, kvs.get(i).getK());
                            ps.setString(2, kvs.get(i).getV());

                            final Timestamp ts = (kvs.get(i).getCreation_date() == null) ? new Timestamp(System.currentTimeMillis()) : asTimestamp(kvs.get(i).getCreation_date());

                            ps.setTimestamp(3, ts);
                            ps.setTimestamp(4, asTimestamp(kvs.get(i).getActivated_at()));
                            ps.setTimestamp(5, asTimestamp(kvs.get(i).getDeactivated_at()));
                            ps.setBoolean(6, kvs.get(i).getAllow_delete() == null ? false : kvs.get(i).getAllow_delete());
                        }

                        @Override
                        public int getBatchSize() {
                            return kvs.size();
                        }
                    }
            );
        } catch (TransientDataAccessException e) {
            throw new ResubmitException(e.getMessage());
        }
    }

    @Override
    public void obliterateKv(String target) {
        final List<String> profiles = Arrays.asList(env.getActiveProfiles());
        if (profiles.contains(PROFILE_PRODUCTION)) {
            throw new RuntimeException("No way!");
        }
        cFact.get("kv").update("DELETE FROM "+ resolveTarget(target));
    }


    private String resolveTarget(String target) {
        try {
           return cFact.get("kv").queryForObject("SELECT * FROM kv_type WHERE code = ?", KV_TYPE_MAPPER, target).getTable();
        }  catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(String.format("'%s' is not a valid kv type!", target));
        }
    }

    private static Timestamp asTimestamp(String s) {
        if (s == null)
            return null;
        return Timestamp.valueOf(s);
    }

    // Double quotes each segment of the fully qualified name
    private static String fixQuoting(String value) {
        final String[] segments = value.split("\\.");
        String rv = "";
        for (String  s : segments) {
            rv = rv + "\"" + s + "\""+".";
        }
        return rv.substring(0,rv.length()-1);
    }

    // TODO In the genaral use case of this code you would be able to connect to a whole bunch of databases
    private class ConnectionFactory {
        // TODO We need to implement a bi-directional map here so we can also look up the viewName based on the ds so we don't instantiate multiple connection pools for the same db
        private final Map<String, JdbcTemplate> dataSources = new HashMap<>();

        public JdbcTemplate get(String viewName) {
            JdbcTemplate ds = dataSources.get(viewName);
            if (ds == null) {
                ds = new JdbcTemplate(
                        HikariCPClientConfigDatasourceHelper.getDefaultDataSource(
                            config.getPropertyAsString("jdbc.jdbcUrl"),
                            config.getPropertyAsString("jdbc.username"),
                            config.getPropertyAsString("jdbc.password")
                ));
                dataSources.put(viewName, ds);
            }
            return ds;
        }
    }
}
