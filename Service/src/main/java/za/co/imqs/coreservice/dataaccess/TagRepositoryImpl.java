package za.co.imqs.coreservice.dataaccess;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import za.co.imqs.coreservice.dataaccess.exception.NotFoundException;
import za.co.imqs.coreservice.dataaccess.exception.ResubmitException;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Repository
public class TagRepositoryImpl implements TagRepository {
    private final JdbcTemplate jdbc;

    @Autowired
    public TagRepositoryImpl(@Qualifier("core_ds") DataSource ds) {
        this.jdbc = new JdbcTemplate(ds);
    }

    @Override
    @Transactional("core_tx_mgr")
    public List<String> getTagsFor(UUID uuid) {
        try {
            return jdbc.queryForObject(
                    "SELECT tags FROM public.asset_tags WHERE asset_id = ?",
                    (rs, i) -> {
                        final String[] t = (String[]) rs.getArray("tags").getArray();
                        return Arrays.asList(t);
                    },
                    uuid);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        } catch (Exception e) {
            return mapFunctionExceptions(e);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    @Transactional("core_tx_mgr")
    public boolean hasTags(UUID uuid, String ...tags) {
        try {
            return jdbc.execute("{? = call public.fn_has_tags(?, ?)}", (CallableStatement stmt) -> {
                stmt.registerOutParameter(1, Types.BOOLEAN);
                stmt.setObject(2, uuid);
                stmt.setArray(3, stmt.getConnection().createArrayOf("VARCHAR", tags));
                stmt.execute();
                return stmt.getBoolean(1);
            });
        } catch (Exception e) {
            return mapFunctionExceptions(e);
        }
    }

    @Override
    @Transactional("core_tx_mgr")
    public void addTags(UUID uuid, String ...tags) {
        try {
            jdbc.execute("{call public.fn_add_tags(?, ?)}", (CallableStatement stmt) -> {
                stmt.setObject(1, uuid);
                stmt.setArray(2, stmt.getConnection().createArrayOf("VARCHAR", tags));
                stmt.execute();
                return null;
            });
        } catch (Exception e) {
            mapFunctionExceptions(e);
        }
    }

    @Override
    @Transactional("core_tx_mgr")
    public void deleteTags(UUID uuid, String ...tags) {
        try {
            jdbc.execute("{call public.fn_remove_tags(?, ?)}", (CallableStatement stmt) -> {
                stmt.setObject(1, uuid);
                stmt.setArray(2, stmt.getConnection().createArrayOf("VARCHAR", tags));
                stmt.execute();
                return null;
            });
        } catch (Exception e) {
             mapFunctionExceptions(e);
        }
    }

    // The generics are just smoke and mirrors to have a method signature that returns the correct
    // type even though the method will always throw an exception
    private <T> T mapFunctionExceptions(Exception e) {
        if (e instanceof TransientDataAccessException) {
            throw new ResubmitException(e.getMessage());
        } else if (e instanceof UncategorizedSQLException) {
            if (e.getCause() != null && !e.getCause().getMessage().isEmpty()) {
                if (e.getCause().getMessage().contains("are not defined")) {
                    throw new NotFoundException(e.getCause().getMessage());
                }
            }
            throw (RuntimeException)e;

        } else if (e instanceof RuntimeException) {
            throw (RuntimeException)e;
        } else {
            throw new RuntimeException(e.getMessage());
        }
    }
}
