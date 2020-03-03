package za.co.imqs.api.lookup;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;
import za.co.imqs.TestUtils;
import za.co.imqs.coreservice.dataaccess.LookupProvider;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.assertj.core.api.Fail.fail;
import static za.co.imqs.TestUtils.*;


/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/03/10
 */
@Slf4j
public class LookupControllerGetWithOperatorAPITest {
     private static String session;

    @BeforeClass
    public static void configure() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = CORE_PORT;
        session = TestUtils.getAuthSession(USERNAME, PASSWORD);
    }

    @Test
    public void testGetSuccess() {
       fail("Not implemented");
    }

    @Test
    public void testGetAllSuccess() {
        fail("Not implemented");
    }

    @Test
    public void testGetNonExistant() {
        fail("Not implemented");
    }

    private static class MapBuilder {
        private final Map<String,String> map = new HashMap<>();

        public MapBuilder put(String key, String value) {
            map.put(key,value);
            return this;
        }

        public Map<String,String> get() {
            return map;
        }
    }


    private ValidatableResponse verifyGetWithOperators(String name, final Map<String, LookupProvider.Field> ops) {
         return given().
                header("Cookie", session).
                contentType(ContentType.JSON).body(ops).
                get("/lookups/", name).
                then();

    }
}