package za.co.imqs.coreservice.dataaccess;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

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

    @Override
    public void write(Collection<AuditLogRow> rows) {

    }

    @Override
    public void write(AuditLogRow... rows) {

    }
}
