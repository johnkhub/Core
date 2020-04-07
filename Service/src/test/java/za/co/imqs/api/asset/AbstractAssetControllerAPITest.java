package za.co.imqs.api.asset;

import com.jayway.restassured.RestAssured;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.BeforeClass;
import za.co.imqs.coreservice.dto.AssetEnvelopeDto;
import za.co.imqs.coreservice.dto.CoreAssetDto;

import java.util.UUID;

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
public class AbstractAssetControllerAPITest {
    public static final UUID THE_ASSET = UUID.fromString("455ac960-8fc6-409f-b2ef-cd5be4ebe683");
    public static String session;

    @BeforeClass
    public static void configure() {
        RestAssured.baseURI = "http://"+SERVICES.get(CORE);
        RestAssured.port = CORE_PORT;
        session = getAuthSession(USERNAME, PASSWORD);
    }

    protected static CoreAssetDto getAsset(UUID uuid) {
        return given().
                header("Cookie", session).
                get("/assets/testing/{uuid}", uuid).
                then().assertThat().
                statusCode(HttpStatus.SC_OK).
                extract().as(CoreAssetDto.class);
    }

    protected static void assertNotFound(UUID uuid) {
        given().
                header("Cookie", session).
                get("/assets/testing/{uuid}", uuid).
                then().assertThat().
                statusCode(HttpStatus.SC_NOT_FOUND);
    }

    protected static void assertEquals(CoreAssetDto asset, AssetEnvelopeDto envelope) throws Exception  {
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

}
