package za.co.imqs.api.lookup;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import za.co.imqs.TestUtils;
import za.co.imqs.coreservice.dataaccess.LookupProvider;
import za.co.imqs.coreservice.dataaccess.exception.BusinessRuleViolationException;
import za.co.imqs.coreservice.dto.lookup.KvRegion;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.jayway.restassured.RestAssured.given;
import static junit.framework.TestCase.assertEquals;
import static org.assertj.core.api.Fail.fail;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static za.co.imqs.TestUtils.*;
import static za.co.imqs.TestUtils.ServiceRegistry.CORE;
import static za.co.imqs.coreservice.dataaccess.LookupProvider.KvDef.def;
import static za.co.imqs.coreservice.dto.lookup.KvRegion.tripple;


/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/03/10
 */
@Slf4j
public class LookupControllerKvAPITest {
    private static final String COMPOSE_FILE = TestUtils.resolveWorkingFolder()+"/src/test/resources/Docker_Test_Env/docker-compose.yml";
    private static final boolean DOCKER = true;
    private static String session;

    @ClassRule
    public static TestRule compose = !DOCKER ? NULL_RULE :
            new DockerComposeContainer(new File(COMPOSE_FILE)).withServices("auth", "router", "db", "asset-core-service")
                    .withLogConsumer("asset-core-service", new Slf4jLogConsumer(log));

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @BeforeClass
    public static void configure() {
        RestAssured.baseURI = "http://"+SERVICES.get(CORE);
        RestAssured.port = CORE_PORT;
        session = waitForSession(USERNAME, PASSWORD);
        poll(()-> given().get("/assets/ping").then().assertThat().statusCode(HttpStatus.SC_OK), TimeUnit.SECONDS, 10);
    }

    @Test
    public void testGetKvTypes() {
        final List<LookupProvider.KvDef> defs = Arrays.asList(
               given().
                    header("Cookie", session).
                    get("/lookups/kv/").
                    then().
                       statusCode(200).
                       extract().as(LookupProvider.KvDef[].class)
        );

        org.hamcrest.MatcherAssert.assertThat(
                defs,
                containsInAnyOrder(
                    def("SUBURB", "Suburb", "public.ref_suburb", null),
                    def("WARD", "Ward", "public.ref_ward", null),
                    def("REGION", "Region", "public.ref_region", null),
                    def("TOWN", "Town", "public.ref_town", null),
                    def("MUNIC", "Municiplaity", "public.ref_municipality", null),
                    def("DISTRICT", "District", "public.ref_district", null),
                    def("TAGS", "List of tags", "public.tags", null),

                    def("BRANCH", "Branch", "dtpw.ref_branch", null),
                    def("CHIEF_DIR", "Chief Directorate", "dtpw.ref_chief_directorate", null),
                    def("CLIENT_DEP", "Client Department", "dtpw.ref_client_department", null),
                    def("FACIL_TYPE","Facility Type", "asset.ref_facility_type", null),
                    def("EI_DISTR","Educational District", "dtpw.ref_ei_district", null)

                )
        );
    }

    @Test
    public void addKvSuccess() {
        given().
                header("Cookie", session).
                delete("/assets/testing/lookups/{target}", "REGION").
                then().statusCode(200);

        final List<KvRegion> kv = new LinkedList<>();
        kv.add(tripple("a","A", null));
        kv.add(tripple("b","B", "MULTIPOLYGON Z (((20.3680977460657 -33.2484598755242 0,20.3681727402499 -33.2484664430128 0,20.368210339253 -33.2485038037424 0,20.3682298561824 -33.2485650738627 0,20.368167133447 -33.2485817212164 0,20.3681425497901 -33.2485120323787 0,20.3680217144183 -33.2485450959406 0,20.3680414719439 -33.2486149280528 0,20.3679787477295 -33.2486314851749 0,20.3679598010498 -33.2485723724306 0,20.367984059638 -33.2485179231429 0,20.3680459276575 -33.248473968693 0,20.3680977460657 -33.2484598755242 0)))"));
        kv.add(tripple("c","C", null));



        given().
                header("Cookie", session).
                contentType(ContentType.JSON).body(kv).
                put("/lookups/kv/{target}", "REGION").
                then().statusCode(200);
    }

    @Test
    public void addKvDuplicate() {
        fail("Not implemented");
    }

    @Test
    public void addKvModifyDates() {
        fail("Not implemented");
    }

    @Test
    public void addKvValidationFailure() {
        fail("Not implemented");
    }

    @Test
    public void addKvBusinessRuleFailure() {
        expected.expect(BusinessRuleViolationException.class);
        fail("Not implemented");
    }

    @Test
    public void getKvSuccess() {
        addKvSuccess();

        assertEquals("A", given().
                header("Cookie", session).
                get("/lookups/v/{target}/{k}", "REGION", "a").
                then().statusCode(200).extract().asString());

        final KvRegion result = given().
                header("Cookie", session).
                get("/lookups/kv/{target}/{k}","REGION", "b").
                then().statusCode(200).extract().as(KvRegion.class);

        assertEquals("b", result.getK());
        assertEquals("01060000A0E6100000010000000103000080010000000D000000601565A73B5E344051DF7F88CD9F40C00000000000000000EBA69691405E34405C7297BFCD9F40C0000000000000000005246508435E3440AB00FFF8CE9F40C00000000000000000D5B6D54F445E34408595F7FAD09F40C00000000000000000EF9D8533405E344000819D86D19F40C00000000000000000529D13973E5E344050DD053ECF9F40C00000000000000000FBA4CBAB365E3440B2526153D09F40C000000000000000001D9245F7375E344060A42C9DD29F40C00000000000000000091FEFDA335E344082CA1028D39F40C0000000000000000066C00F9D325E3440A8233138D19F40C0000000000000000033980D34345E34402E2F706FCF9F40C0000000000000000007B70642385E3440CDB8B8FECD9F40C00000000000000000601565A73B5E344051DF7F88CD9F40C00000000000000000", result.getGeom());
    }

    @Test
    public void getKvMissing() {
        given().
                header("Cookie", session).
                get("/lookups/v/{target}/{k}", "REGION", "bbb").
                then().statusCode(204);
    }
}