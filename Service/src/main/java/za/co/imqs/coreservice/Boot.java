package za.co.imqs.coreservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import za.co.imqs.configuration.client.ClientConfigurationFactory;

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
        "za.co.imqs.coreservice"
        })
@EnableAutoConfiguration(exclude = {LiquibaseAutoConfiguration.class})
@org.springframework.context.annotation.Import(za.co.imqs.spring.service.factorybeandefinitions.ClientConfiguration.class)
public class Boot {
    private static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        java.util.Locale.setDefault(java.util.Locale.US);
        Boot boot = new Boot();
        boot.start(args);
    }


    //-Dlog_name=imqs-input-form.log
    //-Dlogback.configurationFile=src\test\resources\logback.groovy
    //-Dspring.profiles.active=production

   // -Dlogback.configurationFile=http://localhost:2010/config-service/config/solar-gateway-service/1/logback-solar-gateway-service.groovy


    //--server.port=8667
    // http://localhost:2010/config-service/config/inputform-service/1/inputform-service-config.json

    // locale

    public static void setConfigurationProperty(String[] config) {
        String property = System.getProperty("imqs.configuration.file");
        if (property == null) {
            String strConfig = "";
            String[] var4 = config;
            int var5 = config.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                String con = var4[var6];
                if (!con.startsWith("--")) {
                    strConfig = strConfig + " " + con;
                }
            }

            System.setProperty("imqs.configuration.file", strConfig);
        }
    }

    public ClientConfigurationFactory config() {
        return context.getBean(ClientConfigurationFactory.class);
    }

    public Boot start(String[] args) {
        setConfigurationProperty(args);
        context = SpringApplication.run(Boot.class, args);
        return this;
    }

    public void shutdown() {
        context.close();
    }
}
