package za.co.imqs.coreservice.dataaccess;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.jdbc.core.JdbcTemplate;
import za.co.imqs.coreservice.dataaccess.exception.AlreadyExistsException;
import za.co.imqs.coreservice.dataaccess.exception.NotFoundException;
import za.co.imqs.coreservice.dataaccess.exception.NotPermittedException;
import za.co.imqs.coreservice.dataaccess.exception.ValidationFailureException;
import za.co.imqs.coreservice.model.CoreAsset;
import za.co.imqs.libimqs.dbutils.HikariCPClientConfigDatasourceHelper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/06
 */
public class CoreAssetWriterImplTest {
    private static final UUID THE_ASSET_ID = UUID.fromString("46514cb4-c4a1-4ef2-a76c-c2b16f4cdbaa");

    private final JdbcTemplate jdbc;

    @Rule
    public ExpectedException expect = ExpectedException.none();

    public CoreAssetWriterImplTest() {
        this.jdbc = new JdbcTemplate(
                HikariCPClientConfigDatasourceHelper.getDefaultDataSource(
                        "jdbc:postgresql://localhost:5432/CoreFrank","postgres","1mq5p@55w0rd"
                )
        );
    }

    @Before
    public void clear() {
        clearAsset(THE_ASSET_ID);
    }

    @Test
    public void addNewAllFields() {
        final CoreAsset expected = getObject();

        final CoreAsset underTest = new CoreAsset();
        underTest.setSerial_number(expected.getSerial_number());
        underTest.setReference_count(null);
        underTest.setName(expected.getName());
        underTest.setLongitude(expected.getLongitude());
        underTest.setLatitude(expected.getLatitude());
        underTest.setGeometry(expected.getGeometry());
        underTest.setFunc_loc_path(expected.getFunc_loc_path());
        underTest.setCode(expected.getCode());
        underTest.setBarcode(expected.getBarcode());
        underTest.setAsset_type_code(expected.getAsset_type_code());
        underTest.setAsset_id(expected.getAsset_id());
        underTest.setAdm_path(expected.getAdm_path());
        underTest.setCreation_date(expected.getCreation_date());
        underTest.setDeactivated_at(expected.getDeactivated_at());

        final CoreAssetWriter writer = new CoreAssetWriterImpl(jdbc.getDataSource());
        writer.createAssets(Collections.singletonList(underTest));
        assertEquals(expected, retrieveAsset(expected.getAsset_id()));
    }

    @Test
    public void addExisting() {
        expect.expect(AlreadyExistsException.class);

        addNewAllFields();
        addNewAllFields();
    }

    @Test
    public void addNewNoPermission() {
        expect.expect(NotPermittedException.class);
    }

    @Test
    public void updateExisting() {
        addNewAllFields();

        final CoreAsset expected = retrieveAsset(THE_ASSET_ID);
        expected.setBarcode("abcd");

        final CoreAsset underTest = retrieveAsset(THE_ASSET_ID);
        underTest.setBarcode(expected.getBarcode());
        underTest.setAsset_id(expected.getAsset_id());

        final CoreAssetWriter writer = new CoreAssetWriterImpl(jdbc.getDataSource());
        writer.updateAssets(Collections.singletonList(underTest));
        assertEquals(expected, retrieveAsset(expected.getAsset_id()));
    }

    @Test
    public void updateNonExisting() {
        expect.expect(NotFoundException.class);
        final CoreAssetWriter writer = new CoreAssetWriterImpl(jdbc.getDataSource());
        writer.updateAssets(Collections.singletonList(getObject()));
    }

    @Test
    public void deleteExisting() {
        addNewAllFields();
        final CoreAssetWriter writer = new CoreAssetWriterImpl(jdbc.getDataSource());
        writer.deleteAssets(Collections.singletonList(getObject().getAsset_id()));
    }

    @Test
    public void deleteNonExisting() {
        expect.expect(NotFoundException.class);
        final CoreAssetWriter writer = new CoreAssetWriterImpl(jdbc.getDataSource());
        writer.deleteAssets(Collections.singletonList(getObject().getAsset_id()));
    }


    @Test
    public void addInvalid() {
        expect.expect(ValidationFailureException.class);
        final CoreAssetWriter writer = new CoreAssetWriterImpl(jdbc.getDataSource());
        final CoreAsset asset = getObject();
        asset.setAsset_id(null);
        writer.createAssets(Collections.singletonList(asset));
    }

    @Test
    public void updateInvalid() {
        expect.expect(ValidationFailureException.class);
        final CoreAssetWriter writer = new CoreAssetWriterImpl(jdbc.getDataSource());
        final CoreAsset asset = getObject();
        asset.setAsset_id(null);
        writer.updateAssets(Collections.singletonList(asset));
    }

    @Test
    public void addExternalLink() {
        fail("Unimplemented");
    }

    @Test
    public void deleteExternalLink() {
        fail("Unimplemented");
    }

    @Test
    public void deleteNonExistingExternalLink() {
        expect.expect(NotFoundException.class);
    }

    @Test
    public void addExternalLinkToUnknownType() {
        expect.expect(NotFoundException.class);
    }

    @Test
    public void deleteExternalLinkFromUnknownType() {
        expect.expect(NotFoundException.class);
    }

    private void clearAsset(UUID uuid) {
        jdbc.update("DELETE FROM location WHERE asset_id=?", uuid);
        jdbc.update("DELETE FROM geoms WHERE asset_id=?", uuid);
        jdbc.update("DELETE FROM asset_identification WHERE asset_id=?", uuid);
        jdbc.update("DELETE FROM asset WHERE asset_id=?", uuid);
    }

    private CoreAsset retrieveAsset(UUID uuid) {
        return jdbc.queryForObject(
                "SELECT asset.*, " +
                            "location.latitude, location.longitude ," +
                            "geoms.geom, " +
                            "asset_identification.barcode, " +
                            "asset_identification.serial_number " +
                        "FROM " +
                        "   asset " +
                        "LEFT JOIN location ON asset.asset_id = location.asset_id " +
                        "LEFT JOIN geoms ON asset.asset_id = geoms.asset_id " +
                        "LEFT JOIN asset_identification ON asset.asset_id = asset_identification.asset_id " +
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

                    return asset;
                },
                uuid
        );
    }

    private CoreAsset getObject() {
        final CoreAsset expected = new CoreAsset();
        expected.setSerial_number("RA1234-234-6");
        expected.setReference_count(0);
        expected.setName("Cat flap 12");
        expected.setLongitude(new BigDecimal("12.345600"));
        expected.setLatitude(new BigDecimal("-13.786800"));
        expected.setGeometry("01030000000100000005000000000000000000000000000000000000000000000000000000000000000000F03F000000000000F03F000000000000F03F000000000000F03F000000000000000000000000000000000000000000000000");
        expected.setFunc_loc_path("a.b.c"); // this should actually fail
        expected.setCode("c");
        expected.setBarcode("12342346");
        expected.setAsset_type_code("ROOM");
        expected.setAsset_id(THE_ASSET_ID);
        expected.setAdm_path("x.y.z");
        expected.setCreation_date(new Timestamp(System.currentTimeMillis()));
        expected.setDeactivated_at(null);
        return expected;
    }
}