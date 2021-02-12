package za.co.imqs.coreservice.dataaccess;

import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Slf4j
public class CsvDownload {
    private final DataSource ds;
    /*
    CREATE INDEX asset_func_text
    ON public.asset USING btree
            (replace(ltree2text(func_loc_path), '.'::text, '-'::text) COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;
*/

    public CsvDownload(DataSource ds) {
        this.ds = ds;
    }


    public OutputStream get(OutputStream out) {
        try (
                Connection c = ds.getConnection();
                Statement s = c.createStatement();
        ) {
            // This is critically important as it sets the resultset to page using the underlying cursor
            // rather than caching the entire resultset : https://jdbc.postgresql.org/documentation/head/query.html
            s.setFetchSize(1000);
            ResultSet rs = null;
            try {
                CSVWriter writer = new CSVWriter(new OutputStreamWriter(out));
                rs = s.executeQuery("SELECT * FROM dtpw.dtpw_export_view ORDER BY func_loc_path");
                writer.writeAll(rs, true);
                writer.flush();
            } catch (Exception e) {
                throw e;
            } finally {
                rs.close();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return out;
    }
}
