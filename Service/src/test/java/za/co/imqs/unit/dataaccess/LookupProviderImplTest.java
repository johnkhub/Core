package za.co.imqs.unit.dataaccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import za.co.imqs.configuration.client.ClientConfigurationFactory;
import za.co.imqs.coreservice.dataaccess.LookupProvider;
import za.co.imqs.coreservice.dataaccess.LookupProviderImpl;

import java.util.HashMap;
import java.util.Map;

import static za.co.imqs.TestUtils.BOING;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/28
 */
@Slf4j
public class LookupProviderImplTest {

    @Test
    public void test() throws Exception  {
        final LookupProvider p = new LookupProviderImpl(
                new ClientConfigurationFactory("file:C:/Users/frankvr/Documents/Core/Service/src/test/resources/config.json"), BOING
        );
        final Map<String,String> params = new HashMap<>();

        params.put("asset_type_code", "ROOM");
        log.info(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(p.get("asset", params)));
    }

    @Test
    public void testWithOperators() throws Exception  {
        final LookupProvider p = new LookupProviderImpl(
                new ClientConfigurationFactory("file:C:/Users/frankvr/Documents/Core/Service/src/test/resources/config.json"), BOING
        );
        final Map<String, LookupProvider.Field> params = new HashMap<>();

        final LookupProvider.Field fld = new LookupProvider.Field();
        fld.setOperator("=");
        fld.setValue("FLOOR");
        params.put("asset_type_code", fld);
        log.info(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(p.getWithOperators("asset", params)));
    }
}
