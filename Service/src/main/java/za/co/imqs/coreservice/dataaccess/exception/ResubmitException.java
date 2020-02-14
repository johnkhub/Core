package za.co.imqs.coreservice.dataaccess.exception;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/05
 */
public class ResubmitException extends RuntimeException {
    public ResubmitException(String message) {
        super(message);
    }

    public ResubmitException(String message, Throwable cause) {
        super(message, cause);
    }
}
