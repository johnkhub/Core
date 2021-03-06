package za.co.imqs.api.asset;

import com.jayway.restassured.http.ContentType;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.*;
import za.co.imqs.coreservice.dataaccess.LookupProvider;
import za.co.imqs.coreservice.dto.asset.AssetEnvelopeDto;
import za.co.imqs.coreservice.dto.asset.AssetFacilityDto;
import za.co.imqs.coreservice.dto.asset.AssetLandparcelDto;

import java.util.*;

import static com.jayway.restassured.RestAssured.given;
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
public class AssetControllerLandparcelLinkAPITest extends AbstractAssetControllerAPITest {
    private static final UUID ENVELOPE = UUID.fromString("63d905b0-7efd-4873-b10f-1da649cc0961");
    private static final UUID LANDPARCEL = UUID.fromString("93795d63-f5fd-4e87-b138-8388275cd721");
    private static final UUID FACILITY1 = UUID.fromString("f5b98b7e-dc41-4e26-a26e-df02cb9b601a");
    private static final UUID FACILITY2 = UUID.fromString("2739c56a-12b8-4e0a-98e4-7f3c5c88526a");

    @Before
    public void init() throws Exception {
        prepPermissions();

        after();
        populate();
    }


    @After
    @Override
    public void after() throws Exception{
        super.after();
        int code = given().
                header("Cookie", login.getSession()).
                delete("/assets/landparcel/{landparcel_id}/asset/{asset_id}", LANDPARCEL, FACILITY1).statusCode();
        Assert.assertTrue(code == HttpStatus.SC_OK || code == HttpStatus.SC_FORBIDDEN);
        code = given().
                header("Cookie", login.getSession()).
                delete("/assets/landparcel/{landparcel_id}/asset/{asset_id}", LANDPARCEL, FACILITY2).statusCode();
        Assert.assertTrue(code == HttpStatus.SC_OK || code == HttpStatus.SC_FORBIDDEN);
        deleteAssets(FACILITY1, FACILITY2, LANDPARCEL, ENVELOPE);
    }

    @Test
    public void createLinkSuccess() throws Exception  {
        //Assert.assertEquals(2, getLinkTotalLinked());

        given().
                header("Cookie", login.getSession()).
                put("/assets/landparcel/{landparcel_id}/asset/{asset_id}", LANDPARCEL, FACILITY1).
                then().assertThat().statusCode(HttpStatus.SC_CREATED);

        given().
                header("Cookie", login.getSession()).
                put("/assets/landparcel/{landparcel_id}/asset/{asset_id}", LANDPARCEL, FACILITY2).
                then().assertThat().statusCode(HttpStatus.SC_CREATED);


        final Set<UUID> linked = getLinkedTo(LANDPARCEL);
        Assert.assertEquals(2, linked.size());
        Assert.assertTrue(linked.contains(FACILITY1));
        Assert.assertTrue(linked.contains(FACILITY2));
        //Assert.assertEquals(2, getLinkTotalLinked());
    }

    @Test
    public void createLinkDuplicate() throws Exception  {
        createLinkSuccess();

        given().
                header("Cookie", login.getSession()).
                put("/assets/landparcel/{landparcel_id}/asset/{asset_id}", LANDPARCEL, FACILITY1).
                then().assertThat().statusCode(HttpStatus.SC_CREATED);
        given().
                header("Cookie", login.getSession()).
                put("/assets/landparcel/{landparcel_id}/asset/{asset_id}", LANDPARCEL, FACILITY2).
                then().assertThat().statusCode(HttpStatus.SC_CREATED);

        final Set<UUID> linked = getLinkedTo(LANDPARCEL);
        Assert.assertEquals(2, linked.size());
        Assert.assertTrue(linked.contains(FACILITY1));
        Assert.assertTrue(linked.contains(FACILITY2));
        //Assert.assertEquals(2, getLinkTotalLinked());
    }

    @Test
    public void removeLink() throws Exception  {
        createLinkSuccess();

        given().
                header("Cookie", login.getSession()).
                delete("/assets/landparcel/{landparcel_id}/asset/{asset_id}", LANDPARCEL, FACILITY2).
                then().assertThat().statusCode(HttpStatus.SC_OK);

        final Set<UUID> linked = getLinkedTo(LANDPARCEL);
        Assert.assertEquals(1, linked.size());
        Assert.assertTrue(linked.contains(FACILITY1));
        //Assert.assertEquals(1, getLinkTotalLinked());
    }

    @Test
    public void removeLinkNotExist() throws Exception  {
        createLinkSuccess();

        given().
                header("Cookie", login.getSession()).
                put("/assets/landparcel/{landparcel_id}/asset/{asset_id}", LANDPARCEL, UUID.randomUUID()).
                then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST);

        final Set<UUID> linked = getLinkedTo(LANDPARCEL);
        Assert.assertEquals(2, linked.size());
        Assert.assertTrue(linked.contains(FACILITY1));
        Assert.assertTrue(linked.contains(FACILITY2));
        //Assert.assertEquals(2, getLinkTotalLinked());
    }

    @Test
    public void createLinkForbidden() throws Exception  {
        org.junit.Assume.assumeTrue(TEST_PERMISSIONS);
        fail("Not implemented");
    }

    @Test
    public void removeLinkForbidden() throws Exception  {
        org.junit.Assume.assumeTrue(TEST_PERMISSIONS);
        fail("Not implemented");
    }

    private Set<UUID> getLinkedTo(UUID landparcel) {
        final Set<UUID> values = new HashSet<>();
        for (UUID u : given().
                header("Cookie", login.getSession()).
                get("/assets/landparcel/{uuid}/assets", landparcel).
                then().assertThat().
                statusCode(HttpStatus.SC_OK).
                extract().as(UUID[].class)) {
            values.add(u);

        }
        return values;
    }
    private int getLinkTotalLinked() {
        return 0;
    }

    private void populate() {
        final List<LookupProvider.Kv> kv = new LinkedList<>();
        kv.add(pair("OFF","Office"));
        given().
                header("Cookie", login.getSession()).
                contentType(ContentType.JSON).body(kv).
                put("/lookups/kv/{target}", "FACIL_TYPE").
                then().statusCode(200);

        final AssetEnvelopeDto envelope = (AssetEnvelopeDto) new CoreAssetBuilder(new AssetEnvelopeDto()).
                code("e1").
                name("Envelope 1").
                type("ENVELOPE").
                funcloc("e1").
                dept("WCED")
                .get();

        final AssetLandparcelDto landparcel = (AssetLandparcelDto) new CoreAssetBuilder(new AssetLandparcelDto()).
                code("l1").
                name("Landparcel 1").
                type("LANDPARCEL").
                funcloc("e1.l1").
                dept("WCED")
                .get();

        final AssetFacilityDto facility1 = (AssetFacilityDto) new CoreAssetBuilder(new AssetFacilityDto()).
                code("f1").
                name("facility 1").
                type("FACILITY").
                funcloc("e1.f1").
                dept("WCED").
                get();
        facility1.setFacility_type_code("OFF");

        final AssetFacilityDto facility2 = (AssetFacilityDto) new CoreAssetBuilder(new AssetFacilityDto()).
                code("f2").
                name("facility 1").
                type("FACILITY").
                funcloc("e1.f2").
                dept("WCED").
                get();
        facility2.setFacility_type_code("OFF");

        putAsset(ENVELOPE, envelope);
        putAsset(LANDPARCEL, landparcel);
        putAsset(FACILITY1, facility1);
        putAsset(FACILITY2, facility2);
    }
}
