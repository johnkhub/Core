package za.co.imqs.coreservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import za.co.imqs.coreservice.dataaccess.FDW_Builder;
import za.co.imqs.coreservice.dataaccess.Meta;
import za.co.imqs.coreservice.dataaccess.MetaImpl;

import javax.sql.DataSource;

import static za.co.imqs.coreservice.WebMvcConfiguration.ASSET_ROOT_PATH;
import static za.co.imqs.coreservice.controller.ExceptionRemapper.mapException;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_PRODUCTION;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_TEST;

@Profile({PROFILE_PRODUCTION, PROFILE_TEST})
@RestController
@Slf4j
@RequestMapping(ASSET_ROOT_PATH+"/meta")
public class MetaController {

    private final Meta meta;
    private static final String[] PREAMBLE = {
            "-- We are very restrictive  in how the fdw is constructed",
            "-- 1. We force the use of the 'normal_reader' role as it is read-only and has restricted visibility",
            "-- 2. We force all local proxies of remote tables into the local public schema"
    };

    private static final String VIEW_DEPS =
            "SELECT \n" +
            "   '\"'||v.view_schema ||'.'|| v.view_name ||'\" -> \"'||\tt.table_schema ||'.'||t.table_name ||'\";' AS n \n" +
            "FROM \n" +
            "   information_schema.view_table_usage v JOIN information_schema.tables t\n" +
            "   ON v.table_schema = t.table_schema \n" +
            "   AND v.table_name = t.table_name \n" +
            "   AND t.table_type = 'VIEW'\n" +
            "   AND view_schema NOT IN ('information_schema', 'pg_catalog')";

    private final DataSource core;

    @Autowired
    public MetaController(
            @Qualifier("core_ds") DataSource core
    ) {
        this.meta = new MetaImpl(core);
        this.core  = core;
    }

    // e.g. http://localhost:8669/assets/meta/fdw/alias/core_host/username/core_user
    @RequestMapping(
            method = RequestMethod.GET, value = "/fdw/alias/{serveralias}/username/{username}",
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<String> getFdwDefinition(
            @PathVariable String serveralias,
            @PathVariable String username
    ) {
        try {
            final FDW_Builder bob = new FDW_Builder("normal_reader", "*******",  meta);
            bob.preamble(PREAMBLE).createServer(serveralias).asUser(username);
            bob.excludeFrom("public", "geometry_columns");
            return new ResponseEntity(bob.get(), HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }


    @RequestMapping(
            method = RequestMethod.GET, value = "/viewdependencies",
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<String> getViewDependencies() {
        final StringBuilder data = new StringBuilder("digraph ").append("views-dependencies").append("{\n\r");
        for (String s : new JdbcTemplate(core).query(VIEW_DEPS, (rs,i)-> rs.getString("n"))) {
            data.append("\t").append(s).append("\n\r");
        }
        data.append("}\n\r");
        data.append("/*\n\r");
        data.append("View with Graphviz or similar.  sudo apt-install graphviz \n\r");
        data.append("Then pipe data into dot executible. \n\r");
        data.append("*/");


        return new ResponseEntity<>(data.toString(),HttpStatus.OK);
    }
}