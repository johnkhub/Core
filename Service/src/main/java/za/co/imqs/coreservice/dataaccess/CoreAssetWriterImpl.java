package za.co.imqs.coreservice.dataaccess;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import za.co.imqs.coreservice.dataaccess.exception.AlreadyExistsException;
import za.co.imqs.coreservice.dataaccess.exception.NotFoundException;
import za.co.imqs.coreservice.model.CoreAsset;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    public CoreAssetWriterImpl(DataSource ds) {
        this.jdbc = new NamedParameterJdbcTemplate(ds);
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
                    jdbc.update(generateInsert("geoms", tGeoms).toString(), tGeoms);
                }
            } catch(Exception e) {
                throw exceptionMapper(e, a);
            }
        }
    }

    @Override
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
                    count += jdbc.update(generateUpdate("geoms", tGeoms).toString(), tGeoms);
                }
                if (count == 0)
                    throw new NotFoundException("Asset " + a.getAsset_id() + " does not exist");
            } catch (Exception e) {
                throw exceptionMapper(e, a);
            }
        }
    }

    @Override
    public void deleteAssets(List<UUID> uuid) {
        throw new UnsupportedOperationException("Deletion of asset not implemented");
    }

    @Override
    public void addExternalLink(UUID uuid, String externalIdType, String externalId) {
        jdbc.getJdbcTemplate().update("INSERT INTO asset_link (uuid,externalIdType,externalId) VALUES (?,?,?)", uuid.toString(), externalIdType, externalId);
    }

    @Override
    public void deleteExternalLink(UUID uuid, String externalIdType, String externalId) {
        jdbc.getJdbcTemplate().update("DELETE FROM asset_link WHERE uuid = ? externalIdType = ? AND externalId = ?", uuid.toString(), externalIdType, externalId);
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
            tGeoms.addValue("geom", asset.getGeometry(), Types.OTHER);
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
        final StringBuffer insert = new StringBuffer("INSERT INTO ").append(target).append("(");
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

    private RuntimeException exceptionMapper(Exception e, CoreAsset asset) {
        if (e instanceof org.springframework.dao.DuplicateKeyException) {
            return new AlreadyExistsException("Asset " +  asset.getAsset_id() + " already exists! ("+e.getMessage()+")");
        } else if (e instanceof RuntimeException) {
            return (RuntimeException)e;
        } else {
            return new RuntimeException(e);
        }
    }
}
