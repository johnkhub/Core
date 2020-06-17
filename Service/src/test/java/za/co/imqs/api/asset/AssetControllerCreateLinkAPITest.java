package za.co.imqs.api.asset;

import com.jayway.restassured.http.ContentType;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import za.co.imqs.coreservice.dto.AssetEnvelopeDto;
import za.co.imqs.coreservice.dto.CoreAssetDto;

import java.util.UUID;

import static com.jayway.restassured.RestAssured.given;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.*;
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
    private static final String THE_EXTERNAL_ID = "c45036b1-a1fb-44f4-a254-a668c0d09eaa";

    @Before
    public void clearAsset() throws Exception {
        given().
                header("Cookie", session).
                delete("/assets/testing/{uuid}", THE_ASSET).
                then().assertThat().
                statusCode(HttpStatus.SC_OK);
        createAsset();

        given().
                header("Cookie", session).
                delete("/assets/testing/link/{uuid}/to/{external_id_type}/{external_id}", THE_ASSET, "c6a74a62-54f5-4f93-adf3-abebab3d3467", THE_EXTERNAL_ID).
                then().assertThat().
                statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void addAssetCreateLinkSuccess() throws Exception  {
        given().
                header("Cookie", session).
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
                header("Cookie", session).
                put("assets/link/{uuid}/to/{external_id_type}/{external_id}", THE_ASSET, "c6a74a62-54f5-4f93-adf3-abebab3d3467", THE_EXTERNAL_ID).
                then().
                assertThat().statusCode(HttpStatus.SC_CREATED);

        assertLinked();

        given().
                header("Cookie", session).
                put("assets/link/{uuid}/to/{external_id_type}/{external_id}", THE_ASSET, "c6a74a62-54f5-4f93-adf3-abebab3d3467", THE_EXTERNAL_ID).
                then().
                assertThat().statusCode(HttpStatus.SC_CONFLICT);

        assertLinked();
    }

    @Test
    public void addAssetCreateLinkBusinesseException() throws Exception  {
        fail("Not implemented");
    }

    @Test
    public void addAssetCreateLinkForbidden() throws Exception  {
        fail("Not implemented");
    }


    private AssetEnvelopeDto createAsset() throws Exception {
        final AssetEnvelopeDto envelope = (AssetEnvelopeDto) new CoreAssetBuilder(new AssetEnvelopeDto()).
                code("e1").
                name("Envelope 1").
                type("ENVELOPE").
                funcloc("at")
                .get();


        given().
                header("Cookie", session).
                contentType(ContentType.JSON).body(envelope).
                put("/assets/{uuid}", THE_ASSET).
                then().assertThat().
                statusCode(HttpStatus.SC_CREATED);

        // read and verify contents
        assertEquals(getAsset(THE_ASSET),envelope);
        return envelope;
    }

    private void assertLinked() {
        assertTrue(given().
                header("Cookie", session).
                get("assets/testing/link/{uuid}/to/{external_id_type}", THE_ASSET, "c6a74a62-54f5-4f93-adf3-abebab3d3467").
                then().assertThat().
                statusCode(HttpStatus.SC_OK).extract().as(String[].class)[0].equals(THE_EXTERNAL_ID));
    }
}
