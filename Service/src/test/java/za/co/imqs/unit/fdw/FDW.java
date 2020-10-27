package za.co.imqs.unit.fdw;


import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import za.co.imqs.coreservice.dataaccess.FDW_Builder;
import za.co.imqs.coreservice.dataaccess.Meta;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
public class FDW {

    @Test
    public void test() throws Exception  {
        FDW_Builder bob = new FDW_Builder("normal_reader", "normal_reader", new Meta() {
            @Override
            public Set<String> userSchemas() {
                return Collections.singleton("dtpw");
            }

            @Override
            public Set<String> systemSchemas() {
                return null;
            }

            @Override
            public String getServerIP() {
                return "172.19.0.2";
            }

            @Override
            public List<String> listExtentions() {
                return Arrays.asList(
                        "plpgsql",
                        "ltree",
                        "uuid-ossp",
                        "postgis",
                        "unaccent",
                        "pg_trgm");
            }

            @Override
            public List<String> getTablesAndViewsForUser(String userName) {
                return null;
            }

            @Override
            public String getDbName() {
                return null;
            }
        });
        bob.createServer("core_host").asUser("postgres");
        log.info(bob.get());
    }
}
