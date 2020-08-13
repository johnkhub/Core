package za.co.imqs.api.asset.tag;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import za.co.imqs.api.asset.AbstractAssetControllerAPITest;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.jayway.restassured.RestAssured.given;
import static junit.framework.TestCase.fail;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class AssetTagGetTest extends AbstractAssetControllerAPITest {

    // TODO add the asset here, add the tags here

    @Before
    public void before() {
        prepPermissions();
    }

    @Test
    public void getAll() {
        final List<String> results =  Arrays.asList(given().
                header("Cookie", session).
                get("/assets/{uuid}/tag", THE_ASSET).
                then().assertThat().
                statusCode(HttpStatus.SC_OK).
                extract().as(String[].class));

        org.hamcrest.MatcherAssert.assertThat(
                results,
                containsInAnyOrder(
                         "TAG1", "TAG2", "TAG3"
                 )
         );
    }

    @Test
    public void getNonExistent() {
        given().
                header("Cookie", session).
                get("/assets/{uuid}/tag", THE_ASSET).
                then().assertThat().
                statusCode(HttpStatus.SC_OK);
        // TODO CHECK emty array result !!!!!!!!!!!!!!!!!!!
    }

    @Test
    public void getFromNonExistentAsset() {
        given().
                header("Cookie", session).
                get("/assets/{uuid}/tag", UUID.randomUUID()).
                then().assertThat().
                statusCode(HttpStatus.SC_OK);
        // TODO CHECK emty array result !!!!!!!!!!!!!!!!!!!
    }

    @Test
    public void getHavingSpecialCharacters() {
        fail("Not implemented");
    }

    @Before
    public void preconditions() throws Exception {
        Populate.populate(session, THE_ASSET);
        given().header("Cookie", session).put("/assets/{uuid}/tag/{tag1}?{tag2}&{tag3}", THE_ASSET, "TAG1", "TAG2", "TAG3");
    }
}
