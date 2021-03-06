package za.co.imqs.api.asset;

import com.jayway.restassured.http.ContentType;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import za.co.imqs.coreservice.dataaccess.exception.BusinessRuleViolationException;
import za.co.imqs.coreservice.dto.asset.AssetEnvelopeDto;

import java.util.UUID;

import static com.jayway.restassured.RestAssured.given;
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
public class AssetControllerUpdateAPITest extends AbstractAssetControllerAPITest {

    @Before
    public void clearAsset() {
        prepPermissions();

        deleteAssets(THE_ASSET);
    }

    @Test
    public void assetUpdateSuccess() throws Exception {
        final AssetEnvelopeDto envelope = (AssetEnvelopeDto) new CoreAssetBuilder(new AssetEnvelopeDto()).
                code("e1").
                name("Envelope 1").
                type("ENVELOPE").
                funcloc("at").
                dept("WCED").
                serial("1234").
                get();

        given().
                header("Cookie", login.getSession()).
                contentType(ContentType.JSON).body(envelope).
                put("/assets/{uuid}", THE_ASSET).
                then().assertThat().
                statusCode(HttpStatus.SC_CREATED);

        assertEquals(getAsset(THE_ASSET), envelope);

        final  AssetEnvelopeDto update = (AssetEnvelopeDto) new CoreAssetBuilder(new AssetEnvelopeDto()).
                type("ENVELOPE").
                serial("1234").
                get();
        envelope.setSerial_number("1234");

        given().
                header("Cookie", login.getSession()).
                contentType(ContentType.JSON).body(update).
                patch("/assets/{uuid}", THE_ASSET).
                then().assertThat().
                statusCode(HttpStatus.SC_OK);


        assertEquals(getAsset(THE_ASSET), envelope);
    }

    @Test
    public void assetUpdateChangeType() throws Exception {
        final AssetEnvelopeDto envelope = (AssetEnvelopeDto) new CoreAssetBuilder(new AssetEnvelopeDto()).
                code("e1").
                name("Envelope 1").
                type("ENVELOPE").
                funcloc("at").
                dept("WCED").
                serial("1234").
                get();

        given().
                header("Cookie", login.getSession()).
                contentType(ContentType.JSON).body(envelope).
                put("/assets/{uuid}", THE_ASSET).
                then().assertThat().
                statusCode(HttpStatus.SC_CREATED);

        assertEquals(getAsset(THE_ASSET), envelope);



        final  AssetEnvelopeDto update = (AssetEnvelopeDto) new CoreAssetBuilder(new AssetEnvelopeDto()).
                type("BUILDING").
                serial("1234").
                get();
        envelope.setSerial_number("1234");

        given().
                header("Cookie", login.getSession()).
                contentType(ContentType.JSON).body(update).
                patch("/assets/{uuid}", THE_ASSET).
                then().assertThat().
                statusCode(HttpStatus.SC_PRECONDITION_FAILED);


        assertEquals(getAsset(THE_ASSET), envelope);
    }


    @Test
    public void assetUpdateValidationFailure() throws Exception {
        final AssetEnvelopeDto envelope = (AssetEnvelopeDto) new CoreAssetBuilder(new AssetEnvelopeDto()).
                code("e1").
                name("Envelope 1").
                type("ENVELOPE").
                funcloc("at").
                dept("WCED").
                get();

        given().
                header("Cookie", login.getSession()).
                contentType(ContentType.JSON).body(envelope).
                put("/assets/{uuid}", THE_ASSET).
                then().assertThat().
                statusCode(HttpStatus.SC_CREATED);

        assertEquals(getAsset(THE_ASSET), envelope);

        final  AssetEnvelopeDto update = (AssetEnvelopeDto) new CoreAssetBuilder(new AssetEnvelopeDto()).
                type("ENVELOPE").
                geom("this is not valid").
                get();

        given().
                header("Cookie", login.getSession()).
                contentType(ContentType.JSON).body(update).
                patch("/assets/{uuid}", THE_ASSET).
                then().assertThat().
                statusCode(HttpStatus.SC_BAD_REQUEST);


        assertEquals(getAsset(THE_ASSET), envelope);
    }


    @Test
    public void assetUpdateBusinesseException() throws Exception  {
        expected.expect(BusinessRuleViolationException.class);
        fail("Not implemented");
        /*
        final AssetEnvelopeDto envelope = (AssetEnvelopeDto) new CoreAssetBuilder(new AssetEnvelopeDto()).
                code("e1").
                name("Envelope 1").
                type("ENVELOPE").
                funcloc("at").
                get();

        given().
                header("Cookie", login.getSession()).
                contentType(ContentType.JSON).body(envelope).
                put("/assets/{uuid}", THE_ASSET).
                then().assertThat().
                statusCode(HttpStatus.SC_CREATED);

        assertEquals(getAsset(THE_ASSET), envelope);

        final  AssetEnvelopeDto update = (AssetEnvelopeDto) new CoreAssetBuilder(new AssetEnvelopeDto()).
                type("ENVELOPE").
                funcloc("other.doesnotexist"). /
                get();

        given().
                header("Cookie", login.getSession()).
                contentType(ContentType.JSON).body(update).
                patch("/assets/{uuid}", THE_ASSET).
                then().assertThat().
                statusCode(HttpStatus.SC_PRECONDITION_FAILED);

        THIS DOES FAIL BUT AS A VALIDATION FAILURE

        assertEquals(getAsset(THE_ASSET), envelope);
        */
    }

    @Test
    public void assetUpdateForbidden()  throws Exception  {
        org.junit.Assume.assumeTrue(TEST_PERMISSIONS);
        fail("Not implemented");
    }

    @Test
    public void assetUpdateNotFound() throws Exception {
        final AssetEnvelopeDto envelope = (AssetEnvelopeDto) new CoreAssetBuilder(new AssetEnvelopeDto()).
                code("e1").
                name("Envelope 1").
                type("ENVELOPE").
                funcloc("at")
                .get();

        final  AssetEnvelopeDto update = (AssetEnvelopeDto) new CoreAssetBuilder(new AssetEnvelopeDto()).
                type("ENVELOPE").
                serial("1234").
                get();
        envelope.setSerial_number("1234");

        given().
                header("Cookie", login.getSession()).
                contentType(ContentType.JSON).body(update).
                patch("/assets/{uuid}", "33a47a2a-164b-4f72-a9c4-cad267b0e56a").
                then().assertThat().
                statusCode(HttpStatus.SC_BAD_REQUEST);

        assertNotFound(UUID.fromString("33a47a2a-164b-4f72-a9c4-cad267b0e56a"));
    }
}
