package za.co.imqs.api.asset;

import com.jayway.restassured.http.ContentType;
import org.apache.commons.httpclient.HttpStatus;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import za.co.imqs.TestUtils;
import za.co.imqs.coreservice.dataaccess.LookupProvider;
import za.co.imqs.coreservice.dto.asset.AssetBuildingDto;
import za.co.imqs.coreservice.dto.asset.AssetEnvelopeDto;
import za.co.imqs.coreservice.dto.asset.AssetFacilityDto;
import za.co.imqs.coreservice.dto.asset.CoreAssetDto;
import za.co.imqs.coreservice.imports.Importer;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static za.co.imqs.coreservice.dataaccess.LookupProvider.Kv.pair;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/27
 *
 * We have a class per API method.
 * We have a method for every possible return code.
 * Consider a test case for each field as well to test validation.
 */
public class AssetControllerQueryAPITest extends AbstractAssetControllerAPITest {
    // NOTE:
    //      get by uuid  is tested implicitly by the other Asset API tests
    //      get asset link is tested implicitly by asset link tests

    private static final UUID THE_ASSET2 = UUID.fromString("8b16aeb2-4681-41a6-b270-3cc3aeaea822");
    private static final UUID THE_FACILITY = UUID.fromString("d9b8ee54-067b-4879-b4f1-58760b5d3ec1");
    private static final UUID THE_BUILDING = UUID.fromString("691bc006-d1c1-4f05-960b-c20bf22a0f55");

    // TODO this must become programmatic
    private static final String CONFIG = TestUtils.resolveWorkingFolder() + "/src/test/resources/import_config.json";

    @Before
    public void clearAsset() throws Exception{
        Importer.main(new String[]{CONFIG, "lookups", TestUtils.resolveWorkingFolder() + "/src/test/resources/lookups/ref_facility_type.csv", "FACIL_TYPE"});
        deleteAssets(THE_BUILDING, THE_FACILITY, THE_ASSET, THE_ASSET2);
    }

    @After
    public void after() throws Exception {
        deleteAssets(THE_BUILDING, THE_FACILITY, THE_ASSET, THE_ASSET2);
    }


    @Test
    public void getByFuncLocPath() throws Exception {
        final AssetEnvelopeDto envelope = (AssetEnvelopeDto) new CoreAssetBuilder(new AssetEnvelopeDto()).
                code("e1").
                name("Envelope 1").
                type("ENVELOPE").
                funcloc("e1").
                serial("1234").
                dept("WCED").
                get();
        given().
                header("Cookie", session).
                contentType(ContentType.JSON).body(envelope).
                put("/assets/{uuid}", THE_ASSET).
                then().assertThat().
                statusCode(HttpStatus.SC_CREATED);

        final AssetFacilityDto facility = new AssetFacilityDto();
        facility.setFacility_type_code("LAND");
        new CoreAssetBuilder(facility).
                code("e1-f1").
                name("Facility 1").
                type("FACILITY").
                funcloc("e1.f1").
                serial("1235").
                dept("WCED").
                get();


        given().
                header("Cookie", session).
                contentType(ContentType.JSON).body(facility).
                put("/assets/{uuid}", THE_FACILITY).
                then().assertThat().
                statusCode(HttpStatus.SC_CREATED);


        CoreAssetDto asset = given().
                header("Cookie", session).
                get("/assets/func_loc_path/{path}", "e1.f1".replace(".","+")).
                then().
                assertThat().statusCode(HttpStatus.SC_OK).extract().as(CoreAssetDto.class);

        assertEquals(asset, facility);
    }

    @Test
    public void getByFuncLocPathNotExist() throws Exception {
        final AssetEnvelopeDto envelope = (AssetEnvelopeDto) new CoreAssetBuilder(new AssetEnvelopeDto()).
                code("e1").
                name("Envelope 1").
                type("ENVELOPE").
                funcloc("at").
                serial("1234").
                dept("WCED").
                get();

        given().
                header("Cookie", session).
                contentType(ContentType.JSON).body(envelope).
                put("/assets/{uuid}", THE_ASSET).
                then().assertThat().
                statusCode(HttpStatus.SC_CREATED);

        given().
                header("Cookie", session).
                get("/assets/func_loc_path/{path}", "nat".replace(".","+")).
                then().
                assertThat().statusCode(HttpStatus.SC_NOT_FOUND);

    }



