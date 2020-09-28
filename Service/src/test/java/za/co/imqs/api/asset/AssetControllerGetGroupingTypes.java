package za.co.imqs.api.asset;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.Test;
import za.co.imqs.coreservice.dto.AssetExternalLinkTypeDto;

import java.util.UUID;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static za.co.imqs.coreservice.model.DTPW.GROUPING_TYPE_EMIS;

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
                        AssetExternalLinkTypeDto.of(UUID.fromString(GROUPING_TYPE_EMIS), "EMIS", "DTPW EMIS")
                )
        );
    }
}
