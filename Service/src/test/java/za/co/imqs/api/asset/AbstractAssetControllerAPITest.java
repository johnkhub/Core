package za.co.imqs.api.asset;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.HttpStatus;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import za.co.imqs.LoginRule;
import za.co.imqs.TestUtils;
import za.co.imqs.coreservice.dataaccess.LookupProvider;
import za.co.imqs.coreservice.dto.asset.CoreAssetDto;
import za.co.imqs.coreservice.dto.auth.GroupDto;
import za.co.imqs.coreservice.dto.auth.UserDto;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static za.co.imqs.TestUtils.*;
import static za.co.imqs.TestUtils.ServiceRegistry.AUTH;
import static za.co.imqs.TestUtils.ServiceRegistry.CORE;
import static za.co.imqs.api.TestConfig.COMPOSE_FILE;
import static za.co.imqs.api.TestConfig.DOCKER;
import static za.co.imqs.coreservice.dataaccess.LookupProvider.Kv.pair;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/03/10
 */
@Slf4j
public class AbstractAssetControllerAPITest {
    protected static final boolean TEST_PERMISSIONS = false;

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


    public static final UUID THE_ASSET = UUID.fromString("455ac960-8fc6-409f-b2ef-cd5be4ebe683");
    public static final String THE_EXTERNAL_ID = "c45036b1-a1fb-44f4-a254-a668c0d09eaa";
    public static final String THE_GROUPING_ID = "25d0e46a-8360-4fc5-b792-994cd43311b5";

    public static final GroupDto GRP_WCED = GroupDto.of(UUID.fromString("20d93f56-294b-424a-8492-a8ba866d5c0c"), "WCED");
    public static final UserDto USR_DEV = UserDto.of(UUID.fromString("f6aefa3f-e1db-4ed9-bc65-4b3265b18ebc"), "dev");

    public static String session;
    public static UUID userId;

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @BeforeClass
    public static void configure() {
        RestAssured.baseURI = "http://"+SERVICES.get(CORE);
        RestAssured.port = CORE_PORT;

        session = login.getSession();
        userId = UUID.fromString(login.getPermit().getInternalUUID());

        poll(()-> given().get("/assets/ping").then().assertThat().statusCode(HttpStatus.SC_OK),TimeUnit.SECONDS, 25);
    }

    @After
    public void after() throws Exception  {
        deleteAssets(THE_ASSET);
    }


