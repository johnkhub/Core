package za.co.imqs.api.asset;

import com.jayway.restassured.http.ContentType;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import za.co.imqs.coreservice.dto.AssetEnvelopeDto;

import java.util.UUID;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.fail;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/27
 *
 */
public class AssetControllerCreateAPITest extends AbstractAssetControllerAPITest {
    @Before
    public void clearAsset() {
        given().
                header("Cookie", session).
                delete("/assets/testing/{uuid}", THE_ASSET).
                then().assertThat().
                statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void assetCreateSuccess() throws Exception {
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
    }

    @Test
    public void assetCreateValidationFailure() throws Exception {
        final UUID uuid = UUID.randomUUID();
        // Missing location path
        final AssetEnvelopeDto envelope = (AssetEnvelopeDto) new CoreAssetBuilder(new AssetEnvelopeDto()).
                code("e1").
                name("Envelope 1").
                type("ENVELOPE")
                .get();

        given().
                header("Cookie", session).
                contentType(ContentType.JSON).body(envelope).
        put("/assets/{uuid}", uuid).
                then().assertThat().
                statusCode(HttpStatus.SC_BAD_REQUEST).
                body(containsString("func_loc_path"));

        assertNotFound(THE_ASSET);
    }

    @Test
    public void assetCreateDuplicate() throws Exception {
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

        assertEquals(getAsset(THE_ASSET), envelope);

        given().
                header("Cookie", session).
                contentType(ContentType.JSON).body(envelope).
        put("/assets/{uuid}", THE_ASSET).
                then().assertThat().
                statusCode(HttpStatus.SC_CONFLICT).
                body(containsString("already exists"));

        assertEquals(getAsset(THE_ASSET), envelope); // should still be there!
    }

    @Test
    public void assetCreateBusinesseException() throws Exception {
       fail("Not implemented");
    }

    @Test
    public void assetCreateForbidden() {
        /*
        final AssetEnvelopeDto envelope = new AssetEnvelopeDto();

        given().
                header("Cookie", getSession()).
                contentType(ContentType.JSON).body(envelope).
        put("/assets/{uuid}", THE_ASSET).
                then().assertThat().
                statusCode(HttpStatus.SC_FORBIDDEN);

         */
        //assertNotFound(THE_ASSET);
        fail("Not implemented");
    }
}
