package za.co.imqs.api.asset;

import com.jayway.restassured.http.ContentType;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.DockerComposeContainer;
import za.co.imqs.coreservice.dataaccess.exception.BusinessRuleViolationException;
import za.co.imqs.coreservice.dto.asset.AssetEnvelopeDto;

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
public class AssetControllerDeleteAPITest extends AbstractAssetControllerAPITest{

    @Before
    public void clearAsset() {
        deleteAssets(THE_ASSET);
        prepPermissions();
    }

    @Test
    public void assetDeleteSuccessSetInactiveSuccess() {
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
                delete("/assets/testing/{uuid}", THE_ASSET).
                then().assertThat().statusCode(HttpStatus.SC_OK);

        // CHECK THAT IT IS DISABLED!
    }

    @Test
    public void assetDeleteHardNotAdmin() {
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
                delete("/assets/{uuid}", THE_ASSET).
                then().assertThat().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void assetDeleteValidationFailure() {
        fail("Not implemented");
    }

    @Test
    public void assetDeleteBusinesseException() {
        expected.expect(BusinessRuleViolationException.class);
        fail("Not implemented");
    }

    @Test
    public void assetDeleteForbidden() {
        org.junit.Assume.assumeTrue(TEST_PERMISSIONS);
        fail("Not implemented");
    }

}