    @Test
    public void getByLink() throws Exception {
        final AssetEnvelopeDto envelope = (AssetEnvelopeDto) new CoreAssetBuilder(new AssetEnvelopeDto()).
                code("e1").
                name("Envelope 1").
                type("ENVELOPE").
                funcloc("at").
                serial("1234").
                dept("WCED").
                get();

        given().
                header("Cookie", session).
                contentType(ContentType.JSON).body(envelope).
                put("/assets/{uuid}", THE_ASSET).
                then().assertThat().
                statusCode(HttpStatus.SC_CREATED);

        given().
                header("Cookie", session).
                put("/assets/link/{uuid}/to/{external_id_type}/{external_id}", THE_ASSET, "c6a74a62-54f5-4f93-adf3-abebab3d3467", THE_EXTERNAL_ID).
                then().
                assertThat().statusCode(HttpStatus.SC_CREATED);


        CoreAssetDto asset = given().
                header("Cookie", session).
                get("/assets/linked_to/{external_id_type}/{external_id}", "c6a74a62-54f5-4f93-adf3-abebab3d3467", THE_EXTERNAL_ID).
                then().
                assertThat().statusCode(HttpStatus.SC_OK).extract().as(CoreAssetDto.class);

        assertEquals(asset, envelope);
    }


    @Test
    public void getByLinkNotExist() throws Exception {
        final AssetEnvelopeDto envelope = (AssetEnvelopeDto) new CoreAssetBuilder(new AssetEnvelopeDto()).
                code("e1").
                name("Envelope 1").
                type("ENVELOPE").
                funcloc("at").
                serial("1234").
                dept("WCED").
                get();

        given().
                header("Cookie", session).
                contentType(ContentType.JSON).body(envelope).
                put("/assets/{uuid}", THE_ASSET).
                then().assertThat().
                statusCode(HttpStatus.SC_CREATED);

        given().
                header("Cookie", session).
                put("/assets/link/{uuid}/to/{external_id_type}/{external_id}", THE_ASSET, "c6a74a62-54f5-4f93-adf3-abebab3d3467", THE_EXTERNAL_ID).
                then().
                assertThat().statusCode(HttpStatus.SC_CREATED);


        given().
                header("Cookie", session).
                get("/assets/linked_to/{external_id_type}/{external_id}", "c6a74a62-54f5-4f93-adf3-abebab3d3467", UUID.randomUUID()).
                then().
                assertThat().statusCode(HttpStatus.SC_NOT_FOUND);
    }


    @Test
    public void queryOnNameExact() throws Exception {
        final AssetEnvelopeDto envelope = populate();

        CoreAssetDto[] dtos = given().
                header("Cookie", session).
                queryParam("filter", "name='Envelope 1'").
                queryParam("offset", 0).
                queryParam("limit", 10).
                queryParam("orderby", "func_loc_path").
                //queryParam("groupby", "func_loc_path").
                get("/assets/query").
                then().assertThat().
                statusCode(HttpStatus.SC_OK).assertThat().extract().as(CoreAssetDto[].class);

        assertEquals(dtos[0], envelope);
    }

    @Test
    public void queryOnLikeExact() throws Exception {
        final AssetEnvelopeDto envelope = populate();

        CoreAssetDto[] dtos = given().
                header("Cookie", session).
                queryParam("filter", "name LIKE 'Envelope 1'").
                get("/assets/query").
                then().assertThat().
                statusCode(HttpStatus.SC_OK).assertThat().extract().as(CoreAssetDto[].class);

        assertEquals(dtos[0], envelope);
    }

    @Test
    public void queryOnLikeWildcard() throws Exception {
        final AssetEnvelopeDto envelope = populate();

        CoreAssetDto[] dtos = given().
                header("Cookie", session).
                queryParam("filter", "name LIKE 'Envelope %'").
                get("/assets/query").
                then().assertThat().
                statusCode(HttpStatus.SC_OK).assertThat().extract().as(CoreAssetDto[].class);

        assertEquals(dtos[0], envelope);
    }

    @Test
    public void queryOnNameLower() throws Exception {
        final AssetEnvelopeDto envelope = populate();

        CoreAssetDto[] dtos = given().
                header("Cookie", session).
                queryParam("filter", "LOWER(name)='envelope 1'").
                queryParam("offset", 0).
                queryParam("limit", 10).
                queryParam("orderby", "func_loc_path").
                //queryParam("groupby", "func_loc_path").
                get("/assets/query").
                then().assertThat().
                statusCode(HttpStatus.SC_OK).assertThat().extract().as(CoreAssetDto[].class);

        assertEquals(dtos[0], envelope);
    }

