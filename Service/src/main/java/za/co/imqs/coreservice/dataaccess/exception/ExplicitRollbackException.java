package za.co.imqs.coreservice.dataaccess.exception;

public class ExplicitRollbackException extends RuntimeException {
    public ExplicitRollbackException(String s) {
        super(s);
    }
}
