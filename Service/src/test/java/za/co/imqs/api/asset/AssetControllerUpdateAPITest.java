package za.co.imqs.api.asset;

import com.jayway.restassured.http.ContentType;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import za.co.imqs.coreservice.dto.AssetEnvelopeDto;

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
        given().
                header("Cookie", session).
                delete("/assets/testing/{uuid}", THE_ASSET).
                then().assertThat().
                statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void assetUpdateSuccess() throws Exception {
        final AssetEnvelopeDto envelope = (AssetEnvelopeDto) new CoreAssetBuilder(new AssetEnvelopeDto()).
                code("e1").
                name("Envelope 1").
                type("ENVELOPE").
                funcloc("at").
                serial("1234").
                get();

        given().
                header("Cookie", session).
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
                header("Cookie", session).
                contentType(ContentType.JSON).body(update).
                patch("/assets/{uuid}", THE_ASSET).
                then().assertThat().
                statusCode(HttpStatus.SC_OK);


        assertEquals(getAsset(THE_ASSET), envelope);
    }

    @Test
    public void assetUpdateValidationFailure() throws Exception {
        final AssetEnvelopeDto envelope = (AssetEnvelopeDto) new CoreAssetBuilder(new AssetEnvelopeDto()).
                code("e1").
                name("Envelope 1").
                type("ENVELOPE").
                funcloc("at").
                get();

        given().
                header("Cookie", session).
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
                header("Cookie", session).
                contentType(ContentType.JSON).body(update).
                patch("/assets/{uuid}", THE_ASSET).
                then().assertThat().
                statusCode(HttpStatus.SC_BAD_REQUEST);


        assertEquals(getAsset(THE_ASSET), envelope);
    }

    @Test
    public void assetUpdateBusinesseException() throws Exception  {
        final AssetEnvelopeDto envelope = (AssetEnvelopeDto) new CoreAssetBuilder(new AssetEnvelopeDto()).
                code("e1").
                name("Envelope 1").
                type("ENVELOPE").
                funcloc("at").
                get();

        given().
                header("Cookie", session).
                contentType(ContentType.JSON).body(envelope).
                put("/assets/{uuid}", THE_ASSET).
                then().assertThat().
                statusCode(HttpStatus.SC_CREATED);

        assertEquals(getAsset(THE_ASSET), envelope);

        final  AssetEnvelopeDto update = (AssetEnvelopeDto) new CoreAssetBuilder(new AssetEnvelopeDto()).
                type("ENVELOPE").
                funcloc("other.doesnotexist"). // this should fail as soon as we add the constraint!
                get();

        given().
                header("Cookie", session).
                contentType(ContentType.JSON).body(update).
                patch("/assets/{uuid}", THE_ASSET).
                then().assertThat().
                statusCode(HttpStatus.SC_PRECONDITION_FAILED);


        assertEquals(getAsset(THE_ASSET), envelope);
    }

    @Test
    public void assetUpdateForbidden()  throws Exception  {
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
                header("Cookie", session).
                contentType(ContentType.JSON).body(update).
                patch("/assets/{uuid}", "33a47a2a-164b-4f72-a9c4-cad267b0e56a").
                then().assertThat().
                statusCode(HttpStatus.SC_NOT_FOUND);

        assertNotFound(UUID.fromString("33a47a2a-164b-4f72-a9c4-cad267b0e56a"));
    }

}
