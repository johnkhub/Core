package za.co.imqs.api.asset;

import org.junit.Test;
import za.co.imqs.coreservice.dataaccess.exception.BusinessRuleViolationException;

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
public class AssetControllerDeleteGroupingAPITest extends AbstractAssetControllerAPITest {

    @Test
    public void addAssetDeleteGroupingSuccess() {
        fail("Not implemented");
    }

    @Test
    public void addAssetDeleteValidationFailure() {
        fail("Not implemented");
    }

    @Test
    public void addAssetDeleteBusinesseException() {
        expected.expect(BusinessRuleViolationException.class);
        fail("Not implemented");
    }

    @Test
    public void addAssetDeleteForbidden() {
        org.junit.Assume.assumeTrue(TEST_PERMISSIONS);
        fail("Not implemented");
    }


    @Test
    public void addAssetDeleteNotFound() {
        fail("Not implemented");
    }
}
