package za.co.imqs.coreservice.dataaccess;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import za.co.imqs.coreservice.dataaccess.exception.AlreadyExistsException;
import za.co.imqs.coreservice.dataaccess.exception.NotFoundException;
import za.co.imqs.coreservice.dataaccess.exception.ResubmitException;
import za.co.imqs.coreservice.dataaccess.exception.ValidationFailureException;
import za.co.imqs.coreservice.model.CoreAsset;
import za.co.imqs.coreservice.model.ORM;
import za.co.imqs.coreservice.model.Quantity;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static za.co.imqs.coreservice.WebMvcConfiguration.PROFILE_ADMIN;
import static za.co.imqs.coreservice.model.CoreAsset.CREATE;
import static za.co.imqs.coreservice.model.CoreAsset.UPDATE;
import static za.co.imqs.coreservice.model.ORM.getTableName;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_PRODUCTION;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_TEST;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/05
 */
@Slf4j
@Profile({PROFILE_PRODUCTION, PROFILE_TEST, PROFILE_ADMIN})
@Repository
public class CoreAssetWriterImpl implements CoreAssetWriter {
    private static final Set<String> EXCLUDED_GETTERS = Collections.unmodifiableSet(ORM.getReadMethods(CoreAsset.class));

    private final NamedParameterJdbcTemplate jdbc;
    private final Environment env;
    private final Meta meta;

    private final AtomicBoolean assetDirty = new AtomicBoolean(false);

    @Autowired
    public CoreAssetWriterImpl(
            @Qualifier("core_ds") DataSource ds,
            Environment env,
            TaskScheduler scheduler
    ) {
        this.jdbc = new NamedParameterJdbcTemplate(ds);
        this.env = env;
        this.meta = new MetaImpl(ds);
        scheduler.scheduleAtFixedRate(new ExecuteModTrackUpdate(), 3000);
    }

    // TODO: Retry
    // TODO: More elegant mapping
    // TODO: This will be very, very sloooooow
    @Override
    public void createAssets(List<CoreAsset> assets) {
        for (CoreAsset a : assets) {
            try {
                a.validate(CREATE);

                final MapSqlParameterSource tAsset = new MapSqlParameterSource();
                final MapSqlParameterSource tLocation = new MapSqlParameterSource();
                final MapSqlParameterSource tAssetIdentification = new MapSqlParameterSource();
                final MapSqlParameterSource tGeoms = new MapSqlParameterSource();
                final MapSqlParameterSource tAssetClassification = new MapSqlParameterSource();

                process(a, tAsset, tLocation, tAssetIdentification, tGeoms, tAssetClassification);
                if (tAsset.getValues().size() > 1) {
                    jdbc.update(generateInsert("asset", tAsset).toString(), tAsset);
                }
                if (tLocation.getValues().size() > 0) {
                    tLocation.addValue("asset_id", tAsset.getValue("asset_id"), tAsset.getSqlType("asset_id"));
                    jdbc.update(generateInsert("location", tLocation).toString(), tLocation);
                }
                if (tAssetIdentification.getValues().size() > 0) {
                    tAssetIdentification.addValue("asset_id", tAsset.getValue("asset_id"), tAsset.getSqlType("asset_id"));
                    jdbc.update(generateInsert("asset_identification", tAssetIdentification).toString(), tAssetIdentification);
                }
                if (tGeoms.getValues().size() > 0) {
                    tGeoms.addValue("asset_id", tAsset.getValue("asset_id"), tAsset.getSqlType("asset_id"));
                    jdbc.update("INSERT INTO geoms (asset_id, geom) VALUES (:asset_id, ST_GeomFromText(:geom, 4326))", tGeoms);
                }
                if (tAssetClassification.getValues().size() > 0) {
                    tAssetClassification.addValue("asset_id", tAsset.getValue("asset_id"), tAsset.getSqlType("asset_id"));
                    jdbc.update(generateInsert("asset_classification", tAssetClassification).toString(), tAssetClassification);
                }

                final MapSqlParameterSource tAssetExt = mapExtension(a.getAsset_id(), a);
                final String tableName = getTableName(a);
                if (!tAssetExt.getValues().isEmpty() && !tableName.equals("asset.a_tp_core")) {
                    jdbc.update(generateInsert(tableName, tAssetExt).toString(), tAssetExt);
                }
            } catch(Exception e) {
                throw exceptionMapperAsset(e, a.getAsset_id());
            }
        }
    }

