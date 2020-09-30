package za.co.imqs.coreservice;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;

import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_PRODUCTION;

/**
 * (c) 2019 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2019/06/18
 */
@ComponentScan({
        "za.co.imqs.formservicebase.errors",
        "za.co.imqs.spring.service.health",
        "za.co.imqs.formservicebase.interceptors",
        "za.co.imqs.spring.service.factorybeandefinitions",

        "za.co.imqs.coreservice",
        "za.co.imqs.coreservice.controller",
        "za.co.imqs.coreservice.dataaccess"
        })
@EnableAutoConfiguration(exclude = {LiquibaseAutoConfiguration.class})
@org.springframework.context.annotation.Import(za.co.imqs.spring.service.factorybeandefinitions.ClientConfiguration.class)
public class Boot {
    // Yeah, this is mutton dressed as lamb, but it starts us down the road of making use of Togglz
    // At least the interface of checking if a feature is enabled sprinkled throughout the code remains the same
    public enum Features implements Feature {

        @Label("Global switch to turn authentication on / off")
        AUTHENTICATION_GLOBAL(true),

        @Label("Global switch to turn authorisation on / off")
        AUTHORISATION_GLOBAL(false),

        @Label("Global switch to turn audit logging on / off")
        AUDIT_GLOBAL(true),

        @Label("Enable the command line option to sync schemas - see SchemaManagment.java")
        SCHEMA_MGMT_SYNC(true),

        @Label("Enable the command line option allow documenting database schemas - see SchemaManagment.java")
        SCHEMA_MGMT_DOC(false),

        @Label("Enable the command line option allow comparing a remote database within the current schemas - see SchemaManagment.java")
        SCHEMA_MGMT_COMPARE(true),

        @Label("Enable to stop liquibase form managing the schema - see SchemaManagment.java")
        SCHEMA_MGMT_SUPPRESS(true);

        private final boolean enabled;

        Features(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isActive() {
            //return FeatureContext.getFeatureManager().isActive(this);
            return enabled;
        }
    }

    public static void main(String[] args) {
        java.util.Locale.setDefault(java.util.Locale.US);

        if (!System.getenv().containsKey("spring_profiles_active")) {
            System.getProperties().putIfAbsent("spring.profiles.active", PROFILE_PRODUCTION);
        }

        new Boot().start(args);
    }

    private static void setConfigurationProperty(String[] config) {
        String property = System.getProperty("imqs.configuration.file");

        if (property == null) {
            System.setProperty("imqs.configuration.file", config[config.length-1].substring("--config=".length()));
        }
    }

    private Boot start(String[] args) {
        setConfigurationProperty(args);
        ConfigurableApplicationContext context = SpringApplication.run(Boot.class, args);
        return this;
    }

    // These implement empty 'handle' methods. They are used for validating the commandline and generating the help text only.
    public static class ConfigFileHandler implements CliHandler {

        @Override
        public Options getOptions() {
            return new Options().addOption(Option.builder().longOpt("config").required(true).hasArg().argName("uri").build());
        }

        @Override
        public boolean handle(CommandLine cmd, Options options) {
            return true;
        }
    }

    public static class HttpPortHandler implements CliHandler {

        public HttpPortHandler() {
            super();
        }

        @Override
        public Options getOptions() {
            return new Options().addOption(Option.builder().longOpt("server.port").required(false).valueSeparator('=').hasArg().argName("HTTP Port").build());
        }

        @Override
        public boolean handle(CommandLine cmd, Options options) {
            return true;
        }
    }

    /*
    public static class JavaPropsHandler implements CliHandler {
        @Override
        public Options getOptions() {
            Option.builder("D").argName( "property=value" ).numberOfArgs(2).valueSeparator('=').desc("use value for given property" ).build();

            return new Options().addOption(Option.builder().longOpt("in.container").required(false).valueSeparator('=').hasArg().type(Boolean.class).argName("true or false").build());
        }

        @Override
        public boolean handle(CommandLine cmd, Options options) {
            return true;
        }
    }

     */
}
