package za.co.imqs.api.asset;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.Test;
import za.co.imqs.coreservice.dto.asset.AssetExternalLinkTypeDto;

import java.util.UUID;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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
public class AssetControllerGetLinkTypes extends AbstractAssetControllerAPITest {

    @Test
    public void getTypes() throws Exception  {
        AssetExternalLinkTypeDto[] result = given().
                header("Cookie", login.getSession()).
                get("/assets/link/types").
                then().
                assertThat().statusCode(HttpStatus.SC_OK).extract().as(AssetExternalLinkTypeDto[].class);

        assertThat(result,
                arrayContainingInAnyOrder(
                        AssetExternalLinkTypeDto.of(UUID.fromString("c6a74a62-54f5-4f93-adf3-abebab3d3467"), "V6", "Version 6 component identifier")
                )
        );
    }
}
