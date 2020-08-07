package za.co.imqs.coreservice.dataaccess;

import filter.FilterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import za.co.imqs.coreservice.dataaccess.exception.NotFoundException;
import za.co.imqs.coreservice.dataaccess.exception.ResubmitException;
import za.co.imqs.coreservice.dataaccess.exception.ValidationFailureException;
import za.co.imqs.coreservice.dto.AssetExternalLinkTypeDto;
import za.co.imqs.coreservice.model.CoreAsset;
import za.co.imqs.coreservice.model.ORM;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;

import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_PRODUCTION;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_TEST;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/03/06
 *
 */
@Profile({PROFILE_PRODUCTION, PROFILE_TEST})
@Repository
public class CoreAssetReaderImpl implements CoreAssetReader {
    // TODO: We probably need to base our repo layer on a view that is client specific?

    private static final String SELECT_ASSET = "SELECT asset.*, " +
            "location.latitude, location.longitude, location.address," +
            "location.region_code, location.district_code, location.municipality_code," +
            "location.town_code, location.suburb_code, location.ward_code," +
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
            "LEFT JOIN asset_classification ON asset.asset_id = asset_classification.asset_id ";

    private static final String SELECT_ASSET_INCL_DEPT_TREE = "SELECT asset.*, " +
            "location.latitude, location.longitude, location.address," +
            "location.region_code, location.district_code, location.municipality_code," +
            "location.town_code, location.suburb_code, location.ward_code," +
            "ST_AsText(geoms.geom) AS geom, " +
            "asset_identification.barcode, " +
            "asset_identification.serial_number, " +
            "asset_classification.responsible_dept_code, " +
            "asset_classification.is_owned, " +
            "dtpw.ref_client_department.responsible_dept_classif " +
            "FROM " +
            "   asset " +
            "LEFT JOIN location ON asset.asset_id = location.asset_id " +
            "LEFT JOIN geoms ON asset.asset_id = geoms.asset_id " +
            "LEFT JOIN asset_identification ON asset.asset_id = asset_identification.asset_id " +
            "LEFT JOIN asset_classification ON asset.asset_id = asset_classification.asset_id " +
            "JOIN dtpw.ref_client_department ON asset_classification.responsible_dept_code = dtpw.ref_client_department.k ";

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
            return jdbc.queryForObject(SELECT_ASSET + "WHERE asset.asset_id = ?", MAPPER, uuid);
        } catch (TransientDataAccessException e) {
            throw new ResubmitException(e.getMessage());
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Asset "+ uuid.toString() + " not found.");
        }
    }


    @Override
    public CoreAsset getAssetByFuncLocPath(String path) {
       return getAssetByXXPath("func_loc_path", path);
    }

    @Override
    public CoreAsset getAssetByXXPath(String pathName, String value) {
        try {
            return jdbc.queryForObject(
                    String.format(SELECT_ASSET + "WHERE asset.%s = text2ltree(?)", pathName),
                    MAPPER, value);
        } catch (TransientDataAccessException e) {
            throw new ResubmitException(e.getMessage());
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Asset where "+ pathName + " = " + value + " not found.");
        }
    }

    @Override
    public CoreAsset getAssetByExternalId(String externalType, String externalId) {
        try {
            return jdbc.queryForObject(
                    SELECT_ASSET+
                        "JOIN asset_link ON asset_link.asset_id = asset.asset_id " +
                        "WHERE asset_link.external_id_type = uuid(?) AND asset_link.external_id = ?",
                    MAPPER, externalType, externalId);
        } catch (TransientDataAccessException e) {
            throw new ResubmitException(e.getMessage());
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(String.format("Asset where external id of type %s = %s not found", externalType, externalId));
        }
    }

    @Override
    public List<CoreAsset> getAssetByFilter(FilterBuilder filter) {
        final String sql = filter.build();


        // TODO we obviously can't continue having logic based on explicit user specific field names
        try {
            return jdbc.query(
                    (filter.getFields().contains("responsible_dept_classif") ? SELECT_ASSET_INCL_DEPT_TREE : SELECT_ASSET) + "WHERE " + sql
                    , MAPPER);
        } catch (TransientDataAccessException e) {
            throw new ResubmitException(e.getMessage());
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Asset matching "+ sql + " not found.");
        } catch (BadSqlGrammarException b) {
            throw new ValidationFailureException("Bad query syntax." + b.getMessage(), b);
        }
    }

    @Override
    public String getExternalLink(UUID uuid, UUID external_id_type) {
        try {
            return jdbc.queryForObject("SELECT external_id FROM asset_link WHERE asset_id = ? AND external_id_type = ? ", String.class, uuid, external_id_type);
        } catch (TransientDataAccessException e) {
            throw new ResubmitException(e.getMessage());
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(String.format("No link to external_id_type %s  for asset %s", external_id_type, uuid.toString()));
        }
    }

    @Override
    public List<AssetExternalLinkTypeDto> getExternalLinkTypes() {
        try {
            return jdbc.query("SELECT * FROM public.external_id_type",
                    (rs,i)->{
                        final AssetExternalLinkTypeDto l = new AssetExternalLinkTypeDto();
                        l.setType_id(UUID.fromString(rs.getString("type_id")));
                        l.setDescription(rs.getString("description"));
                        l.setName(rs.getString("name"));
                        return l;
                    });
        } catch (TransientDataAccessException e) {
            throw new ResubmitException(e.getMessage());
        }
    }

    @Override
    public String getGrouping(UUID uuid, UUID grouping_id_type) {
        try {
            return jdbc.queryForObject("SELECT grouping_id FROM asset_grouping WHERE asset_id = ? AND grouping_id_type = ? ", String.class, uuid, grouping_id_type);
        } catch (TransientDataAccessException e) {
            throw new ResubmitException(e.getMessage());
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(String.format("No link to grouping_id_type %s  for asset %s", grouping_id_type, uuid.toString()));
        }
    }

    @Override
    public List<AssetExternalLinkTypeDto> getGroupingTypes() {
        try {
            return jdbc.query("SELECT * FROM public.grouping_id_type",
                    (rs,i)->{
                        final AssetExternalLinkTypeDto l = new AssetExternalLinkTypeDto();
                        l.setType_id(UUID.fromString(rs.getString("type_id")));
                        l.setDescription(rs.getString("description"));
                        l.setName(rs.getString("name"));
                        return l;
                    });
        } catch (TransientDataAccessException e) {
            throw new ResubmitException(e.getMessage());
        }
    }

    @Override
    public List<CoreAsset> getAssetsByGroupingId(String groupingType, String groupingId) {
        try {
            return jdbc.query(
                    SELECT_ASSET+
                            "JOIN asset_grouping ON asset_grouping.asset_id = asset.asset_id " +
                            "WHERE asset_grouping.grouping_id_type = uuid(?) AND asset_grouping.grouping_id = ?",
                    MAPPER, groupingType, groupingId);
        } catch (TransientDataAccessException e) {
            throw new ResubmitException(e.getMessage());
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(String.format("Asset where grouping id of type %s = %s not found", groupingType, groupingId));
        }
    }


    @Override
    public List<UUID> getAssetsLinkedToLandParcel(UUID landparcel) {
        try {
            return jdbc.queryForList("SELECT asset_id FROM asset.asset_landparcel WHERE landparcel_asset_id=?", UUID.class, landparcel);
        } catch (TransientDataAccessException e) {
            throw new ResubmitException(e.getMessage());
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(String.format("No assets linked to landparcel %s", landparcel.toString()));
        }
    }

    private static final RowMapper<CoreAsset> MAPPER =
        (ResultSet rs, int i) -> {
            //final CoreAsset asset = ORM.modelFactory(rs.getString("asset_type_code"));
            final CoreAsset asset = new CoreAsset();
            ORM.populateFromResultSet(rs, asset);
            return asset;
        };

}