    @Test
    // Representative of boolean data type
    public void queryOwned() throws Exception {
        final AssetEnvelopeDto envelope = populate();

        CoreAssetDto[] dtos = given().
                header("Cookie", session).
                queryParam("filter","name='Envelope 1' and is_owned = true").
                queryParam("offset", 0).
                queryParam("limit", 10).
                queryParam("orderby", "func_loc_path").
                //queryParam("groupby", "func_loc_path").
                get("/assets/query").
                then().assertThat().
                statusCode(HttpStatus.SC_OK).assertThat().extract().as(CoreAssetDto[].class);

        assertEquals(dtos[0], envelope);
    }

    @Test
    public void queryNotOwned() throws Exception {
        final AssetEnvelopeDto envelope2 = (AssetEnvelopeDto) new CoreAssetBuilder(new AssetEnvelopeDto()).
                code("e2").
                name("Envelope 2").
                type("ENVELOPE").
                funcloc("at1").
                serial("1235").
                dept("WCED").
                get();
        envelope2.setIs_owned(false);
        putAsset(THE_ASSET2, envelope2);

        CoreAssetDto[] dtos = given().
                header("Cookie", session).
                queryParam("filter","name='Envelope 2' and is_owned = false").
                queryParam("offset", 0).
                queryParam("limit", 10).
                queryParam("orderby", "func_loc_path").
                //queryParam("groupby", "func_loc_path").
                get("/assets/query").
                then().assertThat().
                statusCode(HttpStatus.SC_OK).assertThat().extract().as(CoreAssetDto[].class);

        assertEquals(dtos[0], envelope2);
    }

    @Test
    // Representative of date time
    public void queryCreateTimePast() throws Exception {
        final AssetEnvelopeDto envelope = populate();

        CoreAssetDto[] dtos = given().
                header("Cookie", session).
                queryParam("filter", "name='Envelope 1' and creation_date < '" + new DateTime() +  "'").
                queryParam("offset", 0).
                queryParam("limit", 10).
                queryParam("orderby", "func_loc_path").
                //queryParam("groupby", "func_loc_path").
                get("/assets/query").
                then().assertThat().
                statusCode(HttpStatus.SC_OK).assertThat().extract().as(CoreAssetDto[].class);

        assertEquals(dtos[0], envelope);
    }

    @Test
    public void queryCreateTimeFuture() throws Exception {
        populate();

        CoreAssetDto[] dtos = given().
                header("Cookie", session).
                queryParam("filter", "name='Envelope 1' and creation_date > '" + new DateTime().plusDays(5) +  "'").
                queryParam("offset", 0).
                queryParam("limit", 10).
                queryParam("orderby", "func_loc_path").
                //queryParam("groupby", "func_loc_path").
                get("/assets/query").
                then().assertThat().
                statusCode(HttpStatus.SC_OK).extract().as(CoreAssetDto[].class);

        assertTrue(dtos.length == 0);
    }

    @Test
    public void queryByAssetTypeCodeFound() throws Exception {
        populate();

        CoreAssetDto[] dtos = given().
                header("Cookie", session).
                queryParam("filter", "name='Envelope 1' and (asset_type_code = 'ENVELOPE')").
                queryParam("offset", 0).
                queryParam("limit", 10).
                queryParam("orderby", "func_loc_path").
                //queryParam("groupby", "func_loc_path").
                get("/assets/query").
                then().assertThat().
                statusCode(HttpStatus.SC_OK).extract().as(CoreAssetDto[].class);

        assertTrue(dtos.length == 1);
    }

    @Test
    public void queryByAssetTypeCodeNotFound() throws Exception {
        final AssetEnvelopeDto envelope = populate();

        CoreAssetDto[] dtos = given().
                header("Cookie", session).
                queryParam("filter","name='Envelope 1' and (asset_type_code = 'KETTLE')").
                queryParam("offset", 0).
                queryParam("limit", 10).
                queryParam("orderby", "func_loc_path").
                //queryParam("groupby", "func_loc_path").
                get("/assets/query").
                then().assertThat().
                statusCode(HttpStatus.SC_OK).extract().as(CoreAssetDto[].class);

        assertTrue(dtos.length == 0);
    }

