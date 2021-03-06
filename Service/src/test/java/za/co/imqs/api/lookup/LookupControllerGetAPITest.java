package za.co.imqs.api.lookup;

import ch.qos.logback.classic.Level;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runners.model.Statement;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import za.co.imqs.LoginRule;
import za.co.imqs.TestUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.jayway.restassured.RestAssured.given;
import static org.assertj.core.api.Fail.fail;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static za.co.imqs.TestUtils.*;
import static za.co.imqs.TestUtils.ServiceRegistry.AUTH;
import static za.co.imqs.TestUtils.ServiceRegistry.CORE;
import static za.co.imqs.api.TestConfig.COMPOSE_FILE;
import static za.co.imqs.api.TestConfig.DOCKER;


/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/03/10
 */
@Slf4j
public class LookupControllerGetAPITest {
    static {
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(Level.INFO);
    }

    protected static final TestRule NULL_RULE = new TestRule() {
        @Override
        public Statement apply(Statement statement, org.junit.runner.Description description) {
            return statement;
        }
    };

    @ClassRule
    public static LoginRule login = new LoginRule().
            withUrl("http://"+SERVICES.get(AUTH)+ "/auth2/login").
            withUsername(USERNAME).
            withPassword(PASSWORD);

    @ClassRule
    public static TestRule compose = !DOCKER ? NULL_RULE :
            new DockerComposeContainer(new File(COMPOSE_FILE)).
                    withServices("auth", "router", "db", "asset-core-service").
                    withLogConsumer("asset-core-service", new Slf4jLogConsumer(log)).
                    withEnv("BRANCH",TestUtils.getCurrentGitBranch());

    @BeforeClass
    public static void configure() {
        RestAssured.baseURI = "http://"+SERVICES.get(CORE);
        RestAssured.port = CORE_PORT;
        poll(()-> given().get("/assets/ping").then().assertThat().statusCode(HttpStatus.SC_OK), TimeUnit.SECONDS, 25);
    }

    @Test
    public void testGet() {
        List<Map<String,String>> result = new ArrayList<>(0);
        result = verifyGet("public+assettype", new MapBuilder().put("code", "ENVELOPE").put("name", "Envelope").get()).
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
    public void testGetAll() {
        List<Map<String,String>> result = new ArrayList<>(0);
        result = verifyGet("assettype", new HashMap<>()).
                statusCode(200).extract().as(result.getClass());

        org.hamcrest.MatcherAssert.assertThat(
                result,
                containsInAnyOrder(
                        new MapBuilder().
                                put("code", "ENVELOPE").
                                put("name","Envelope").
                                put("description",null).
                                put("uid", "20430440-b8d4-45db-bedb-dbbfbe6699c6")
                        .get(),
                        new MapBuilder().
                                put("code", "LANDPARCEL").
                                put("name","Land Parcel").
                                put("description",null).
                                put("uid", "3c2faf23-9011-4458-9d8b-d4ffadbaeb9a")
                        .get(),
                        new MapBuilder().
                                put("code", "FACILITY").
                                put("name","Facility").
                                put("description",null).
                                put("uid", "10fdd030-7d94-46ce-b3c9-ae82d1c0f4bd")
                        .get(),
                        new MapBuilder().
                                put("code", "SITE").
                                put("name","Site").
                                put("description","Site resides under Facility and is used to group things like fences that are not part of a Building").
                                put("uid", "9d92001f-bed0-484f-b2ae-d5fee4f4993d")
                        .get(),
                        new MapBuilder().
                                put("code", "BUILDING").
                                put("name","Building").
                                put("description","Building in a Facility").
                                put("uid", "e2963409-44eb-480f-b12f-bde4ea4f3c52")
                        .get(),
                        new MapBuilder().
                                put("code", "FLOOR").
                                put("name","Floor").
                                put("description","Floor in a Building").
                                put("uid", "03298160-03d6-4d14-bd7e-1dde7e771871")
                        .get(),
                        new MapBuilder().
                                put("code", "ROOM").
                                put("name","Room").
                                put("description","Room on a Floor").
                                put("uid", "d6adf53d-0ad9-4047-ae72-334f5a15853d")
                        .get(),
                        new MapBuilder().
                                put("code", "COMPONENT").
                                put("name","Component").
                                put("description","This is a placeholder - we will break these down further later").
                                put("uid", "37144c0d-615f-4096-807c-d80c51c6a762")
                        .get()
                )
        );
    }

    @Test
    public void testGetNonExistant() {
        fail("Not implemented");
    }

    @Test
    public void testGetAnEntireLookupTable() {
        // method = RequestMethod.GET, value = "/kv/{view}",
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

    private ValidatableResponse verifyGet(String name, Map<String,String> vals) {
        return given().
                header("Cookie", login.getSession()).
                params(vals).
                get("/lookups/{name}",name).
                then();
    }
}