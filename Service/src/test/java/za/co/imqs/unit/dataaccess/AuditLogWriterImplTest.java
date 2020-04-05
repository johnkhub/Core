package za.co.imqs.unit.dataaccess;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import za.co.imqs.coreservice.dataaccess.AuditLogWriter;
import za.co.imqs.coreservice.dataaccess.AuditLogWriterImpl;
import za.co.imqs.libimqs.dbutils.HikariCPClientConfigDatasourceHelper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import static junit.framework.TestCase.*;
import static za.co.imqs.TestUtils.SERVICES;
import static za.co.imqs.TestUtils.ServiceRegistry.PG;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/17
 */
public class AuditLogWriterImplTest {

    private final JdbcTemplate jdbc;

    @Rule
    public ExpectedException expect = ExpectedException.none();

    public AuditLogWriterImplTest() {
        this.jdbc = new JdbcTemplate(
                HikariCPClientConfigDatasourceHelper.getDefaultDataSource(
                        "jdbc:postgresql://"+SERVICES.get(PG)+":5432/test_core","postgres","1mq5p@55w0rd"
                )
        );
        DbCreator.create(jdbc.getDataSource());
    }

    @Before
    public void before() {
        jdbc.update("DELETE FROM audit.auditlink");
        jdbc.update("DELETE FROM audit.audit");
        jdbc.update("DELETE FROM audit.audit_type");
        jdbc.update("INSERT INTO audit.audit_type(mnemonic) VALUES ('A1')");
    }


    @Test
    public void testWriteAll() {
        final AuditLogWriter audit = new AuditLogWriterImpl(jdbc.getDataSource());
        final AuditLogWriter.AuditLogRow row = new AuditLogWriter.AuditLogRow();

        row.setStatus("S1");
        row.setAction("A1");
        row.setPrincipal_id(UUID.randomUUID());
        row.setEvent_time(new Timestamp(System.currentTimeMillis()));
        row.setAudit_id(UUID.randomUUID());

        row.setInsert_time(new Timestamp(System.currentTimeMillis()));
        audit.write(row);

        assertEquals(row, getRow(row.getAudit_id()));
    }

    @Test
    public void testWriteExcludeOptional() {

        final AuditLogWriter audit = new AuditLogWriterImpl(jdbc.getDataSource());
        final AuditLogWriter.AuditLogRow row = new AuditLogWriter.AuditLogRow();

        row.setStatus("S1");
        row.setAction("A1");
        row.setPrincipal_id(UUID.randomUUID());
        row.setAudit_id(UUID.randomUUID());
        row.setEvent_time(new Timestamp(System.currentTimeMillis()));
        audit.write(row);

        final AuditLogWriter.AuditLogRow actual = getRow(row.getAudit_id());

        // System should set insert time ind db to time of insert so ~ event  time
        row.setInsert_time(actual.getInsert_time()); // set it the same so equals passes
        assertEquals(row, actual);
        assertTrue(actual.getInsert_time().getTime()-row.getEvent_time().getTime() < 30);
    }

    @Test
    public void testLinkToEntity() {
        final AuditLogWriter audit = new AuditLogWriterImpl(jdbc.getDataSource());
        final AuditLogWriter.AuditLogRow row = new AuditLogWriter.AuditLogRow();

        row.setStatus("S1");
        row.setAction("A1");
        row.setPrincipal_id(UUID.randomUUID());
        row.setEvent_time(new Timestamp(System.currentTimeMillis()));
        row.setAudit_id(UUID.randomUUID());
        row.setCorrelation(UUID.randomUUID());

        row.setInsert_time(new Timestamp(System.currentTimeMillis()));
        audit.write(row);

        assertEquals(row, getRow(row.getAudit_id()));
    }

    @Test
    public void testTamperCheckDeleteRow() {
        // Insert 5 or so rows
        // Delete one and recalculate all the checksums
        fail("Not implemented");
    }

    @Test
    public void testTamperCheckInsertRow() {
        fail("Not implemented");
        // Insert 5 or so rows
        // Delete one and recalculate all the checksums
    }

    @Test
    public void testTamperCheckUpdateRow() {
        fail("Not implemented");
        // Insert 5 or so rows
        // Delete one and recalculate all the checksums
    }

    private AuditLogWriter.AuditLogRow getRow(UUID uuid) {
        return jdbc.queryForObject(
                "SELECT audit.*, entity_id FROM audit.audit LEFT JOIN audit.auditlink ON audit.audit_id = auditlink.audit_id WHERE audit.audit_id = ?",
                new EntryMapper(), uuid
        );
    }


    private static class EntryMapper implements RowMapper<AuditLogWriter.AuditLogRow> {
        @Override
        public AuditLogWriter.AuditLogRow mapRow(ResultSet rs, int rowNum) throws SQLException {
            final AuditLogWriter.AuditLogRow a = new AuditLogWriter.AuditLogRow();
            a.setInsert_time(rs.getTimestamp("insert_time"));
            a.setEvent_time(rs.getTimestamp("event_time"));
            a.setCorrelation(rs.getObject("entity_id", UUID.class));
            a.setAudit_id(rs.getObject("audit_id", UUID.class));
            a.setPrincipal_id(rs.getObject("principal_id", UUID.class));
            a.setAction(rs.getString("action"));
            a.setStatus(rs.getString("status"));

            return a;
        }
    }
}