    @Override
    @Transactional(transactionManager="core_tx_mgr", rollbackFor = Exception.class)
    public void updateAssets(List<CoreAsset> assets) {
        for (CoreAsset a : assets) {
            try {
                a.validate(UPDATE);

                final MapSqlParameterSource tAsset = new MapSqlParameterSource();
                final MapSqlParameterSource tLocation = new MapSqlParameterSource();
                final MapSqlParameterSource tAssetIdentification = new MapSqlParameterSource();
                final MapSqlParameterSource tGeoms = new MapSqlParameterSource();
                final MapSqlParameterSource tAssetClassification = new MapSqlParameterSource();

                process(a, tAsset, tLocation, tAssetIdentification, tGeoms, tAssetClassification);
                int count = 0;
                if (tAsset.getValues().size() > 1) {
                    count += jdbc.update(generateUpdate("asset", tAsset).toString(), tAsset);
                }
                if (tLocation.getValues().size() > 0) {
                    tLocation.addValue("asset_id", tAsset.getValue("asset_id"), tAsset.getSqlType("asset_id"));
                    count += jdbc.update(generateUpsert("location", tLocation).toString(), tLocation);
                }
                if (tAssetIdentification.getValues().size() > 0) {
                    tAssetIdentification.addValue("asset_id", tAsset.getValue("asset_id"), tAsset.getSqlType("asset_id"));
                    count += jdbc.update(generateUpsert("asset_identification", tAssetIdentification).toString(), tAssetIdentification);
                }
                if (tGeoms.getValues().size() > 0) {
                    tGeoms.addValue("asset_id", tAsset.getValue("asset_id"), tAsset.getSqlType("asset_id"));
                    final String sql = "INSERT INTO geoms (asset_id, geom) VALUES (:asset_id, ST_GeomFromText(:geom, 4326))"+
                            " ON CONFLICT(asset_id) DO "+
                            "UPDATE SET geom = EXCLUDED.geom";
                    count += jdbc.update(sql, tGeoms);
                }
                if (tAssetClassification.getValues().size() > 0) {
                    tAssetClassification.addValue("asset_id", tAsset.getValue("asset_id"), tAsset.getSqlType("asset_id"));
                    count += jdbc.update(generateUpsert("asset_classification", tAssetClassification).toString(), tAssetClassification);
                }

                final MapSqlParameterSource tAssetExt = mapExtension(a.getAsset_id(), a);
                final String tableName = getTableName(a);
                if (tAssetExt.getValues().size() > 1 && !tableName.equals("asset.a_tp_core")) {
                    count += jdbc.update(generateUpdate(getTableName(a), tAssetExt).toString(), tAssetExt);
                }

                if (count == 0)
                    throw new NotFoundException("Asset " + a.getAsset_id() + " does not exist");

            } catch (Exception e) {
                throw exceptionMapperAsset(e, a.getAsset_id());
            }
        }
    }

    @Override
    @Transactional(transactionManager="core_tx_mgr", rollbackFor = Exception.class)
    public void deleteAssets(List<UUID> uuids) {
        for (UUID uuid : uuids) {
            try {
                jdbc.getJdbcTemplate().update("UPDATE public.asset SET deactivated_at = now() WHERE asset_id = ?", uuid);
            } catch (Exception e) {
                throw exceptionMapperAsset(e, uuid);
            }
        }
    }

