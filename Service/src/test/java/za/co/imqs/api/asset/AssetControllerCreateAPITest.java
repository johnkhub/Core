package za.co.imqs.api.asset;

import com.jayway.restassured.http.ContentType;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import za.co.imqs.coreservice.dataaccess.LookupProvider;
import za.co.imqs.coreservice.dataaccess.exception.BusinessRuleViolationException;
import za.co.imqs.coreservice.dataaccess.exception.ValidationFailureException;
import za.co.imqs.coreservice.dto.asset.AssetEnvelopeDto;
import za.co.imqs.coreservice.dto.lookup.KvRegion;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.fail;
import static za.co.imqs.coreservice.dataaccess.LookupProvider.Kv.pair;
import static za.co.imqs.coreservice.dto.lookup.KvRegion.tripple;

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
       deleteAssets(THE_ASSET);
       prepPermissions();
    }

    @Test
    public void assetCreateSuccess() throws Exception {
        final AssetEnvelopeDto envelope = (AssetEnvelopeDto) new CoreAssetBuilder(new AssetEnvelopeDto()).
                code("e1").
                name("Envelope 1").
                type("ENVELOPE").
                funcloc("at").
                dept("WCED")
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
                type("ENVELOPE").
                dept("WCED")
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
                funcloc("at").
                dept("WCED")
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
       expected.expect(BusinessRuleViolationException.class);
       fail("Not implemented");
    }

    @Test
    public void assetCreateForbidden() {
        org.junit.Assume.assumeTrue(TEST_PERMISSIONS);
        fail("Not implemented");
    }
}