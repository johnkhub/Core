package za.co.imqs.api.lookup;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;
import za.co.imqs.TestUtils;
import za.co.imqs.coreservice.dataaccess.LookupProvider;

import java.util.*;

import static com.jayway.restassured.RestAssured.given;
import static junit.framework.TestCase.assertEquals;
import static org.assertj.core.api.Fail.fail;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static za.co.imqs.TestUtils.*;
import static za.co.imqs.TestUtils.ServiceRegistry.CORE;


/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/03/10
 */
@Slf4j
public class LookupControllerKvAPITest {
     private static String session;

    @BeforeClass
    public static void configure() {
        RestAssured.baseURI = "http://"+SERVICES.get(CORE);
        RestAssured.port = CORE_PORT;
        session = TestUtils.getAuthSession(USERNAME, PASSWORD);
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
                    def("SUBURB", "Suburb", "asset.ref_suburb", null),
                    def("WARD", "Ward", "asset.ref_ward", null),
                    def("REGION", "Region", "asset.ref_region", null),
                    def("TOWN", "Town", "asset.ref_town", null),
                    def("MUNIC", "Municiplaity", "asset.ref_municipality", null),
                    def("DISTRICT", "District", "asset.ref_district", null),
                    def("TAGS", "List of tags", "public.tags", null),

                    def("BRANCH", "Branch", "dtpw.ref_branch", null),
                    def("CHIEF_DIR", "Chief Directorate", "dtpw.ref_chief_directorate", null),
                    def("CLIENT_DEP", "Client Department", "dtpw.ref_client_department", null)
                )
        );
    }

    @Test
    public void addKvSuccess() {
        given().
                header("Cookie", session).
                delete("/assets/testing/lookups/{target}", "REGION").
                then().statusCode(200);

        final List<LookupProvider.Kv> kv = new LinkedList<>();
        kv.add(pair("a","A"));
        kv.add(pair("b","B"));
        kv.add(pair("c","C"));

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
        fail("Not implemented");
    }

    @Test
    public void getKvSuccess() {
       addKvSuccess();

        assertEquals("B", given().
                header("Cookie", session).
                get("/lookups/v/{target}/{k}", "REGION", "b").
                then().statusCode(200).extract().asString());
    }

    @Test
    public void getKvMissing() {
        given().
                header("Cookie", session).
                get("/lookups/v/{target}/{k}", "REGION", "bbb").
                then().statusCode(204);
    }

    private static LookupProvider.Kv pair(String key, String value) {
        final LookupProvider.Kv kv = new LookupProvider.Kv();
        kv.setK(key);
        kv.setV(value);
        return kv;
    }

    private static LookupProvider.KvDef def(String code, String name, String table, String owner) {
        final LookupProvider.KvDef d = new LookupProvider.KvDef();
        d.setTable(table);
        d.setOwner(owner);
        d.setName(name);
        d.setCode(code);
        return d;
    }
}