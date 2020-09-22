package za.co.imqs.api.lookup;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import za.co.imqs.TestUtils;
import za.co.imqs.coreservice.dataaccess.LookupProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.jayway.restassured.RestAssured.given;
import static org.assertj.core.api.Fail.fail;
import static org.hamcrest.Matchers.hasItem;
import static za.co.imqs.TestUtils.*;
import static za.co.imqs.TestUtils.ServiceRegistry.CORE;


/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/03/10
 */
@Slf4j
public class LookupControllerGetWithOperatorAPITest {
    private static final String COMPOSE_FILE = TestUtils.resolveWorkingFolder()+"/Docker_Test_Env/docker-compose.yml";
    private static final boolean DOCKER = true;
    private static String session;

    @ClassRule
    public static TestRule compose = !DOCKER ? NULL_RULE :
            new DockerComposeContainer(new File(COMPOSE_FILE)).withServices("auth", "router", "db", "asset-core-service")
                    .withLogConsumer("asset-core-service", new Slf4jLogConsumer(log));

    @BeforeClass
    public static void configure() {
        RestAssured.baseURI = "http://"+SERVICES.get(CORE);
        RestAssured.port = CORE_PORT;
        session = (String)waitForSession(USERNAME, PASSWORD)[0];

        poll(()-> given().get("/assets/ping").then().assertThat().statusCode(HttpStatus.SC_OK), TimeUnit.SECONDS, 10);
    }

    @Test
    public void testGetSuccess() {
        final Map<String, LookupProvider.Field> parameters  = new HashMap<>();
        parameters.put("code", LookupProvider.Field.of("=","ENVELOPE"));
        parameters.put("name", LookupProvider.Field.of("=","Envelope"));


        List<Map<String,String>> result = new ArrayList<>(0);
        result = verifyGetWithOperators(
                "public+assettype",
                parameters).
                statusCode(200).extract().as(result.getClass());

        org.hamcrest.MatcherAssert.assertThat(
                result,
                hasItem(
                        new MapBuilder().
                                put("code", "ENVELOPE").
                                put("name","Envelope").
                                put("description",null).
                                put("uid", "20430440-b8d4-45db-bedb-dbbfbe6699c6")
                                .get()
                )
        );

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
                params(ops).
                get("/lookups/{view}/using_operators", name).
                then();

    }
}