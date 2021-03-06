package za.co.imqs.coreservice;

import com.zaxxer.hikari.HikariDataSource;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.report.DiffToReport;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import za.co.imqs.coreservice.sanitychecks.CheckReadOnlyGrants;
import za.co.imqs.libimqs.dbutils.DatabaseUtil;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Statement;

import static za.co.imqs.coreservice.Boot.Features.*;
import static za.co.imqs.coreservice.WebMvcConfiguration.PROFILE_ADMIN;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_PRODUCTION;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_TEST;

@Slf4j
@Component
@Profile({PROFILE_PRODUCTION, PROFILE_TEST, PROFILE_ADMIN})
public class SchemaManagement implements CliHandler {

    private final OptionGroup grp = new OptionGroup();
    private final HikariDataSource ds;
    private final String[] schemas;

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @Autowired
    public SchemaManagement(
            @Qualifier("core_ds") DataSource ds,
            @Qualifier("schemas") String ...schemas
    )  {
        this.ds = (HikariDataSource)ds;
        this.schemas = schemas;

        if (SCHEMA_MGMT_SYNC.isActive()) grp.addOption(Option.builder("sync").longOpt("sync-schemas").desc("Synchronise the schema of the database with the embedded changelog").build());
        if (SCHEMA_MGMT_DOC.isActive()) grp.addOption(Option.builder("doc").longOpt("document-schemas").desc("Document the embedded schemas").build());
        if (SCHEMA_MGMT_COMPARE.isActive()) {
            grp.addOption(Option.builder("cmp").
                    longOpt("compare-schemas").
                    numberOfArgs(3).
                    valueSeparator('=').
                    argName("url username password").
                    desc("Compare this database against the one for which the parameters ars supplied").
                    build());
        }
        if (SCHEMA_MGMT_SUPPRESS.isActive()) grp.addOption(Option.builder("none").longOpt("manual-schemas").desc("Disable liquibase schema management. ").build());
        //grp.addOption(Option.builder("none").longOpt("dump_schemas").desc("Dump all schemas").build());
    }

    private void upgrade() {
        boolean drop = Boolean.parseBoolean(System.getenv("DROP_DB"));
        if (drop & !activeProfile.equals(PROFILE_TEST)) {
            drop = false;
            log.warn("DROP_DB specified outside of TEST profile. Ignoring.");
        }

        if (drop) {
            log.warn("DROPPING DB!");
        }

        for (String schema : schemas) {
            final String schemaName = schema.substring(schema.indexOf("changelog")+10).split("\\.")[0];
            if (!schemaName.equalsIgnoreCase("public")) {
                createSchema(schemaName);
            }
            DatabaseUtil.updateDb(ds, schema, true, drop);
            drop = false;
        }
    }

    private void generateDifferenceReport(
            String schema,
            String username,
            String password,
            String url
    ) {
        try  (Connection c = ds.getConnection()) {
            final Liquibase l = new Liquibase(schema, new ClassLoaderResourceAccessor(), new JdbcConnection(c));

            final Database mine = DatabaseFactory.getInstance().openDatabase(appendSchema(ds.getJdbcUrl(),schema), username, password, null, new FileSystemResourceAccessor());
            final Database theirs = DatabaseFactory.getInstance().openDatabase(appendSchema(url,schema), username, password, null, new FileSystemResourceAccessor());
            final DiffResult diff = l.diff(mine, theirs, new CompareControl());
            new  DiffToReport(diff, System.out).print();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void generateDocumentation(String s) {
        try  (Connection c = ds.getConnection()) {
            Liquibase liquibase = new Liquibase(s, new ClassLoaderResourceAccessor(), new JdbcConnection(c));
            liquibase.generateDocumentation("dbdocs");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void changelogSync(String schema) {
        try  (Connection c = ds.getConnection()) {
            Liquibase liquibase = new Liquibase(schema, new ClassLoaderResourceAccessor(), new JdbcConnection(c));
            liquibase.changeLogSync("", new PrintWriter(System.out));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public OptionGroup getOptionGroup() {
        return grp;
    }

    @Override
    public Options getOptions() {
        return new Options();
    }

    @Override
    public boolean handle(CommandLine cmd, Options options) {
        if (cmd.hasOption("sync")) {
            for (String s : schemas) {
                changelogSync(s);
            }
            return false;
        } else if (cmd.hasOption("doc")) {
            for (String s : schemas) {
                generateDocumentation(s);
            }
            return false;
        } else if (cmd.hasOption("cmp")) {
            for (String s : schemas) {
                String[] values = cmd.getOptionValues("cmp");
                generateDifferenceReport(
                        s,
                        values[1],
                        values[2],
                        values[0]
                );
            }
            return false;
        } else if (cmd.hasOption("manual-schemas")) {
            return true;
        }

        upgrade();
        runChecks();
        return true;
    }

    private String appendSchema(String url, String json) {
        String s = json.split("\\.")[0];
        String[] q = s.split("_");
        return url + "?currentSchema=" + q[q.length-1];
    }

    private void createSchema(String name)  {
        try (
                final Connection c = ds.getConnection();
                final Statement createStatement = c.createStatement()
        ) {
            createStatement.executeUpdate("CREATE SCHEMA IF NOT EXISTS  " + name +";");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void runChecks() {
        new CheckReadOnlyGrants(new JdbcTemplate(ds)).check();
    }
}
