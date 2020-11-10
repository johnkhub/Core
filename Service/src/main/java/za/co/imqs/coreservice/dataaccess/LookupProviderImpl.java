package za.co.imqs.coreservice.dataaccess;

import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Transactional;
import za.co.imqs.configuration.client.ConfigClient;
import za.co.imqs.coreservice.dataaccess.exception.NotFoundException;
import za.co.imqs.coreservice.dataaccess.exception.ResubmitException;
import za.co.imqs.coreservice.dataaccess.exception.ValidationFailureException;
import za.co.imqs.coreservice.dto.lookup.Geometry;
import za.co.imqs.coreservice.model.ORM;
import za.co.imqs.libimqs.dbutils.HikariCPClientConfigDatasourceHelper;
import za.co.imqs.libimqs.utils.ConfigClientExt;
import za.co.imqs.libimqs.utils.SimpleConfigClient;

import java.sql.ResultSet;
import java.util.*;

import static za.co.imqs.coreservice.WebMvcConfiguration.PROFILE_ADMIN;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_PRODUCTION;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_TEST;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/28
 */
@Slf4j
@Profile({PROFILE_PRODUCTION, PROFILE_TEST, PROFILE_ADMIN})
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
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        } catch (Exception e) {
            throw exceptionMapper(e, null);
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
                    values.add(p.getValue().getValue());
                    i++;
                }
            }

            return cFact.get(viewName).queryForList(query.toString(), values.toArray());
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        } catch (Exception e) {
            throw exceptionMapper(e, null);
        }
    }

    @Override
    public List<KvDef> getKvTypes() {
        try {
            return cFact.get("kv").query("SELECT * FROM kv_type", KV_TYPE_MAPPER);
        } catch (DataIntegrityViolationException d) { // TODO Can't remember why I did this?
            if (d.getMessage() != null && d.getMessage().contains("No results were returned by the query")) {
                return Collections.emptyList();
            }
            throw d;
        } catch (Exception e) {
            throw exceptionMapper(e, null);
        }
    }

    @Override
    public String getKvValue(String target, String key) {
        final String fqn = resolveTarget(target);
        try {
            return cFact.get("kv").queryForObject("SELECT v FROM " + fqn +" WHERE k = ?", String.class,  key);
        }  catch (EmptyResultDataAccessException e) {
            return null;
        } catch (Exception e) {
            throw exceptionMapper(e, null);
        }
    }

    @Override
    public <T extends Kv> T getKv(String target, String key) {
        final String fqn = resolveTarget(target);
        final T kv = ORM.lookupModelFactory(target);
        final boolean isGeom = (kv instanceof Geometry);

        try {
            final String selectList = "k,v,creation_date, activated_at, deactivated_at, allow_delete" + (isGeom ? ", geom" : "");

            List<T> list = cFact.get("kv").query(
                    "SELECT "+selectList+" FROM " + fqn +" WHERE k = ?",
                    (rs,i) -> {
                        if (isGeom)  ((Geometry)kv).setGeom(rs.getString("geom"));

                        kv.setAllow_delete(rs.getBoolean("allow_delete"));
                        kv.setDeactivated_at("deactivated_at");
                        kv.setV(rs.getString("v"));
                        kv.setK(rs.getString("k"));
                        kv.setCreation_date("creation_date");
                        kv.setActivated_at("activated_at");
                        return kv;
                    }
                    ,key
            );

            if (list.isEmpty()) return null;
            return list.get(0);
        }  catch (EmptyResultDataAccessException e) {
            return null;
        } catch (Exception e) {
            throw exceptionMapper(e, null);
        }
    }

    @Override
    public List<Kv> getEntireKvTable(String target) {
        final String fqn = resolveTarget(target);
        try {
            return cFact.get("kv").query("SELECT * FROM " + fqn,
                    (rs, i) -> {
                        final Kv kv = new Kv();
                        kv.setActivated_at(rs.getTimestamp("activated_at").toString());
                        kv.setCreation_date(rs.getTimestamp("creation_date").toString());
                        kv.setK(rs.getString("k"));
                        kv.setV(rs.getString("v"));
                        kv.setAllow_delete(rs.getBoolean("allow_delete"));
                        if (rs.getTimestamp("deactivated_at") != null)
                            kv.setDeactivated_at(rs.getTimestamp("deactivated_at").toString());
                        return kv;
                    });
        }  catch (EmptyResultDataAccessException e) {
            return null;
        } catch (Exception e) {
            throw exceptionMapper(e, null);
        }
    }

    @Override
    @Transactional("lookup_tx_mgr")
    public <T extends Kv> void acceptKv(String target, List<T> kvs) {
        final String fqn = resolveTarget(target);

        try {
            final Mapper<T> mapper = new Mapper<>(kvs);
            final SqlParameterSource[] source = mapper.getParameters();
            new NamedParameterJdbcTemplate(cFact.get("kv")).batchUpdate(mapper.getStatement(fqn,source[0]), source);
        } catch (Exception e) {
            throw exceptionMapper(e, null);
        }
    }

    @Override
    public void obliterateKv(String target) {
        final List<String> profiles = Arrays.asList(env.getActiveProfiles());
        if (profiles.contains(PROFILE_PRODUCTION)) {
            throw new RuntimeException("No way!");
        }
        // -- noinspection SqlWithoutWhere
        cFact.get("kv").update("DELETE FROM "+ resolveTarget(target));
    }


    private RuntimeException exceptionMapper(Exception e, String msg) {
        if (e instanceof DataIntegrityViolationException) {
            return new ValidationFailureException(e.getMessage());
        } else if (e instanceof TransientDataAccessException) {
            return new ResubmitException(e.getMessage());

            // Esoteric mappings
        } if (e instanceof TransactionSystemException) {
            final Throwable t = ((TransactionSystemException) e).getOriginalException();

            // A socket write timeout communicating with the Postgres server
            if (t instanceof PSQLException && t.getMessage().contains("An I/O error occurred while sending to the backend")) {
                return new ResubmitException(e.getMessage());
            }
        }

        // fall through
        if (e instanceof RuntimeException) {
            return (RuntimeException)e;
        } else {
            return new RuntimeException(e);
        }
    }


    private String resolveTarget(String target) {
        try {
           return cFact.get("kv").queryForObject("SELECT * FROM kv_type WHERE code = ?", KV_TYPE_MAPPER, target).getTable();
        }  catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(String.format("'%s' is not a valid kv type!", target));
        }
    }

    // Double quotes each segment of the fully qualified name
    private static String fixQuoting(String value) {
        final String[] segments = value.split("\\.");
        StringBuilder rv = new StringBuilder();
        for (String  s : segments) {
            rv.append("\"").append(s).append("\"").append(".");
        }
        return rv.substring(0,rv.length()-1);
    }

    // TODO In the general use case of this code you would be able to connect to a whole bunch of databases
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


    private static class Mapper<T extends Kv> {
        private final Collection<T> elements;

        public Mapper(Collection<T> elements) {
            this.elements = elements;
        }

        public SqlParameterSource[] getParameters() throws Exception {
            SqlParameterSource[] result = new SqlParameterSource[elements.size()];
            int i = 0;
            for (T e : elements) {
                result[i] = mapKv(e);
                i++;
            }

            return result;
        }

        public String getStatement(String fqn, SqlParameterSource src) {
            StringBuilder statement = new StringBuilder("INSERT INTO ").append(fqn).append("(");
            for (String n : src.getParameterNames()) {
                statement.append(n).append(",");
            }
            statement.delete(statement.length()-1,statement.length());

            statement.append(") VALUES (");
            for (String n : src.getParameterNames()) {
                if (n.equals("geom")) { // TODO this is seriously hacky figure out a better way
                    statement.append("ST_GeomFromText(:geom, 4326)").append(",");
                } else {
                    statement.append(":").append(n).append(",");
                }
            }
            statement.delete(statement.length()-1,statement.length());
            statement.append(") ON CONFLICT (k) DO UPDATE SET ");

            for (String n : src.getParameterNames()) {
                if (!n.equals("k")) {
                    statement.append(n).append("=").append("EXCLUDED.").append(n).append(",");
                }
            }
            statement.delete(statement.length()-1,statement.length());

            return statement.append(";").toString();
        }

        private MapSqlParameterSource mapKv(T kv) throws Exception {
            return ORM.mapToSql(kv, new HashSet<>(Arrays.asList("getClass", "getType")), Collections.singleton("getGeom"));
        }
    }
}