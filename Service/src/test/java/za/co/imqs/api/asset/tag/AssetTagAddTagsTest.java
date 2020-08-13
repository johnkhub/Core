package za.co.imqs.api.asset.tag;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import za.co.imqs.api.asset.AbstractAssetControllerAPITest;

import static com.jayway.restassured.RestAssured.given;
import static junit.framework.TestCase.fail;

public class AssetTagAddTagsTest extends AbstractAssetControllerAPITest {

    @Before
    public void clear() throws Exception {
        prepPermissions();
        deleteAssets(THE_ASSET);
        Populate.populate(session, THE_ASSET);
    }

    @After
    public void after() {
        deleteAssets(THE_ASSET);
    }

    @Test
    public void addOne() {
        given().
                header("Cookie", session).
                put("/assets/{uuid}/tag/{tag}", THE_ASSET, "TAG1").
                then().assertThat().
                statusCode(HttpStatus.SC_OK);
        // TODO check that it exists
    }

    @Test
    public void addThree() {
        given().
                header("Cookie", session).
                put("/assets/{uuid}/tag/{tag1}?{tag2}&{tag3}", THE_ASSET, "TAG1", "TAG2", "TAG3").
                then().assertThat().
                statusCode(HttpStatus.SC_OK);
        // TODO check that they all exists
    }


    @Test
    public void addDuplicate() {
        addOne();
        given().
                header("Cookie", session).
                put("/assets/{uuid}/tag/{tag}", THE_ASSET, "TAG1").
                then().assertThat().
                statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void addBusinessRuleViolation() {
        fail("Not implemented");
    }

    @Test
    public void addForbidden() {
        org.junit.Assume.assumeTrue(TEST_PERMISSIONS);
        Assert.fail("Not implemented");
        fail("Not implemented");
    }

    @Test
    public void addHavingValidSpecialCharacters() {
        fail("Not implemented");
    }

    @Test
    public void addHavingInvalidSpecialCharacters() {
        fail("Not implemented");
    }
}