    @Test
    // Representative of path queries
    public void queryFindParents() throws Exception {
        final AssetEnvelopeDto envelope =
                (AssetEnvelopeDto) new CoreAssetBuilder(new AssetEnvelopeDto()).code("e1").name("Envelope 1").type("ENVELOPE").funcloc("e1").serial("1234").get();

        given().
                header("Cookie", session).contentType(ContentType.JSON).body(envelope).
                put("/assets/{uuid}", THE_ASSET).
                then().assertThat().statusCode(HttpStatus.SC_CREATED);

        Importer.main(new String[]{CONFIG, "lookups", TestUtils.resolveWorkingFolder() + "/src/test/resources/lookups/ref_facility_type.csv", "FACIL_TYPE"});

        final AssetFacilityDto facility = new AssetFacilityDto();
        facility.setFacility_type_code("LAND");
        new CoreAssetBuilder(facility).code("e1-f1").name("Facility 1").type("FACILITY").funcloc("e1.f1").serial("1235").get();

        given().
                header("Cookie", session).contentType(ContentType.JSON).body(facility).
                put("/assets/{uuid}", THE_FACILITY).
                then().assertThat().statusCode(HttpStatus.SC_CREATED);

        final AssetBuildingDto building = new AssetBuildingDto();
        new CoreAssetBuilder(building).code("e1-f1-b1").name("Building 1").type("BUILDING").funcloc("e1.f1.b1").serial("1236").get();

        given().
                header("Cookie", session).contentType(ContentType.JSON).body(building).
                put("/assets/{uuid}", THE_BUILDING).
                then().assertThat().
                statusCode(HttpStatus.SC_CREATED);

        CoreAssetDto[] dtos = given().
                header("Cookie", session).
                queryParam
                        ("filter", "func_loc_path > @('e1.f1.b1')"
                        ).
                queryParam("offset", 0).
                queryParam("limit", 10).
                queryParam("orderby", "func_loc_path").
                //queryParam("groupby", "func_loc_path").
                get("/assets/query").
                then().assertThat().
                statusCode(HttpStatus.SC_OK).extract().as(CoreAssetDto[].class);

        Assert.assertEquals(3, dtos.length);
        assertEquals(dtos[0], envelope);
        assertEquals(dtos[1], facility);
        assertEquals(dtos[2], building);
    }

    @Test
    public void queryFindChildren() throws Exception {
        final AssetEnvelopeDto envelope =
                (AssetEnvelopeDto) new CoreAssetBuilder(new AssetEnvelopeDto()).code("e1").name("Envelope 1").type("ENVELOPE").funcloc("e1").serial("1234").get();

        given().
                header("Cookie", session).contentType(ContentType.JSON).body(envelope).
                put("/assets/{uuid}", THE_ASSET).
                then().assertThat().statusCode(HttpStatus.SC_CREATED);

        Importer.main(new String[]{CONFIG, "lookups", TestUtils.resolveWorkingFolder() + "/src/test/resources/lookups/ref_facility_type.csv", "FACIL_TYPE"});

        final AssetFacilityDto facility = new AssetFacilityDto();
        facility.setFacility_type_code("LAND");
        new CoreAssetBuilder(facility).code("e1-f1").name("Facility 1").type("FACILITY").funcloc("e1.f1").serial("1235").get();


        given().
                header("Cookie", session).contentType(ContentType.JSON).body(facility).
                put("/assets/{uuid}", THE_FACILITY).
                then().assertThat().statusCode(HttpStatus.SC_CREATED);

        final AssetBuildingDto building = new AssetBuildingDto();
        new CoreAssetBuilder(building).code("e1-f1-b1").name("Building 1").type("BUILDING").funcloc("e1.f1.b1").serial("1236").get();


        given().
                header("Cookie", session).contentType(ContentType.JSON).body(building).
                put("/assets/{uuid}", THE_BUILDING).
                then().assertThat().
                statusCode(HttpStatus.SC_CREATED);

        CoreAssetDto[] dtos = given().
                header("Cookie", session).
                queryParam("filter","func_loc_path < @('e1.f1')").
                queryParam("offset", 0).
                queryParam("limit", 10).
                queryParam("orderby", "func_loc_path").
                //queryParam("groupby", "func_loc_path").
                get("/assets/query").
                then().assertThat().statusCode(HttpStatus.SC_OK).extract().as(CoreAssetDto[].class);

        Assert.assertEquals(2, dtos.length);
        assertEquals(dtos[0], facility);
        assertEquals(dtos[1], building);
    }

