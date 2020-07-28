package za.co.imqs.api.asset;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.Test;
import za.co.imqs.coreservice.dto.AssetExternalLinkTypeDto;

import java.util.UUID;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;

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
public class AssetControllerGetGroupingTypes extends AbstractAssetControllerAPITest {

    @Test
    public void getTypes() {
        AssetExternalLinkTypeDto[] result = given().
                header("Cookie", session).
                get("/assets/group/types").
                then().
                assertThat().statusCode(HttpStatus.SC_OK).extract().as(AssetExternalLinkTypeDto[].class);

        assertThat(result,
                arrayContainingInAnyOrder(
                        AssetExternalLinkTypeDto.of(UUID.fromString("4a6a4f78-2dc4-4b29-aa9e-5033b834a564"), "EMIS", "DTPW EMIS")
                )
        );
    }
}
