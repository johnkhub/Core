package za.co.imqs.coreservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Timer;
import java.util.TimerTask;

import static za.co.imqs.coreservice.WebMvcConfiguration.DIE_PATH;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_TEST;


/**
 * (c) 2018 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2018/09/06
 */
@Slf4j
@RestController
@RequestMapping(DIE_PATH)
@Profile(PROFILE_TEST)
public class MortalCoilController {


    @RequestMapping(
            method = RequestMethod.GET
    )
    public String shoveOff() {
        if (System.getProperty("ciMode") != null) {
            log.info("Exiting VM");
            new Timer().schedule(
                    new TimerTask() {
                        public void run() {
                            //noinspection CallToSystemExit
                            System.exit(0);
                        }
                    },
                    2000);

            return "Shutting down";
        }
        return "No you don't!";
    }
}
