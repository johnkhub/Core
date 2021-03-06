package za.co.imqs.api.lookup;

import ch.qos.logback.classic.Level;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
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
public class LookupControllerGetWithOperatorAPITest {
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
        /*
        LookupProvider.Kv[] list = given().
                header("Cookie", session).
                get("/lookups/kv/{lookuptype}", "SUBURB").
                then().statusCode(200).and().extract().body().as(LookupProvider.Kv[].class);
        for (LookupProvider.Kv s : list) {
            log.info(s.toString());
        }

         */
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
                header("Cookie", login.getSession()).
                params(ops).
                get("/lookups/{view}/using_operators", name).
                then();

    }
}