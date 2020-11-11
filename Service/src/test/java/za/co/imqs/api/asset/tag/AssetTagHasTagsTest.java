package za.co.imqs.api.asset.tag;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import za.co.imqs.api.asset.AbstractAssetControllerAPITest;

import java.util.LinkedList;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class AssetTagHasTagsTest extends AbstractAssetControllerAPITest  {

    @Before
    public void before() {
        prepPermissions();
    }

    @Test
    public void has() {
        List<Boolean> results = new LinkedList<>();
        results = given().
                header("Cookie", login.getSession()).
                get("/assets/{uuid}/tag/{tag}", THE_ASSET, "TAG2").
                then().assertThat().
                statusCode(HttpStatus.SC_OK).
                extract().as(results.getClass());

        assertThat(results, hasItem(Boolean.TRUE));
    }

    @Test
    public void hasMultiple() {
        List<Boolean> results = new LinkedList<>();
        results = given().
                header("Cookie", login.getSession()).
                get("/assets/{uuid}/tag/{tag1}?{tag2}", THE_ASSET, "TAG1","TAG2").
                then().assertThat().
                statusCode(HttpStatus.SC_OK).
                extract().as(results.getClass());

        assertThat(results, hasItem(Boolean.TRUE));
    }


    @Test
    public void hasNot() {
        List<Boolean> results = new LinkedList<>();
        results = given().
                header("Cookie", login.getSession()).
                get("/assets/{uuid}/tag/{tag}", THE_ASSET, "TAGX").
                then().assertThat().
                statusCode(HttpStatus.SC_OK).
                extract().as(results.getClass());
        assertThat(results, hasItem(Boolean.FALSE));
    }

    @Test
    public void hasHavingSpecialCharacters() {
        fail("Not implemented");
    }

    @Before
    public void preconditions() throws Exception {
        prepPermissions();
        Populate.populate(login.getSession(), THE_ASSET);
        given().header("Cookie", login.getSession()).put("/assets/{uuid}/tag/{tag1}?{tag2}&{tag3}", THE_ASSET, "TAG1", "TAG2", "TAG3");

    }
}
