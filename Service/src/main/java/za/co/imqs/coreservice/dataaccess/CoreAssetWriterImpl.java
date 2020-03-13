package za.co.imqs.coreservice.dataaccess;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import za.co.imqs.coreservice.dataaccess.exception.AlreadyExistsException;
import za.co.imqs.coreservice.dataaccess.exception.NotFoundException;
import za.co.imqs.coreservice.dataaccess.exception.ValidationFailureException;
import za.co.imqs.coreservice.model.CoreAsset;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_PRODUCTION;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/05
 */
@Slf4j
@Repository
public class CoreAssetWriterImpl implements CoreAssetWriter {

    private final NamedParameterJdbcTemplate jdbc;
    private final Environment env;

    @Autowired
    public CoreAssetWriterImpl(
            @Qualifier("core_ds") DataSource ds,
            Environment env
    ) {
        this.jdbc = new NamedParameterJdbcTemplate(ds);
        this.env = env;
    }

    // TODO: Retry
    // TODO: Transactions
    // TODO: More elegant mapping
    @Override
    public void createAssets(List<CoreAsset> assets) {
        for (CoreAsset a : assets) {
            try {
                a.validate();

                final MapSqlParameterSource tAsset = new MapSqlParameterSource();
                final MapSqlParameterSource tLocation = new MapSqlParameterSource();
                final MapSqlParameterSource tAssetIdentification = new MapSqlParameterSource();
                final MapSqlParameterSource tGeoms = new MapSqlParameterSource();

                process(a, tAsset, tLocation, tAssetIdentification, tGeoms);
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
            } catch(Exception e) {
                throw exceptionMapperAsset(e, a);
            }
        }
    }

    @Override
    @Transactional("core_tx_mgr")
    public void updateAssets(List<CoreAsset> assets) {
        for (CoreAsset a : assets) {
            try {
                a.validate();

                final MapSqlParameterSource tAsset = new MapSqlParameterSource();
                final MapSqlParameterSource tLocation = new MapSqlParameterSource();
                final MapSqlParameterSource tAssetIdentification = new MapSqlParameterSource();
                final MapSqlParameterSource tGeoms = new MapSqlParameterSource();

                process(a, tAsset, tLocation, tAssetIdentification, tGeoms);
                int count = 0;
                if (tAsset.getValues().size() > 1) {
                    count += jdbc.update(generateUpdate("asset", tAsset).toString(), tAsset);
                }
                if (tLocation.getValues().size() > 0) {
                    tLocation.addValue("asset_id", tAsset.getValue("asset_id"), tAsset.getSqlType("asset_id"));
                    count += jdbc.update(generateUpdate("location", tLocation).toString(), tLocation);
                }
                if (tAssetIdentification.getValues().size() > 0) {
                    tAssetIdentification.addValue("asset_id", tAsset.getValue("asset_id"), tAsset.getSqlType("asset_id"));
                    count += jdbc.update(generateUpdate("asset_identification", tAssetIdentification).toString(), tAssetIdentification);
                }
                if (tGeoms.getValues().size() > 0) {
                    tGeoms.addValue("asset_id", tAsset.getValue("asset_id"), tAsset.getSqlType("asset_id"));
                    //count += jdbc.update(generateUpdate("geoms", tGeoms).toString(), tGeoms);
                    count += jdbc.update("UPDATE geoms SET geom = ST_GeomFromText(:geom, 4326) WHERE asset_id = :asset_id", tGeoms);
                }
                if (count == 0)
                    throw new NotFoundException("Asset " + a.getAsset_id() + " does not exist");
            } catch (Exception e) {
                throw exceptionMapperAsset(e, a);
            }
        }
    }

    @Override
    @Transactional("core_tx_mgr")
    public void deleteAssets(List<UUID> uuid) {
        throw new UnsupportedOperationException("Deletion of asset not implemented");
    }


    @Override
    @Transactional("core_tx_mgr")
    public void obliterateAssets(List<UUID> uuids) {
        final List<String> profiles = Arrays.asList(env.getActiveProfiles());
        if (profiles.contains(PROFILE_PRODUCTION)) {
            throw new RuntimeException("No way!");
        }

        for (UUID uuid : uuids) {
            jdbc.getJdbcTemplate().update("DELETE FROM asset_link WHERE asset_id=?", uuid);
            jdbc.getJdbcTemplate().update("DELETE FROM location WHERE asset_id=?", uuid);
            jdbc.getJdbcTemplate().update("DELETE FROM geoms WHERE asset_id=?", uuid);
            jdbc.getJdbcTemplate().update("DELETE FROM asset_identification WHERE asset_id=?", uuid);
            jdbc.getJdbcTemplate().update("DELETE FROM asset WHERE asset_id=?", uuid);
        }
    }

    @Override
    @Transactional("core_tx_mgr")
    public void addExternalLink(UUID uuid, UUID externalIdType, String externalId) {
        try {
            jdbc.getJdbcTemplate().update("INSERT INTO asset_link (asset_id,external_Id_Type,external_Id) VALUES (?,?,?)", uuid, externalIdType, externalId);
        } catch (Exception e) {
            throw exceptionMapperExternalLink(e, uuid, externalId);
        }
    }

    @Override
    @Transactional("core_tx_mgr")
    public void deleteExternalLink(UUID uuid, UUID externalIdType, String externalId) {
        try {
            jdbc.getJdbcTemplate().update("DELETE FROM asset_link WHERE asset_id = ? AND external_Id_Type = ? AND external_Id = ?", uuid, externalIdType, externalId);
        } catch (Exception e) {
            throw exceptionMapperExternalLink(e, uuid, externalId);
        }
    }

    private void process(
            CoreAsset asset,
            MapSqlParameterSource tAsset,
            MapSqlParameterSource tLocation,
            MapSqlParameterSource tAssetIdentification,
            MapSqlParameterSource tGeoms
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
        if (asset.getGeometry() != null) {
            tGeoms.addValue("geom", asset.getGeometry(), Types.VARCHAR);
        }
        if (asset.getLatitude() != null) {
            tLocation.addValue("latitude", asset.getLatitude(), Types.DECIMAL);
        }
        if (asset.getLongitude() != null) {
            tLocation.addValue("longitude", asset.getLongitude(), Types.DECIMAL);
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
    }

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

    private RuntimeException exceptionMapperAsset(Exception e, CoreAsset asset) {
        if (e instanceof org.springframework.dao.DuplicateKeyException) {
            return new AlreadyExistsException("Asset " + asset.getAsset_id() + " already exists! (" + e.getMessage() + ")");
        } else if (e instanceof DataIntegrityViolationException) {
            return new ValidationFailureException(e.getMessage());
        } else if (e instanceof RuntimeException) {
            return (RuntimeException)e;
        } else {
            return new RuntimeException(e);
        }
    }

    private RuntimeException exceptionMapperExternalLink(Exception e, UUID asset, String link) {
        if (e instanceof org.springframework.dao.DuplicateKeyException) {
            return new AlreadyExistsException("Asset Link " + asset + ":" + link + " already exists! (" + e.getMessage() + ")");
        } else if (e instanceof DataIntegrityViolationException) {
            return new ValidationFailureException(e.getMessage());
        } else if (e instanceof RuntimeException) {
            return (RuntimeException)e;
        } else {
            return new RuntimeException(e);
        }
    }
}
