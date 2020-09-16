package za.co.imqs.api.asset;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import za.co.imqs.coreservice.dataaccess.exception.BusinessRuleViolationException;
import za.co.imqs.coreservice.dto.asset.AssetEnvelopeDto;
import za.co.imqs.coreservice.dto.asset.CoreAssetDto;

import java.util.UUID;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

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
public class AssetControllerCreateGroupingAPITest extends AbstractAssetControllerAPITest {
    private static final String EMIS_UUID = "4a6a4f78-2dc4-4b29-aa9e-5033b834a564";
    private static final UUID THE_OTHER_ASSET = UUID.fromString("c28fb90c-20b7-4ba1-a0bb-1008c18aacd8");

    private CoreAssetDto[] assets;

    @Before
    public void clearAssets() throws Exception {
        deleteAssets(THE_ASSET, THE_OTHER_ASSET);
        assets = createAssets();

        given().
                header("Cookie", session).
                delete("/assets/group/{uuid}/to/{grouping_id_type}/{grouping_id}", THE_ASSET, EMIS_UUID, THE_GROUPING_ID).
                then().assertThat().
                statusCode(HttpStatus.SC_OK);
        given().
                header("Cookie", session).
                delete("/assets/group/{uuid}/to/{grouping_id_type}/{grouping_id}", THE_OTHER_ASSET, EMIS_UUID, THE_GROUPING_ID).
                then().assertThat().
                statusCode(HttpStatus.SC_OK);
    }

    @After
    public void after() throws Exception {
        clearAssets();
    }

    @Test
    public void addAssetCreateLinkSuccess() throws Exception  {
        given().
                header("Cookie", session).
                put("/assets/group/{uuid}/to/{grouping_id_type}/{grouping_id}", THE_ASSET, EMIS_UUID, THE_GROUPING_ID).
                then().
                assertThat().statusCode(HttpStatus.SC_CREATED);

       assertLinked(THE_ASSET);
    }

    @Test
    public void addAssetCreateLinkValidationFailure() throws Exception  {
        fail("Not implemented");
    }

    @Test
    public void addAssetCreateLinkTwice() throws Exception  {
        given().
                header("Cookie", session).
                put("assets/group/{uuid}/to/{grouping_id_type}/{grouping_id}", THE_ASSET, EMIS_UUID, THE_GROUPING_ID).
                then().
                assertThat().statusCode(HttpStatus.SC_CREATED);

        assertLinked(THE_ASSET);

        given().
                header("Cookie", session).
                put("assets/group/{uuid}/to/{grouping_id_type}/{grouping_id}", THE_ASSET, EMIS_UUID, THE_GROUPING_ID).
                then().
                assertThat().statusCode(HttpStatus.SC_CREATED);

        assertLinked(THE_ASSET);
    }

    @Test
    public void addTwoAssetsCreateGrouping() {
        given().
                header("Cookie", session).
                put("assets/group/{uuid}/to/{grouping_id_type}/{grouping_id}", THE_ASSET, EMIS_UUID, THE_GROUPING_ID).
                then().
                assertThat().statusCode(HttpStatus.SC_CREATED);
        given().
                header("Cookie", session).
                put("assets/group/{uuid}/to/{grouping_id_type}/{grouping_id}", THE_OTHER_ASSET, EMIS_UUID, THE_GROUPING_ID).
                then().
                assertThat().statusCode(HttpStatus.SC_CREATED);

        assertLinked(THE_ASSET);
        assertLinked(THE_OTHER_ASSET);
    }

    @Test
    public void retrieveBasedOnGrouping() throws Exception {
        addTwoAssetsCreateGrouping();
        CoreAssetDto[] result = given().header("Cookie", session).
                get("assets/grouped_by/{grouping_id_type}/{grouping_id}", EMIS_UUID, THE_GROUPING_ID).
                then().assertThat().
                statusCode(HttpStatus.SC_OK).extract().as(CoreAssetDto[].class);
        assertEquals(result[0], assets[0]);
        assertEquals(result[1], assets[1]);
    }

    @Test
    public void addAssetCreateLinkBusinesseException() throws Exception  {
        expected.expect(BusinessRuleViolationException.class);
        fail("Not implemented");
    }

    @Test
    public void addAssetCreateLinkForbidden() throws Exception  {
        org.junit.Assume.assumeTrue(TEST_PERMISSIONS);
        fail("Not implemented");
    }


    private AssetEnvelopeDto[] createAssets() throws Exception {
        final AssetEnvelopeDto envelope = (AssetEnvelopeDto) new CoreAssetBuilder(new AssetEnvelopeDto()).
                code("e1").
                name("Envelope 1").
                type("ENVELOPE").
                funcloc("at")
                .get();


        putAsset(THE_ASSET, envelope);

        final AssetEnvelopeDto envelope2 = (AssetEnvelopeDto) new CoreAssetBuilder(new AssetEnvelopeDto()).
                code("e3").
                name("Envelope 3").
                type("ENVELOPE").
                funcloc("at3")
                .get();

        putAsset(THE_OTHER_ASSET, envelope2);
        return new AssetEnvelopeDto[]{envelope, envelope2};
    }

    private void assertLinked(UUID id) {
        Assert.assertEquals(THE_GROUPING_ID, given().header("Cookie", session).
                get("assets/group/{uuid}/to/{grouping_id_type}", id, EMIS_UUID).
                then().assertThat().
                statusCode(HttpStatus.SC_OK).extract().asString());
    }
}