    @Test
    public void queryByTags() throws Exception {
        // Add tags
        final List<LookupProvider.Kv> kv = new LinkedList<>();
        kv.add(pair("TAG1","TagO OneO"));
        kv.add(pair("TAG2","TagO TwoO"));
        kv.add(pair("TAG3","TagO ThreeO"));

        given().
                header("Cookie", session).
                contentType(ContentType.JSON).body(kv).
                put("/lookups/kv/{target}", "TAGS").
                then().statusCode(200);

        final AssetEnvelopeDto envelope = populate();

        given().
                header("Cookie", session).
                put("/assets/{uuid}/tag/{tag}", THE_ASSET, "TAG1").
                then().assertThat().
                statusCode(HttpStatus.SC_OK);
        given().
                header("Cookie", session).
                put("/assets/{uuid}/tag/{tag}", THE_ASSET, "TAG2").
                then().assertThat().
                statusCode(HttpStatus.SC_OK);


        final AssetFacilityDto facility = new AssetFacilityDto();
        facility.setFacility_type_code("LAND");
        new CoreAssetBuilder(facility).code("e1-f1").name("Facility 1").type("FACILITY").funcloc("e1.f1").serial("1235").get();
        putAsset(THE_FACILITY, facility);

        given().
                header("Cookie", session).
                put("/assets/{uuid}/tag/{tag}", THE_FACILITY, "TAG1").
                then().assertThat().
                statusCode(HttpStatus.SC_OK);

        CoreAssetDto[] dtos = given().
                header("Cookie", session).
                queryParam("filter","TAGS['TAG1']").
                queryParam("offset", 0).
                queryParam("limit", 10).
                queryParam("orderby", "func_loc_path").
                //queryParam("groupby", "func_loc_path").
                get("/assets/query").
                then().assertThat().statusCode(HttpStatus.SC_OK).extract().as(CoreAssetDto[].class);

        Assert.assertEquals(2, dtos.length);
        assertEquals(dtos[0], envelope);
        assertEquals(dtos[1], facility);

        dtos = given().
                header("Cookie", session).
                queryParam("filter","TAGS['TAG1','TAG2']").
                queryParam("offset", 0).
                queryParam("limit", 10).
                queryParam("orderby", "func_loc_path").
                //queryParam("groupby", "func_loc_path").
                get("/assets/query").
                then().assertThat().statusCode(HttpStatus.SC_OK).extract().as(CoreAssetDto[].class);

        Assert.assertEquals(1, dtos.length);
        assertEquals(dtos[0], envelope);
    }

    @Test
    public void queryViaDeptTree() throws Exception {
        Importer.main(new String[]{CONFIG, "lookups", TestUtils.resolveWorkingFolder() + "/src/test/resources/lookups/ref_branch.csv", "BRANCH"});
        Importer.main(new String[]{CONFIG, "lookups", TestUtils.resolveWorkingFolder() + "/src/test/resources/lookups/ref_chief_directorate.csv", "CHIEF_DIR"});
        Importer.main(new String[]{CONFIG, "lookups", TestUtils.resolveWorkingFolder() + "/src/test/resources/lookups/ref_client_department.csv", "CLIENT_DEP"});

        final AssetEnvelopeDto envelope = (AssetEnvelopeDto) new CoreAssetBuilder(new AssetEnvelopeDto()).
                code("e1").
                name("Envelope 1").
                type("ENVELOPE").
                funcloc("at").
                serial("1234").
                dept("WCED").
                get();
        envelope.setIs_owned(true);
        putAsset(THE_ASSET, envelope);

        CoreAssetDto[] dtos = given().
                header("Cookie", session).
                queryParam("filter", "name='Envelope 1' and (responsible_dept_classif = @('B_PPW.CD_EI.WCED'))").
                queryParam("offset", 0).
                queryParam("limit", 10).
                queryParam("orderby", "func_loc_path").
                //queryParam("groupby", "func_loc_path").
                get("/assets/query").
                then().assertThat().
                statusCode(HttpStatus.SC_OK).extract().as(CoreAssetDto[].class);

        assertTrue(dtos.length == 1);
        assertEquals(dtos[0], envelope);
    }


    private AssetEnvelopeDto populate() {
        prepPermissions();
        final AssetEnvelopeDto envelope = (AssetEnvelopeDto) new CoreAssetBuilder(new AssetEnvelopeDto()).
                code("e1").
                name("Envelope 1").
                type("ENVELOPE").
                funcloc("at").
                serial("1234").
                dept("WCED").
                get();
        envelope.setIs_owned(true);
        putAsset(THE_ASSET, envelope);

        return envelope;
    }
}
