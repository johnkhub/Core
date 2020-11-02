package za.co.imqs.coreservice;

import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import za.co.imqs.spring.service.utils.JdbcDefaultRetryable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RetryConfigFactory {
    private static final Map<Class<? extends Throwable>, Boolean> __RETRYABLE_EXCEPTIONS = new HashMap<>();

    static {
        __RETRYABLE_EXCEPTIONS.put(TransientDataAccessException.class, true);
        __RETRYABLE_EXCEPTIONS.put(RecoverableDataAccessException.class, true);
    }

    public static final Map<Class<? extends Throwable>, Boolean> RETRYABLE_EXCEPTIONS = Collections.unmodifiableMap(__RETRYABLE_EXCEPTIONS);


    public static RetryTemplate getSimpleFixedBackoffPolicy(int retries, int delay) {
        final RetryTemplate retry = new RetryTemplate();
        retry.setRetryPolicy(new SimpleRetryPolicy(retries, JdbcDefaultRetryable.RETRYABLE_EXCEPTIONS));
        final FixedBackOffPolicy backOff = new FixedBackOffPolicy();
        backOff.setBackOffPeriod(delay);
        retry.setBackOffPolicy(backOff);
        return retry;
    }
}
