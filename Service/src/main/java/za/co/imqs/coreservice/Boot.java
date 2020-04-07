package za.co.imqs.coreservice;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

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
