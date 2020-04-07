package za.co.imqs.api.asset.tag;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.BeforeClass;
import org.junit.Test;
import za.co.imqs.api.asset.AbstractAssetControllerAPITest;

import java.util.UUID;

import static com.jayway.restassured.RestAssured.given;
import static junit.framework.TestCase.fail;

public class AssetTagDeleteTagTest extends AbstractAssetControllerAPITest {

    @Test
    public void delete() {
        given().
                header("Cookie", session).
                delete("/assets/{uuid}/tag/{tag}", THE_ASSET, "TAG3").
                then().assertThat().
                statusCode(HttpStatus.SC_OK);

        // TODO check that others remain
    }

    @Test
    public void deleteThree() {
        given().
                header("Cookie", session).
                delete("/assets/{uuid}/tag/{tag1}?{tag2}&{tag3}", THE_ASSET, "TAG1", "TAG2", "TAG3").
                then().assertThat().
                statusCode(HttpStatus.SC_OK);

        // TODO check that none remain
    }

    @Test
    public void deleteNonExistent() {
        given().
                header("Cookie", session).
                delete("/assets/{uuid}/tag/{tag}", THE_ASSET, "TAGX").
                then().assertThat().
                statusCode(HttpStatus.SC_NOT_FOUND);
        // CHECK MESSAGE
        // TODO check that all remain
    }

    @Test
    public void deleteFromNonExistentAsset() {
        given().
                header("Cookie", session).
                delete("/assets/{uuid}/tag/{tag}", UUID.randomUUID(), "TAG3").
                then().assertThat().
                statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void deleteHavingSpecialCharacters() {
        fail("Not implemented");
    }

    @BeforeClass
    public static void preconditions() throws Exception {
        Populate.populate(session, THE_ASSET);
        given().header("Cookie", session).put("/assets/{uuid}/tag/{tag1}?{tag2}&{tag3}", THE_ASSET, "TAG1", "TAG2", "TAG3");
    }
}
