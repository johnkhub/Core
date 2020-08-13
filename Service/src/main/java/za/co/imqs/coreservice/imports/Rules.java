package za.co.imqs.coreservice.imports;

import com.opencsv.bean.BeanField;
import com.opencsv.bean.processor.StringProcessor;
import com.opencsv.bean.validators.MustMatchRegexExpression;
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

    public static final String VALID_FREE_TEXT = "^[A-zÀ-ÖØ-öø-įĴ-őŔ-žǍ-ǰǴ-ǵǸ-țȞ-ȟȤ-ȳɃɆ-ɏḀ-ẞƀ-ƓƗ-ƚƝ-ơƤ-ƥƫ-ưƲ-ƶẠ-ỿ|\\p{Digit}|\\p{Punct}|\\p{Blank}|\\u00B1\\`\\:]*$";
    // Or in word:
    //  all alpha characters including diacritics
    //  digits
    //  punctuation
    //  `
    //  u00B1 = ±

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

    public static class MustNotBeNull implements StringValidator {

        @Override
        public boolean isValid(String s) {
            s = s.trim();
            return isNotEmpty(s) && !"NULL".equalsIgnoreCase(s);
        }

        @Override
        public void validate(String s, BeanField beanField) throws CsvValidationException {
            if (!this.isValid(s)) {
                throw new CsvValidationException(
                        String.format("Value %s for column %s is required", s, beanField.getField().getName())
                );
            }
        }

        @Override
        public void setParameterString(String s) {
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

    public static class OptionallyMatchRegex extends MustMatchRegexExpression implements StringValidator {
        @Override
        public boolean isValid(String value) {
            return StringUtils.isEmpty(value) || super.isValid(value);
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

    public class Replace implements StringProcessor {
        private String replace;
        private String with;

        @Override
        public String processString(String value) {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            return value.trim().replace(replace, with);
        }

        @Override
        public void setParameterString(String value) {
            final String[] s = value.split(",");
            if (s.length != 2) throw new IllegalArgumentException("Expected exactly two strings separated by a comma.");
            replace = s[0];
            with = s[1];
        }
    }
}

//
// Notes:
// Processors execute before validators