    @Override
    @Transactional(transactionManager="core_tx_mgr", rollbackFor = Exception.class)
    public void obliterateAssets(List<UUID> uuids) {
        final List<String> profiles = Arrays.asList(env.getActiveProfiles());
        if (profiles.contains(PROFILE_PRODUCTION)) {
            throw new RuntimeException("No way!");
        }

        jdbc.getJdbcTemplate().batchUpdate(
                "select public.fn_delete_asset(?)",
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setObject(1, uuids.get(i));
                    }

                    public int getBatchSize() {
                        return uuids.size();
                    }
                }
        );
    }

    @Override
    @Transactional(transactionManager="core_tx_mgr", rollbackFor = Exception.class)
    public void addExternalLink(UUID uuid, UUID externalIdType, String externalId) {
        try {
            jdbc.getJdbcTemplate().update("INSERT INTO asset_link (asset_id,external_Id_Type,external_Id) VALUES (?,?,?) ON CONFLICT DO NOTHING;", uuid, externalIdType, externalId);
        } catch (Exception e) {
            throw exceptionMapperExternalLink(e, uuid, externalId);
        }
    }

    @Override
    @Transactional(transactionManager="core_tx_mgr", rollbackFor = Exception.class)
    public void updateExternalLink(UUID uuid, UUID externalIdType, String externalId) {
        try {
            jdbc.getJdbcTemplate().update("UPDATE asset_link SET external_Id = ? WHERE asset_id = ? AND external_Id_Type = ?;", externalId, uuid, externalIdType);
        } catch (Exception e) {
            throw exceptionMapperExternalLink(e, uuid, externalId);
        }
    }

    @Override
    @Transactional(transactionManager="core_tx_mgr", rollbackFor = Exception.class)
    public void deleteExternalLink(UUID uuid, UUID externalIdType, String externalId) {
        try {
            jdbc.getJdbcTemplate().update("DELETE FROM asset_link WHERE asset_id = ? AND external_Id_Type = ? AND external_Id = ?", uuid, externalIdType, externalId);
        } catch (Exception e) {
            throw exceptionMapperExternalLink(e, uuid, externalId);
        }
    }


    @Override
    @Transactional(transactionManager="core_tx_mgr", rollbackFor = Exception.class)
    public void addToGrouping(UUID uuid, UUID groupingIdType, String groupingId) {
        try {
            jdbc.getJdbcTemplate().update("INSERT INTO asset_grouping (asset_id,grouping_Id_Type,grouping_Id) VALUES (?,?,?) ON CONFLICT DO NOTHING;", uuid, groupingIdType, groupingId);
        } catch (Exception e) {
            throw exceptionMapperGrouping(e, uuid, groupingId);
        }
    }

    @Override
    @Transactional(transactionManager="core_tx_mgr", rollbackFor = Exception.class)
    public void updateGrouping(UUID uuid, UUID groupingIdType, String groupingId) {
        try {
            jdbc.getJdbcTemplate().update("UPDATE asset_grouping SET grouping_Id = ? WHERE asset_id = ? AND grouping_Id_Type = ?;", groupingId, uuid, groupingIdType);
        } catch (Exception e) {
            throw exceptionMapperGrouping(e, uuid, groupingId);
        }
    }

    @Override
    @Transactional(transactionManager="core_tx_mgr", rollbackFor = Exception.class)
    public void deleteFromGrouping(UUID uuid, UUID groupingIdType, String groupingId) {
        try {
            jdbc.getJdbcTemplate().update("DELETE FROM asset_grouping WHERE asset_id = ? AND grouping_Id_Type = ? AND grouping_Id = ?", uuid, groupingIdType, groupingId);
        } catch (Exception e) {
            throw exceptionMapperGrouping(e, uuid, groupingId);
        }
    }



    @Override
    @Transactional(transactionManager="core_tx_mgr", rollbackFor = Exception.class)
    public void linkAssetToLandParcel(UUID asset, UUID to) {
        try {
            jdbc.getJdbcTemplate().update(
                    "INSERT INTO asset.asset_landparcel (asset_id,landparcel_asset_id) VALUES (?,?) " +
                            "ON CONFLICT  (asset_id,landparcel_asset_id) DO NOTHING;",
                    asset, to);
        } catch (Exception e) {
            throw exceptionMapperLandparcelLink(e);
        }
    }

    @Override
    @Transactional(transactionManager="core_tx_mgr", rollbackFor = Exception.class)
    public void unlinkAssetFromLandParcel(UUID asset, UUID from) {
        try {
            jdbc.getJdbcTemplate().update("DELETE FROM asset.asset_landparcel WHERE asset_id = ? AND landparcel_asset_id = ?", asset, from);
        } catch (Exception e) {
            throw exceptionMapperLandparcelLink(e);
        }
    }

    private void process(
            CoreAsset asset,
            MapSqlParameterSource tAsset,
            MapSqlParameterSource tLocation,
            MapSqlParameterSource tAssetIdentification,
            MapSqlParameterSource tGeoms,
            MapSqlParameterSource tAssetClassification
    ) {
        if (asset.getAdm_path() != null) {
            tAsset.addValue("adm_path", asset.getAdm_path(), Types.OTHER);
        }
        if (asset.getFunc_loc_path() != null) {
            tAsset.addValue("func_loc_path", asset.getFunc_loc_path(), Types.OTHER);
        }
        if (asset.getAsset_id() != null) {
            tAsset.addValue("asset_id", asset.getAsset_id(), Types.OTHER);
        }
        if (asset.getAsset_type_code() != null) {
            tAsset.addValue("asset_type_code", asset.getAsset_type_code(), Types.VARCHAR);
        }
        if (asset.getCreation_date() != null) {
            tAsset.addValue("creation_date", asset.getCreation_date(), Types.TIMESTAMP);
        }
        if (asset.getDeactivated_at() != null) {
            tAsset.addValue("deactivated_at", asset.getDeactivated_at(), Types.TIMESTAMP);
        }
        if (asset.getBarcode() != null) {
            tAssetIdentification.addValue("barcode", asset.getBarcode(), Types.VARCHAR);
        }
        if (asset.getGeom() != null) {
            tGeoms.addValue("geom", asset.getGeom(), Types.VARCHAR);
        }
        if (asset.getLatitude() != null) {
            tLocation.addValue("latitude", asset.getLatitude(), Types.DECIMAL);
        }
        if (asset.getLongitude() != null) {
            tLocation.addValue("longitude", asset.getLongitude(), Types.DECIMAL);
        }
        if (asset.getAddress() != null) {
            tLocation.addValue("address", asset.getAddress(), Types.VARCHAR);
        }

        if (asset.getDistrict_code() != null) {
            tLocation.addValue("district_code", asset.getDistrict_code(), Types.VARCHAR);
        }
        if (asset.getMunicipality_code() != null) {
            tLocation.addValue("municipality_code", asset.getMunicipality_code(), Types.VARCHAR);
        }
        if (asset.getRegion_code() != null) {
            tLocation.addValue("region_code", asset.getRegion_code(), Types.VARCHAR);
        }
        if (asset.getSuburb_code() != null) {
            tLocation.addValue("suburb_code", asset.getSuburb_code(), Types.VARCHAR);
        }
        if (asset.getTown_code() != null) {
            tLocation.addValue("town_code", asset.getTown_code(), Types.VARCHAR);
        }
        if (asset.getWard_code() != null) {
            tLocation.addValue("ward_code", asset.getWard_code(), Types.VARCHAR);
        }

        if (asset.getName() != null) {
            tAsset.addValue("name", asset.getName(), Types.VARCHAR);
        }
        if (asset.getCode() != null) {
            tAsset.addValue("code", asset.getCode(), Types.VARCHAR);
        }
        if (asset.getSerial_number() != null) {
            tAssetIdentification.addValue("serial_number", asset.getSerial_number(), Types.VARCHAR);
        }

        if (asset.getResponsible_dept_code() != null) {
            tAssetClassification.addValue("responsible_dept_code", asset.getResponsible_dept_code(), Types.VARCHAR);
        }
        if (asset.getIs_owned() != null) {
            tAssetClassification.addValue("is_owned", asset.getIs_owned(), Types.BOOLEAN);
        }
        if (asset.getDescription() != null) {
            tAsset.addValue("description", asset.getDescription(), Types.VARCHAR);
        }
        updateModTrack();
    }

    @Override
    @Transactional(transactionManager="core_tx_mgr", rollbackFor = Exception.class)
    public void addLinkedData(String table, String field, UUID assetId, String value) { // TODO modify to handle multiple fields
        try {
            assertValidLinkTable(table);

            jdbc.getJdbcTemplate().update(
                    String.format(
                            "INSERT INTO %s (asset_id, %s) VALUES (?,?)",
                            table, field
                    ),
                    assetId, value);
        } catch (Exception e) {
            throw exceptionMapperLinkedData(e, assetId, field);
        }
    }

    @Override
    @Transactional(transactionManager="core_tx_mgr", rollbackFor = Exception.class)
    public void updateLinkedData(String table, String field, UUID assetId, String value) { // TODO modify to handle multiple fields
        try {
            assertValidLinkTable(table);

            jdbc.getJdbcTemplate().update(
                    String.format(
                            "UPDATE %s SET %s = ? WHERE asset_id = ?",
                            table, field
                    ),
                    value, assetId);
        } catch (Exception e) {
            throw exceptionMapperLinkedData(e, assetId, field);
        }
    }

    @Override
    @Transactional(transactionManager="core_tx_mgr", rollbackFor = Exception.class)
    public void deleteLinkedData(String table, String field, UUID assetId) { // TODO modify to handle multiple fields
        try {
            assertValidLinkTable(table);

            jdbc.getJdbcTemplate().update(
                    String.format(
                            "DELETE FROM %s WHERE asset_id = ?",
                            table
                    ),
                    assetId);
        } catch (Exception e) {
            throw exceptionMapperLinkedData(e, assetId, field);
        }
    }


    @Override
    @Transactional(transactionManager="core_tx_mgr", rollbackFor = Exception.class)
    public void addQuantity(Quantity quantity) {
        try {
            jdbc.update("INSERT INTO public.quantity (asset_id,unit_code,num_units,name) VALUES (:asset_id,:unit_code,:num_units,:name)", new BeanPropertySqlParameterSource(quantity));
        } catch (Exception e) {
            throw exceptionMapperQuantity(e, quantity.getAsset_id(), quantity.getName());
        }
    }

    @Override
    @Transactional(transactionManager="core_tx_mgr", rollbackFor = Exception.class)
    public void updateQuantity(Quantity quantity) {
        try {
            final StringBuilder sql = new StringBuilder("UPDATE public.quantity ");
            // TODO generateInsert and generateUpsert can be simplified by using the pattern below
            // Replace the sql.append with a lambda that manipulates the various StringBuffers
            // Also better to use SqlParameterSource interface as opposed to MapSqlParameterSource
            // TODO much of ORM.mapToSql can also be replaced by this pattern

            final BeanPropertySqlParameterSource params = new BeanPropertySqlParameterSource(quantity);
            final Set<String> exclude = new HashSet<>(Arrays.asList("asset_id","class","name"));
            sql.append("\nSET ");
            for (String name : params.getParameterNames()) {
                if (!exclude.contains(name) && params.getValue(name) != null) {
                    sql.append(name).append(" = :").append(name).append(",");
                }
            }
            sql.deleteCharAt(sql.length()-1);

            sql.append("\nWHERE asset_id = :asset_id AND name = :name");
            jdbc.update(sql.toString(), params);

        } catch (Exception e) {
            throw exceptionMapperQuantity(e, quantity.getAsset_id(), quantity.getName());
        }
    }

    @Override
    @Transactional(transactionManager="core_tx_mgr", rollbackFor = Exception.class)
    public void deleteQuantity(UUID uuid, String name) {
        try {
            jdbc.getJdbcTemplate().update("DELETE FROM public.quantity WHERE asset_id = ? and name = ?", uuid, name);
        } catch (Exception e) {
            throw exceptionMapperQuantity(e, uuid, name);
        }
    }

    // TODO: this could make use of update T set F = coalesce(F, new value), which will simplify the code significantly
    // as we don't have to dynamically exclude fields. It will also allow us to make use of batches!!!!
    private StringBuffer generateUpdate(String target, MapSqlParameterSource map) {
        final StringBuffer update = new StringBuffer("UPDATE ").append(target).append(" SET ");
        for (Map.Entry<String,Object> e : map.getValues().entrySet()) {
            if (!e.getKey().equals("asset_id")) {
                update.append(e.getKey()).append("=").append(":").append(e.getKey()).append(",");
            }
        }
        update.delete(update.length()-1,update.length());
        update.append(" WHERE asset_id = :asset_id;");
        return update;
    }

    private StringBuffer generateUpsert(String target, MapSqlParameterSource map) {
        final StringBuffer insert = generateInsert(target, map);

        insert.append("\n  ON CONFLICT(asset_id) DO \n");

        insert.append("UPDATE SET ");
        for (Map.Entry<String,Object> e : map.getValues().entrySet()) {
            if (!e.getKey().equals("asset_id")) {
                insert.append(e.getKey()).append("=").append("EXCLUDED.").append(e.getKey()).append(",");
            }
        }
        insert.delete(insert.length()-1, insert.length());
        return insert;
    }

    // TODO: this could make use of the DEFAULT feature, to insert the default value into the non-specified columns which will simplify the code significantly
    // as we don't have to dynamically exclude fields. It will also allow us to make use of batches!!!!
    private StringBuffer generateInsert(String target, MapSqlParameterSource map) {
        final StringBuffer insert = new StringBuffer("INSERT INTO ").append(target).append(" (");
        for (Map.Entry<String,Object> e : map.getValues().entrySet()) {
            insert.append(e.getKey()).append(",");
        }
        insert.replace(insert.length()-1, insert.length(), ")");
        insert.append(" VALUES (");
        for (Map.Entry<String,Object> e : map.getValues().entrySet()) {
            insert.append(":").append(e.getKey()).append(",");
        }
        insert.replace(insert.length()-1, insert.length(), ")");
        return insert;
    }

    private RuntimeException exceptionMapperAsset(Exception e, UUID asset_id) {
        return exceptionMapper(e, "Asset " + asset_id + " already exists! (" + e.getMessage() + ")");
    }

    private RuntimeException exceptionMapperExternalLink(Exception e, UUID asset, String link) {
        return exceptionMapper(e, "Asset Link " + asset + ":" + link + " already exists! (" + e.getMessage() + ")");
    }

    private RuntimeException exceptionMapperLandparcelLink(Exception e) {
        return exceptionMapper(e, "");
    }

    private RuntimeException exceptionMapperGrouping(Exception e, UUID asset, String link) {
       return exceptionMapper(e, "");
    }

    private RuntimeException exceptionMapperLinkedData(Exception e, UUID asset, String field) {
        return exceptionMapper(e, "Field " + field + " already exists for asset " + asset +"! (" + e.getMessage() + ")");
    }

    private RuntimeException exceptionMapperQuantity(Exception e, UUID asset, String field) {
        return exceptionMapper(e, "Quantity " + field + " already exists for asset " + asset +"! (" + e.getMessage() + ")");
    }

    private RuntimeException exceptionMapper(Exception e, String msg) {
        if (e instanceof org.springframework.dao.DuplicateKeyException) {
            return new AlreadyExistsException(msg);
        } else  if (e instanceof DataIntegrityViolationException) {
            return new ValidationFailureException(e.getMessage());
        } else if (e instanceof TransientDataAccessException) {
            throw new ResubmitException(e.getMessage());
        } else if (e instanceof RuntimeException) {
            return (RuntimeException)e;
        } else {
            return new RuntimeException(e);
        }
    }

    private <T extends CoreAsset> MapSqlParameterSource mapExtension(UUID assetId, T asset) throws Exception {
        final MapSqlParameterSource parameters = ORM.mapToSql(asset, EXCLUDED_GETTERS);
        parameters.addValue("asset_id", assetId, Types.OTHER);
        return parameters;
    }

    private void assertValidLinkTable(String table) {
        final String[] fqn = table.split("\\.");
        if (!meta.userSchemas().contains(fqn[0])) throw new IllegalArgumentException(String.format("%s is not a valid user schema", fqn[0]));
        if (!fqn[1].endsWith("_link")) throw new IllegalArgumentException(String.format("%s is not a valid field link table", fqn[1]));
    }

    private void updateModTrack() {
        assetDirty.set(true);
    }

    private class ExecuteModTrackUpdate implements Runnable {
        @Override
        public void run() {
            boolean go = assetDirty.compareAndSet(true, false);
            try {
                if (go) {
                    jdbc.getJdbcTemplate().update("UPDATE modtrack_tables SET stamp = stamp+1 WHERE tablename = 'asset'");
                }
            } catch (Exception e) {
                log.error("Failed to update mod_track table. To recover. execute UPDATE modtrack_tables SET stamp = stamp+1 WHERE tablename = 'asset' manually.", e);
            }
        }
    }
}