    protected void prepPermissions() {
        // REMOVE USER PERMISSIONS HERE !


        deleteUserFromGroup(userId, GRP_WCED.getName());
        deleteGroup(GRP_WCED.getName());
        deleteUser(userId);

        addGroup(GRP_WCED);
        USR_DEV.setPrincipal_id(userId);
        addUser(USR_DEV);
        addUserToGroup(USR_DEV.getPrincipal_id(), GRP_WCED.getName());

        grantPermissions(UUID.fromString("20430440-b8d4-45db-bedb-dbbfbe6699c6"),getGroupByName("WCED").getGroup_id(), 1|2|4);
        grantPermissions(UUID.fromString("3c2faf23-9011-4458-9d8b-d4ffadbaeb9a"),getGroupByName("WCED").getGroup_id(),1|2|4);
        grantPermissions(UUID.fromString("10fdd030-7d94-46ce-b3c9-ae82d1c0f4bd"),getGroupByName("WCED").getGroup_id(),1|2|4);
        grantPermissions(UUID.fromString("9d92001f-bed0-484f-b2ae-d5fee4f4993d"),getGroupByName("WCED").getGroup_id(),1|2|4);
        grantPermissions(UUID.fromString("e2963409-44eb-480f-b12f-bde4ea4f3c52"),getGroupByName("WCED").getGroup_id(),1|2|4);
        grantPermissions(UUID.fromString("03298160-03d6-4d14-bd7e-1dde7e771871"),getGroupByName("WCED").getGroup_id(),1|2|4);
        grantPermissions(UUID.fromString("d6adf53d-0ad9-4047-ae72-334f5a15853d"),getGroupByName("WCED").getGroup_id(),1|2|4);
        grantPermissions(UUID.fromString("37144c0d-615f-4096-807c-d80c51c6a762"),getGroupByName("WCED").getGroup_id(),1|2|4);

        final List<LookupProvider.Kv> kv = new LinkedList<>();
        kv.add(pair("WCED","Western Cape Department of Education"));
        given().
                header("Cookie", session).
                contentType(ContentType.JSON).body(kv).
                put("/lookups/kv/{target}", "CLIENT_DEP").
                then().statusCode(200);
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
    
    public static boolean isEq(CoreAssetDto compare, CoreAssetDto to) {
        return 
            Objects.equals(compare.getAdm_path(), to.getAdm_path()) &&
            Objects.equals(compare.getName(), to.getName()) &&
            Objects.equals(compare.getAddress(), to.getAddress()) &&
            Objects.equals(compare.getAsset_type_code(), to.getAsset_type_code()) &&
            Objects.equals(compare.getBarcode(), to.getBarcode()) &&
            Objects.equals(compare.getCode(), to.getCode()) &&
            Objects.equals(compare.getGeom(), to.getGeom()) &&
            Objects.equals(compare.getLatitude(), to.getLatitude()) &&
            Objects.equals(compare.getLongitude(), to.getLongitude()) &&
            Objects.equals(compare.getDeactivated_at(), to.getDeactivated_at()) &&
            Objects.equals(compare.getSerial_number(), to.getSerial_number()) &&
            Objects.equals(compare.getFunc_loc_path(), to.getFunc_loc_path()) &&
            //Objects.equals(compare.getIs_owned(), to.getIs_owned()) && TODO!!!
            Objects.equals(compare.getDescription(), to.getDistrict_code()) &&
            Objects.equals(compare.getDistrict_code(), to.getDistrict_code()) &&
            Objects.equals(compare.getMunicipality_code(), to.getMunicipality_code()) &&
            Objects.equals(compare.getRegion_code(), to.getRegion_code()) &&
            Objects.equals(compare.getSuburb_code(), to.getSuburb_code()) &&
            Objects.equals(compare.getTown_code(), to.getTown_code()) &&
            Objects.equals(compare.getWard_code(), to.getWard_code())
                    &&
            (to.getCreation_date() == null) ? true :
            //assertThat(LocalDateTime.parse(asset.getCreation_date()), LocalDateTimeMatchers.within(100, ChronoUnit.MILLIS, LocalDateTime.now()));
            Objects.equals(compare.getCreation_date(), to.getCreation_date());
    }

    public static class IsEqualMatcher<T extends CoreAssetDto> extends BaseMatcher<T> {
        private final T to;

        public IsEqualMatcher(T to) {
            this.to = to;
        }

        @Override
        public boolean matches(Object match) {
            return isEq((T)match, to);
        }

        @Override
        public void describeTo(Description description) {

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

        public CoreAssetBuilder dept(String dept) {
            asset.setResponsible_dept_code(dept);
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

    public static void addGroup(GroupDto group) {
        given().
                header("Cookie", session).contentType(ContentType.JSON).body(group).
                post( "/assets/access/group").
                then().assertThat().statusCode(HttpStatus.SC_CREATED);
    }

    public static void addUser(UserDto user) {
        given().
                header("Cookie", session).contentType(ContentType.JSON).body(user).
                post( "/assets/access/user").
                then().assertThat().statusCode(HttpStatus.SC_CREATED);
    }

    public static void addUserToGroup(UUID userId, String groupName) {
        given().
                header("Cookie", session).
                post("/assets/access/group/{groupname}/{user_id}", groupName, userId).
                then().assertThat().statusCode(HttpStatus.SC_OK);
    }

    public void grantPermissions(UUID entityId, UUID toUser, int perms) {
        given().
                header("Cookie", session).
                post("assets/access/testing/authorisation/entity/{entity_id}/user/{grantee}/permissions/{perms}", entityId, toUser, perms).
                then().assertThat().statusCode(HttpStatus.SC_OK);
    }

    public void deleteUserFromGroup(UUID userId, String groupName) {
        given().
                header("Cookie", session).
                delete("/assets/access/group/{groupname}/{user_id}", groupName, userId).
                then().assertThat().statusCode(Matchers.isOneOf(HttpStatus.SC_OK, HttpStatus.SC_NOT_FOUND));
    }

    public void deleteGroup(String groupName) {
        given().
                header("Cookie", session).
                delete( "/assets/access/group/{name}", groupName).
                then().assertThat().statusCode(Matchers.isOneOf(HttpStatus.SC_OK, HttpStatus.SC_NOT_FOUND));
    }

    public void deleteUser(UUID uuid) {
        given().
                header("Cookie", session).
                delete( "/assets/access/user/{uuid}", uuid).
                then().assertThat().statusCode(Matchers.isOneOf(HttpStatus.SC_OK, HttpStatus.SC_NOT_FOUND));
    }

    public GroupDto getGroupByName(String name) {
        return given().
                header("Cookie", session).
                get("/assets/access/group/{name}", name).
                then().assertThat().statusCode(HttpStatus.SC_OK).extract().as(GroupDto.class);
    }

}
