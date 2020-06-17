package za.co.imqs.coreservice.imports;

import com.opencsv.bean.BeanField;
import com.opencsv.bean.processor.StringProcessor;
import com.opencsv.bean.validators.StringValidator;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

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
    public static final String VALID_PATH = "^(\\w+([.]\\w+)*)$";

    //
    // Validators
    //
    public static class MustBeInSet implements StringValidator {
        private final Set<String> validValues = new HashSet<>();

        @Override
        public boolean isValid(String s) {
            return isNotEmpty(s) && validValues.contains(s);
        }

        @Override
        public void validate(String s, BeanField beanField) throws CsvValidationException {
            if (!this.isValid(s)) {
                throw new CsvValidationException(
                        String.format("Value %s for column %s is not one of (%s)", s, beanField.getField().getName(), String.join(",", this.validValues))
                );
            }
        }

        @Override
        public void setParameterString(String s) {
            validValues.addAll(Arrays.asList(s.split(",")));
        }
    }

    public static class MustHaveExactLength implements StringValidator {
        private int desiredLength = -1;

        @Override
        public boolean isValid(String s) {
            return isNotEmpty(s) && s.length() == desiredLength;
        }

        @Override
        public void validate(String s, BeanField beanField) throws CsvValidationException {
            if (!this.isValid(s)) {
                throw new CsvValidationException(
                        String.format("Length of value %s for column %s does not have an exact length of %s", s, beanField.getField().getName(), desiredLength)
                );
            }
        }

        @Override
        public void setParameterString(String s) {
            desiredLength = Integer.parseInt(s);
        }
    }


    @Slf4j
    public static class MustBeCoordinate implements StringValidator {
        private static final DecimalFormat FORMAT = new DecimalFormat("00.0000000000");

        @SuppressWarnings("UnusedAssignment")
        @Override
        public boolean isValid(String s) {
            try {
                if (StringUtils.isNotEmpty(s)) {
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

            } catch (Exception ignore) {
                log.debug("", ignore);
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
            return value.toUpperCase().trim();
        }

        @Override
        public void setParameterString(String value) {
            defaultValue = value;
        }
    }

    public class ConvertEmptyOrBlankStringsToNull implements StringProcessor {
        @Override
        public String processString(String value) {
            if (value == null || value.trim().isEmpty() || value.trim().equalsIgnoreCase("NULL")) {
                return null;
            }
            return value.trim();
        }

        @Override
        public void setParameterString(String value) {
        }
    }

    public class Trim implements StringProcessor {
        @Override
        public String processString(String value) {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            return value.trim();
        }

        @Override
        public void setParameterString(String value) {
        }
    }
}

//
// Notes:
// Processors execute before validators
