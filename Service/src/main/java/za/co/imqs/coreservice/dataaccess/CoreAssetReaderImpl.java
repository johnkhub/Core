package za.co.imqs.coreservice.dataaccess;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import za.co.imqs.coreservice.dataaccess.exception.NotFoundException;
import za.co.imqs.coreservice.dataaccess.exception.ResubmitException;
import za.co.imqs.coreservice.model.CoreAsset;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;

import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_TEST;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/03/06
 *
 * THIS IMPLEMENTATION IS NOT PRODUCTION READY AND IS INTENDED FOR TESTING
 */
@Profile(PROFILE_TEST)
@Repository
public class CoreAssetReaderImpl implements CoreAssetReader {
    private final JdbcTemplate jdbc;

    @Autowired
    public CoreAssetReaderImpl(
            @Qualifier("core_ds") DataSource ds
    ) {
        this.jdbc = new JdbcTemplate(ds);
    }

    @Override
    public CoreAsset getAsset(UUID uuid) {
        try {
            return jdbc.queryForObject(
                    "SELECT asset.*, " +
                            "location.latitude, location.longitude ," +
                            "ST_AsText(geoms.geom) AS geom, " +
                            "asset_identification.barcode, " +
                            "asset_identification.serial_number, " +
                            "asset_classification.responsible_dept_code, " +
                            "asset_classification.is_owned " +
                            "FROM " +
                            "   asset " +
                            "LEFT JOIN location ON asset.asset_id = location.asset_id " +
                            "LEFT JOIN geoms ON asset.asset_id = geoms.asset_id " +
                            "LEFT JOIN asset_identification ON asset.asset_id = asset_identification.asset_id " +
                            "LEFT JOIN asset_classification ON asset.asset_id = asset_classification.asset_id " +
                            "WHERE asset.asset_id = ?",
                    (ResultSet rs, int rowNum)-> {
                        final CoreAsset asset = new CoreAsset();

                        asset.setAdm_path(rs.getString("adm_path"));
                        asset.setAsset_id(UUID.fromString(rs.getString("asset_id")));
                        asset.setAsset_type_code(rs.getString("asset_type_code"));
                        asset.setBarcode(rs.getString("barcode"));
                        asset.setCode(rs.getString("code"));
                        asset.setCreation_date(rs.getTimestamp("creation_date"));
                        asset.setDeactivated_at(rs.getTimestamp("deactivated_at"));
                        asset.setFunc_loc_path(rs.getString("func_loc_path"));
                        asset.setGeometry(rs.getString("geom"));
                        asset.setLatitude(rs.getBigDecimal("latitude"));
                        asset.setLongitude(rs.getBigDecimal("longitude"));
                        asset.setName(rs.getString("name"));
                        asset.setReference_count(rs.getInt("reference_count"));
                        asset.setSerial_number(rs.getString("serial_number"));

                        asset.setResponsible_dept_code(rs.getString("responsible_dept_code"));
                        asset.setIs_owned(rs.getBoolean("is_owned"));
                        return asset;
                    },
                    uuid);
        } catch (TransientDataAccessException e) {
            throw new ResubmitException(e.getMessage());
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Asset "+ uuid.toString() + " not found.");
        }
    }

    @Override
    public List<String> getExternalLinks(UUID uuid, UUID external_id_type) {
        try {
            return jdbc.queryForList("SELECT external_id FROM asset_link WHERE asset_id = ? AND external_id_type = ? ", String.class, uuid, external_id_type);
        } catch (TransientDataAccessException e) {
            throw new ResubmitException(e.getMessage());
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(String.format("No link to external_id_type %s  for asset %s", external_id_type, uuid.toString()));
        }
    }
}
