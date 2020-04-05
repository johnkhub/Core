package za.co.imqs.coreservice.dataaccess;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Types;
import java.util.Arrays;
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
    public List<String> getTagsFor(UUID uuid) {
        return jdbc.queryForObject(
                "SELECT tags FROM public.asset_tags WHERE asset_id = ?",
                (rs, i) -> {
                   final String[] t = (String[])rs.getArray("tags").getArray();
                   return Arrays.asList(t);
                },
                uuid);
    }

    @Override
    public boolean hasTag(UUID uuid, String tag) {
        return jdbc.execute("{? = call public.fn_has_tag(?, ?)}", (CallableStatement stmt) -> {
            stmt.registerOutParameter(1, Types.BOOLEAN);
            stmt.setObject(1, uuid);
            stmt.setString(2, tag);
            stmt.execute();
            return stmt.getBoolean(1);
        });
    }

    @Override
    @Transactional
    public void addTags(UUID uuid, String ...tags) {
        jdbc.execute("{? = call public.fn_add_tags(?, ?)}", (CallableStatement stmt) -> {
            stmt.registerOutParameter(1, Types.BOOLEAN);
            stmt.setObject(1, uuid);
            stmt.setArray(2, stmt.getConnection().createArrayOf("VARCHAR", tags));
            stmt.execute();
            return null;
        });
    }

    @Override
    @Transactional
    public void deleteTags(UUID uuid, String ...tags) {
        jdbc.execute("{? = call public.fn_remove_tags(?, ?)}", (CallableStatement stmt) -> {
            stmt.registerOutParameter(1, Types.BOOLEAN);
            stmt.setObject(1, uuid);
            stmt.setArray(2, stmt.getConnection().createArrayOf("VARCHAR", tags));
            stmt.execute();
            return null;
        });
    }
}
