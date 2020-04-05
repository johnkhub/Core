package za.co.imqs.coreservice.dataaccess;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.Collection;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/13
 */
@Slf4j
@Repository
public class AuditLogWriterImpl implements AuditLogWriter {

    private final JdbcTemplate jdbc;

    @Autowired
    public AuditLogWriterImpl(
            @Qualifier("audit_ds") DataSource ds
    ) {
        this.jdbc = new JdbcTemplate(ds);
    }

    @Override
    @Transactional("audit_tx_mgr")
    public void write(Collection<AuditLogRow> rows) {
        for (AuditLogRow r : rows) {
            if (r.getInsert_time() != null) {
                jdbc.update(
                        "INSERT INTO audit.audit (audit_id, principal_id, event_time, insert_time, action, status, parameters) VALUES (?,?,?,?,?,?,to_jsonb(?))",
                        r.getAudit_id(), r.getPrincipal_id(), r.getEvent_time(), r.getInsert_time(), r.getAction(), r.getStatus(),
                        r.getParameters() == null ?  "{}" : r.getParameters()
                );
            } else {
                jdbc.update(
                        "INSERT INTO audit.audit (audit_id, principal_id, event_time,  action, status, parameters) VALUES (?,?,?,?,?,to_jsonb(?))",
                        r.getAudit_id(), r.getPrincipal_id(), r.getEvent_time(), r.getAction(), r.getStatus(),
                        r.getParameters() == null ?  "{}" : r.getParameters()
                );
            }
            if (r.getCorrelation() != null) {
                jdbc.update("INSERT INTO audit.auditlink (audit_id, entity_id) VALUES (?,?)", r.getAudit_id(), r.getCorrelation());
            }
        }
    }
}
