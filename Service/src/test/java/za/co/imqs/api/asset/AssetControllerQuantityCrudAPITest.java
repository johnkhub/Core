package za.co.imqs.api.asset;

import com.jayway.restassured.http.ContentType;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import za.co.imqs.coreservice.dataaccess.exception.BusinessRuleViolationException;
import za.co.imqs.coreservice.dto.asset.QuantityDto;
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
public class AssetControllerQuantityCrudAPITest extends AbstractAssetControllerAPITest {

    @Before
    public void clearAsset() {
        prepPermissions();

        deleteAssets(THE_ASSET);
    }

    @Test
    public void assetAddQuantitySuccess() throws Exception {
        final AssetEnvelopeDto envelope = (AssetEnvelopeDto) new CoreAssetBuilder(new AssetEnvelopeDto()).
                code("e1").
                name("Envelope 1").
                type("ENVELOPE").
                funcloc("at").
                dept("WCED").
                serial("1234").
                get();

        given().
                header("Cookie", login.getSession()).
                contentType(ContentType.JSON).body(envelope).
                put("/assets/{uuid}", THE_ASSET).
                then().assertThat().
                statusCode(HttpStatus.SC_CREATED);

        assertEquals(getAsset(THE_ASSET), envelope);

        final QuantityDto quantity = new QuantityDto();
        quantity.setName("extent");
        quantity.setAsset_id(THE_ASSET);
        quantity.setUnit_code("length_m");
        quantity.setNum_units("12.0");


        given().
                header("Cookie", login.getSession()).
                contentType(ContentType.JSON).body(quantity).
                put("/assets/quantity").
                then().assertThat().
                statusCode(HttpStatus.SC_CREATED);
    }

    @Test
    public void assetUpdateSuccess() throws Exception {
        assetAddQuantitySuccess();

        final QuantityDto quantity = new QuantityDto();
        quantity.setName("extent");
        quantity.setAsset_id(THE_ASSET);
        quantity.setNum_units("13.0");
        quantity.setUnit_code("length_m");


        given().
                header("Cookie", login.getSession()).
                contentType(ContentType.JSON).body(quantity).
                patch("/assets/quantity").
                then().assertThat().
                statusCode(HttpStatus.SC_OK);

        final QuantityDto result = given().
                header("Cookie", login.getSession()).
                get("/assets/quantity/asset_id/{asset_id}/name/{name}", THE_ASSET, "extent").
                then().assertThat().
                statusCode(HttpStatus.SC_OK).extract().as(QuantityDto.class);
        quantity.setUnit_code("length_m");
        Assert.assertEquals(quantity, result);

    }

    @Test
    public void deleteQantity() throws Exception {
        assetAddQuantitySuccess();
        given().
                header("Cookie", login.getSession()).
                contentType(ContentType.JSON).
                delete("/assets/quantity/asset_id/{asset_id}/name/{name}", THE_ASSET, "extent").
                then().assertThat().
                statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void assetUpdateValidationFailure() throws Exception {
        fail("Not implemented");
    }


    @Test
    public void assetUpdateQuantityBusinesseException() throws Exception  {
        expected.expect(BusinessRuleViolationException.class);
        fail("Not implemented");
    }

    @Test
    public void assetUpdateQuantityForbidden()  throws Exception  {
        org.junit.Assume.assumeTrue(TEST_PERMISSIONS);
        fail("Not implemented");
    }

    @Test
    public void assetUpdateQuantityNotFound() throws Exception {
        fail("Not implemented");
    }
}
