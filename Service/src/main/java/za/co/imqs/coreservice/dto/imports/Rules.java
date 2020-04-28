package za.co.imqs.coreservice.dto.imports;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.bean.BeanField;
import com.opencsv.bean.processor.StringProcessor;
import com.opencsv.bean.validators.StringValidator;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;


public interface Rules {

    //
    // Regexes
    //
    public static final String VALID_K = "^[\\w]*$";
    public static final String VALID_CODE = "^[\\w]*$";
    public static final String VALID_PATH = "^(\\w+([-]\\w+)*)$";

    //
    // Validators
    //
    public static class MustBeInSet implements StringValidator {
        private Set<String> validValues = new HashSet<>();

        @Override
        public boolean isValid(String s) {
            final boolean b = isNotEmpty(s) && validValues.contains(s);
            return b;
        }

        @Override
        public void validate(String s, BeanField beanField) throws CsvValidationException {
            if (!this.isValid(s)) {
                throw new CsvValidationException(
                        String.format("Value %s for column %s is not one of (%s)", s, beanField.getField().getName(), s, String.join(",", this.validValues))
                );
            }
        }

        @Override
        public void setParameterString(String s) {
            validValues.addAll(Arrays.asList(s.split(",")));
        }
    }

    @Slf4j
    public static class MustBeCoordinate implements StringValidator {
        private static final DecimalFormat FORMAT = new DecimalFormat("00.0000000000");

        @Override
        public boolean isValid(String s) {
            try {
                if (s.length() >= 1 ) {
                    synchronized (FORMAT) {
                        if (s.charAt(0) == '-' || s.charAt(0) == '+') {
                            s = s.charAt(0) + FORMAT.format(FORMAT.parse(s.substring(1)));
                        } else {
                            s = FORMAT.format(FORMAT.parse(s));
                        }
                    }
                } else {
                    s = null;
                }
                return true;

            } catch (Exception e) {
                log.debug("", e);
            }
            return false;
        }

        @Override
        public void validate(String s, BeanField beanField) throws CsvValidationException {
            if (!isValid(s)) throw new CsvValidationException(String.format("'%s' is not a valid coordinate", s));
        }

        @Override
        public void setParameterString(String s) {
            if (isNotEmpty(s)) {
                throw new IllegalArgumentException("This method does not accept parameters");
            }
        }
    }
    //
    // Processors
    //
    public static class ConvertToUppercase implements StringProcessor {
        String defaultValue;

        @Override
        public String processString(String value) {
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }
            return value.toUpperCase();
        }

        @Override
        public void setParameterString(String value) {
            defaultValue = value;
        }
    }

    //
    // Custom bindings
    //
    public static class TerminalNode<T> extends AbstractBeanField {
        @Override
        protected String convert(String s) throws CsvDataTypeMismatchException, CsvConstraintViolationException {
            final String[] x =  s.split("-");
            return x[x.length-1];
        }
    }
}

//
// Notes:
// Processors execute before validators
