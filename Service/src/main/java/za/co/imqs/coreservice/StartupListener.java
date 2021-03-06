package za.co.imqs.coreservice;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import za.co.imqs.services.serviceauth.ServiceAuth;
import za.co.imqs.spring.service.health.ServiceHealth;

import java.io.*;
import java.net.URL;


/**
 * (c) 2018 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2018/05/25
 */
@Component
@Slf4j
public class StartupListener implements ApplicationListener<ContextRefreshedEvent> {

    private final ServiceHealth serviceHealth;
    private final ServiceAuth serviceAuth;
    private final SchemaManagement schema;
    private final ApplicationArguments applicationArguments;
    private final String visibleHost;
    private final String authUrl;

    @Autowired
    public StartupListener(
            ServiceHealth serviceHealth,
            ServiceAuth serviceAuth,
            SchemaManagement schema,
            ApplicationArguments applicationArguments,
            @Qualifier("visible_host_url")
            String visibleHost,
            @Qualifier("authUrl")
                    URL authUrl
    ) {
        this.serviceHealth = serviceHealth;
        this.serviceAuth = serviceAuth;
        this.schema = schema;
        this.applicationArguments = applicationArguments;
        this.visibleHost = visibleHost;
        this.authUrl = authUrl.toString();
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        try {
            if (serviceHealth.isAvailable()) {
                // creates all the users in auth
                serviceAuth.setupUserAuth();
            }

            processCommandline(
                    applicationArguments.getSourceArgs(),
                    new HelpHandler(),
                    schema,
                    new Boot.HttpPortHandler(),
                    new Boot.ConfigFileHandler()
            );

            new WriteImporterConfig().write("import_config_template.json", "import_config.json", visibleHost);

        } finally {
            if(!serviceHealth.isAvailable()) {
                log.error("Failure in post startup task. Exiting. " + serviceHealth.getFailureMessage(), serviceHealth.getFailureReason());
                System.exit(1);
            }
        }
    }

    // The handlers are called in declaration sequence which may or may not be important to you
    private void processCommandline(String[] args, CliHandler ...handlers) {
        try {
            final Options options = new Options();
            final CommandLineParser parser = new DefaultParser();

            for (CliHandler handler : handlers) {
                if (!handler.getOptionGroup().getOptions().isEmpty()) {
                    options.addOptionGroup(handler.getOptionGroup());
                }
                if (!handler.getOptions().getOptions().isEmpty()) {
                    handler.getOptions().getOptions().forEach(o -> options.addOption(o));
                }
            }

            final CommandLine cmd = parser.parse(options, args);

            for (CliHandler handler : handlers) {
                if (!handler.handle(cmd, options)) {
                    System.exit(0);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class HelpHandler implements CliHandler {

        @Override
        public Options getOptions() {
            return new Options().addOption(Option.builder("h").longOpt("help").build());
        }

        @Override
        public boolean handle(CommandLine cmd, Options options) {
            if (cmd.hasOption("h")) {
                new HelpFormatter().printHelp("java -jar asset-core-service.jar ", options);
                return false;
            }
            return true;
        }
    }

    private class WriteImporterConfig {
        public void write(String templateName, String targetName, String hostUrl)  {
            try (
                BufferedReader r = new BufferedReader(new FileReader(templateName));
                FileWriter w = new FileWriter(targetName);
            ) {
                String line = r.readLine();
                while (line != null) {
                    line = line.replace("{{SERVER}}", hostUrl);
                    line = line.replace("{{AUTH}}", authUrl);
                    w.write(line);
                    line = r.readLine();
                }
                w.flush();
            } catch(IOException e) {
                throw new RuntimeException("Could not create import config file.",e);
            }
        }
    }
}
