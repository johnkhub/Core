package za.co.imqs.coreservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_PRODUCTION;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_TEST;

/**
 * (c) 2016 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2016/11/11
 */
@Configuration
@Profile({PROFILE_PRODUCTION , PROFILE_TEST})
@Slf4j
public class MapperConfiguration {
    @Bean
    public ObjectMapper getFasterXmlObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule()); // joda time
        mapper.setSerializationInclusion(NON_NULL);

        //mapper.registerSubtypes(new NamedType());
        return mapper;
    }
}
