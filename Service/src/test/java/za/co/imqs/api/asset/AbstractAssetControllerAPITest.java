package za.co.imqs.api.asset;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import za.co.imqs.TestUtils;
import za.co.imqs.coreservice.dto.asset.CoreAssetDto;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static za.co.imqs.TestUtils.*;
import static za.co.imqs.TestUtils.ServiceRegistry.CORE;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/03/10
 */
@Slf4j
public class AbstractAssetControllerAPITest {
    protected static final boolean TEST_PERMISSIONS = false;

    private static final String COMPOSE_FILE = TestUtils.resolveWorkingFolder()+"/src/test/resources/Docker_Test_Env/docker-compose.yml";
    private static final boolean DOCKER = true;

    @ClassRule
    public static TestRule compose = !DOCKER ? NULL_RULE :
            new DockerComposeContainer(new File(COMPOSE_FILE)).withServices("auth", "router", "db", "asset-core-service")
            .withLogConsumer("asset-core-service", new Slf4jLogConsumer(log));


    public static final UUID THE_ASSET = UUID.fromString("455ac960-8fc6-409f-b2ef-cd5be4ebe683");
    public static final String THE_EXTERNAL_ID = "c45036b1-a1fb-44f4-a254-a668c0d09eaa";
    public static final String THE_GROUPING_ID = "25d0e46a-8360-4fc5-b792-994cd43311b5";
    public static String session;

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @BeforeClass
    public static void configure() {
        RestAssured.baseURI = "http://"+SERVICES.get(CORE);
        RestAssured.port = CORE_PORT;
        session = waitForSession(USERNAME, PASSWORD);

        poll(()-> given().get("/assets/ping").then().assertThat().statusCode(HttpStatus.SC_OK),TimeUnit.SECONDS, 25);
    }

    @After
    public void after() throws Exception  {
        deleteAssets(THE_ASSET);
    }

    protected static CoreAssetDto getAsset(UUID uuid) {
        return given().
                header("Cookie", session).
                get("/assets/{uuid}", uuid).
                then().assertThat().
                statusCode(HttpStatus.SC_OK).
                extract().as(CoreAssetDto.class);
    }

    protected static void assertNotFound(UUID uuid) {
        given().
                header("Cookie", session).
                get("/assets/{uuid}", uuid).
                then().assertThat().
                statusCode(HttpStatus.SC_NOT_FOUND);
    }

     protected static void assertEquals(CoreAssetDto asset, CoreAssetDto envelope) throws Exception  {
        assertThat(asset, notNullValue());
        assertThat(asset.getAdm_path(), equalTo(envelope.getAdm_path()));
        assertThat(asset.getName(), equalTo(envelope.getName()));
        assertThat(asset.getAddress(), equalTo(envelope.getAddress()));
        assertThat(asset.getAsset_type_code(), equalTo(envelope.getAsset_type_code()));
        assertThat(asset.getBarcode(), equalTo(envelope.getBarcode()));
        assertThat(asset.getCode(), equalTo(envelope.getCode()));
        assertThat(asset.getGeom(), equalTo(envelope.getGeom()));
        assertThat(asset.getLatitude(), equalTo(envelope.getLatitude()));
        assertThat(asset.getLongitude(), equalTo(envelope.getLongitude()));
        assertThat(asset.getDeactivated_at(), equalTo(envelope.getDeactivated_at()));
        assertThat(asset.getSerial_number(), equalTo(envelope.getSerial_number()));
        assertThat(asset.getFunc_loc_path(), equalTo(envelope.getFunc_loc_path()));

        if (envelope.getCreation_date() == null) {
            //assertThat(LocalDateTime.parse(asset.getCreation_date()), LocalDateTimeMatchers.within(100, ChronoUnit.MILLIS, LocalDateTime.now()));
        } else {
            assertThat(asset.getCreation_date(), equalTo(envelope.getCreation_date()));
        }
    }

    public static class CoreAssetBuilder {
        private final CoreAssetDto asset;

        public CoreAssetBuilder(CoreAssetDto asset) {
            this.asset = asset;
        }

        public CoreAssetDto get() {
            return asset;
        }

        public CoreAssetBuilder address(String address) {
            asset.setAddress(address);
            return this;
        }

        //envelope.setAdm_path();

        public CoreAssetBuilder type(String type) {
            asset.setAsset_type_code(type);
            return this;
        }

        //envelope.setBarcode();

        public CoreAssetBuilder code(String type) {
            asset.setCode(type);
            return this;
        }

        //envelope.setCreation_date();

        //envelope.setDeactivated_at();

        public CoreAssetBuilder funcloc(String path) {
            asset.setFunc_loc_path(path);
            return this;
        }

        public CoreAssetBuilder geom(String geom) {
            asset.setGeom(geom);
            return this;
        }


        public CoreAssetBuilder latitude(String lat) {
            asset.setLatitude(lat);
            return this;
        }

        public CoreAssetBuilder longitude(String lng) {
            asset.setLongitude(lng);
            return this;
        }

        public CoreAssetBuilder name(String name) {
            asset.setName(name);
            return this;
        }

        public CoreAssetBuilder serial(String serial) {
            asset.setSerial_number(serial);
            return this;
        }
    }

    public static <T extends CoreAssetDto> void putAsset(UUID assetId, T asset) {
        given().
                header("Cookie", session).contentType(ContentType.JSON).body(asset).
                put("/assets/{uuid}", assetId).
                then().assertThat().statusCode(HttpStatus.SC_CREATED);
    }

    public static <T extends CoreAssetDto> void deleteAssets(UUID ...assets) {
        for (UUID u : assets) {
            given().
                    header("Cookie", session).
                    delete("/assets/testing/{uuid}", u).
                    then().assertThat().statusCode(HttpStatus.SC_OK);
        }
    }

}
