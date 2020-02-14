package za.co.imqs.coreservice.dataaccess.exception;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/05
 */
public class NotPermittedException extends RuntimeException {
    public NotPermittedException(String message) {
        super(message);
    }

    public NotPermittedException(String message, Throwable cause) {
        super(message, cause);
    }
}
