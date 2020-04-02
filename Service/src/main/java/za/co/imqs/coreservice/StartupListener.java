package za.co.imqs.coreservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import za.co.imqs.services.serviceauth.ServiceAuth;
import za.co.imqs.spring.service.health.ServiceHealth;


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

    @Autowired
    public StartupListener(
            ServiceHealth serviceHealth,
            ServiceAuth serviceAuth
    ) {
        this.serviceHealth = serviceHealth;
        this.serviceAuth = serviceAuth;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        try {
            if (serviceHealth.isAvailable()) {
                // creates all the users in auth
                serviceAuth.setupUserAuth();
            }
        } finally {
            if(!serviceHealth.isAvailable()) {
                log.error("Failure in post startup task. Exiting. " + serviceHealth.getFailureMessage(), serviceHealth.getFailureReason());
                System.exit(1);
            }
        }
    }
}
