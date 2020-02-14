package za.co.imqs.coreservice.dataaccess.exception;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/05
 */
public class ValidationFailureException extends RuntimeException {
    public ValidationFailureException(String message) {
        super(message);
    }

    public ValidationFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
