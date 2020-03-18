package za.co.imqs.coreservice;

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
    private ConfigurableApplicationContext context;

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
            String strConfig = "";
            int var5 = config.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                String con = config[var6];
                if (!con.startsWith("--")) {
                    strConfig = strConfig + " " + con;
                }
            }

            System.setProperty("imqs.configuration.file", strConfig);
        }
    }

    private Boot start(String[] args) {
        setConfigurationProperty(args);
        context = SpringApplication.run(Boot.class, args);
        return this;
    }
}
