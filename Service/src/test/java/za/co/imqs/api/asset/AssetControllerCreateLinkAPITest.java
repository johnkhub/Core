package za.co.imqs.api.asset;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.*;
import za.co.imqs.coreservice.dataaccess.exception.BusinessRuleViolationException;
import za.co.imqs.coreservice.dto.asset.AssetEnvelopeDto;

import static com.jayway.restassured.RestAssured.given;
import static junit.framework.TestCase.assertTrue;
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
public class AssetControllerCreateLinkAPITest extends AbstractAssetControllerAPITest {

    @Before
    public void clearAsset() throws Exception {
        prepPermissions();

        deleteAssets(THE_ASSET);
        createAsset();

        given().
                header("Cookie", login.getSession()).
                delete("/assets/link/{uuid}/to/{external_id_type}/{external_id}", THE_ASSET, "c6a74a62-54f5-4f93-adf3-abebab3d3467", THE_EXTERNAL_ID);
    }

    @After
    public void after() throws Exception {
        clearAsset();
    }

    @Test
    public void addAssetCreateLinkSuccess() throws Exception  {
        given().
                header("Cookie", login.getSession()).
                put("/assets/link/{uuid}/to/{external_id_type}/{external_id}", THE_ASSET, "c6a74a62-54f5-4f93-adf3-abebab3d3467", THE_EXTERNAL_ID).
                then().
                assertThat().statusCode(HttpStatus.SC_CREATED);

       assertLinked();
    }

    @Test
    public void addAssetCreateLinkValidationFailure() throws Exception  {
        fail("Not implemented");
    }

    @Test
    @Ignore("At the moment we ignore duplicates")
    public void addAssetCreateLinkDuplicate() throws Exception  {
        given().
                header("Cookie", login.getSession()).
                put("assets/link/{uuid}/to/{external_id_type}/{external_id}", THE_ASSET, "c6a74a62-54f5-4f93-adf3-abebab3d3467", THE_EXTERNAL_ID).
                then().
                assertThat().statusCode(HttpStatus.SC_CREATED);

        assertLinked();

        given().
                header("Cookie", login.getSession()).
                put("assets/link/{uuid}/to/{external_id_type}/{external_id}", THE_ASSET, "c6a74a62-54f5-4f93-adf3-abebab3d3467", THE_EXTERNAL_ID).
                then().
                assertThat().statusCode(HttpStatus.SC_CONFLICT);

        assertLinked();
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


    private AssetEnvelopeDto createAsset() throws Exception {
        final AssetEnvelopeDto envelope = (AssetEnvelopeDto) new CoreAssetBuilder(new AssetEnvelopeDto()).
                code("e1").
                name("Envelope 1").
                type("ENVELOPE").
                funcloc("at").
                dept("WCED")
                .get();


       putAsset(THE_ASSET, envelope);

        // read and verify contents
        assertEquals(getAsset(THE_ASSET), envelope);
        return envelope;
    }

    private void assertLinked() {
        assertTrue(given().
                header("Cookie", login.getSession()).
                get("assets/link/{uuid}/to/{external_id_type}", THE_ASSET, "c6a74a62-54f5-4f93-adf3-abebab3d3467").
                then().assertThat().
                statusCode(HttpStatus.SC_OK).extract().asString().equals(THE_EXTERNAL_ID));
    }
}
