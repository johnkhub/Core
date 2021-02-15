package za.co.imqs.api.asset;

import com.jayway.restassured.http.ContentType;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import za.co.imqs.coreservice.dto.asset.AssetEnvelopeDto;
import java.util.UUID;
import static com.jayway.restassured.RestAssured.given;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/07/27
 *
 * We have a class per API method.
 * We have a method for every possible return code.
 * Consider a test case for each field as well to test validation.
 */

public class AssetControllerGetAPITest extends AbstractAssetControllerAPITest {

    @Before
    public void clearAsset() {
        deleteAssets(THE_ASSET);
        prepPermissions();
    }

    @Test
    public void assetGetSuccess()  throws Exception {
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

        given().
                header("Cookie", login.getSession()).
                get("/assets/{uuid}",THE_ASSET).
                then().assertThat().statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void assetGetNotFound()  throws Exception {
        final UUID NONEXISTENT_ASSET = UUID.fromString("455ac960-8fc6-409f-b2ef-cd5be4ebe684");

        given().
                header("Cookie", login.getSession()).
                get("/assets/{uuid}",NONEXISTENT_ASSET).
                then().assertThat().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void assetGetBadRequest()  throws Exception {
        final UUID wrong_uuid = null;

        given().
                header("Cookie", login.getSession()).
                get("/assets/{uuid}",wrong_uuid).
                then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST);
    }
}
