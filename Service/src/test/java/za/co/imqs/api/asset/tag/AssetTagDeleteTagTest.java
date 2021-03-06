package za.co.imqs.api.asset.tag;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import za.co.imqs.api.asset.AbstractAssetControllerAPITest;

import java.util.UUID;

import static com.jayway.restassured.RestAssured.given;
import static junit.framework.TestCase.fail;

public class AssetTagDeleteTagTest extends AbstractAssetControllerAPITest {

    @Before
    public void before() throws Exception {
        prepPermissions();
        Populate.populate(login.getSession(), THE_ASSET);
        given().header("Cookie", login.getSession()).put("/assets/{uuid}/tag/{tag1}?{tag2}&{tag3}", THE_ASSET, "TAG1", "TAG2", "TAG3");
    }

    @Test
    public void delete() {
        given().
                header("Cookie", login.getSession()).
                delete("/assets/{uuid}/tag/{tag}", THE_ASSET, "TAG3").
                then().assertThat().
                statusCode(HttpStatus.SC_OK);

        // TODO check that others remain
    }

    @Test
    public void deleteThree() {
        given().
                header("Cookie", login.getSession()).
                delete("/assets/{uuid}/tag/{tag1}?{tag2}&{tag3}", THE_ASSET, "TAG1", "TAG2", "TAG3").
                then().assertThat().
                statusCode(HttpStatus.SC_OK);

        // TODO check that none remain
    }

    @Test
    public void deleteNonExistent() {
        given().
                header("Cookie", login.getSession()).
                delete("/assets/{uuid}/tag/{tag}", THE_ASSET, "TAGX").
                then().assertThat().
                statusCode(HttpStatus.SC_NOT_FOUND);
        // CHECK MESSAGE
        // TODO check that all remain
    }

    @Test
    public void deleteFromNonExistentAsset() {
        given().
                header("Cookie", login.getSession()).
                delete("/assets/{uuid}/tag/{tag}", UUID.randomUUID(), "TAG3").
                then().assertThat().
                statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void deleteHavingSpecialCharacters() {
        fail("Not implemented");
    }
}
