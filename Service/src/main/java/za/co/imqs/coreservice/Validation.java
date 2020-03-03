package za.co.imqs.coreservice;

import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import za.co.imqs.coreservice.dataaccess.exception.ValidationFailureException;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/26
 */
public class Validation {
    // alphanum and '_' joined by '.'
    private static final Pattern VALID_PATH = Pattern.compile("^([a-zA-Z0-9_]+([.][a-zA-Z0-9_]+)*)$");

    public static UUID asUUID(String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            throw new ValidationFailureException(uuid + " is not a valid UUID");
        }
    }

    public static String asGeom(String wkt) {
        try {
            WKTReader wktr = new WKTReader();
            wktr.read(wkt);
            return wkt;
        } catch (ParseException e) {
            throw new ValidationFailureException(String.format("Invalid geometry %s. %s", wkt, e.getMessage()));
        }
    }

    public static BigDecimal asBigDecimal(String num) {
        try {
            return new BigDecimal(num);
        } catch (NumberFormatException e) {
            throw new ValidationFailureException(e.getMessage());
        }
    }

    public static Timestamp asTimestamp(String t) {
        try {
            return Timestamp.valueOf(t);
        } catch (IllegalArgumentException e) {
            throw new ValidationFailureException(e.getMessage());
        }
    }

    public static String asPath(String path) {
        if (!VALID_PATH.matcher(path).matches()) {
            throw new ValidationFailureException("Invalid path format " + path);
        }
        return path;
    }
}
