package za.co.imqs.coreservice.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import za.co.imqs.coreservice.dataaccess.LookupProvider;
import za.co.imqs.coreservice.dto.CoreAssetDto;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.Types;
import java.util.*;

/**
 * The whole spiel is based on naming conventions
 *
 */
@Slf4j
public class ORM {
    public static final Set<String> SUB_CLASSES = getAssetSubclasses();

    public static String getTableName(CoreAsset asset) {
        return getTableName(asset.getClass().getSimpleName());
    }

    public static String getTableName(String subClassName) {
        final String name = subClassName.
                // remove asset e.g. AssetBuilding -> Building
                replace("Asset", "").
                // camel to snake case
                replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2").
                replaceAll("([a-z])([A-Z])", "$1_$2");
        return "asset.a_tp_"+name.toLowerCase();
    }

    private static Set<String> getAssetSubclasses() {
        //
        // Make use of the JsonSubTypes annotation on the CoreAssetDto class to determine the names of the
        // subclasses. Note that this depends on a strict naming convention
        //
        final Set<String> names = new HashSet<>();
        final JsonSubTypes subTypes = CoreAssetDto.class.getAnnotation(JsonSubTypes.class);
        for (JsonSubTypes.Type t : subTypes.value()) {
            String name = t.value().getSimpleName().substring(5);
            names.add(name.substring(0, name.length()-3));
        }
        return names;
    }

    public static <T> MapSqlParameterSource mapToSql(T model, Set<String> exclude) throws Exception {
        final MapSqlParameterSource parameters = new MapSqlParameterSource();

        for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(model.getClass()).getPropertyDescriptors()) {
            final Method getter = propertyDescriptor.getReadMethod();

            if (getter != null && !exclude.contains(getter.getName())) {
                final StringBuilder msg = new StringBuilder("ORM Mapping ").append(getter.getName()).append(":").append(getter.getReturnType()).append(" ");
                final Object result = getter.invoke(model);
                if (result != null) {
                    final String field = getter.getName().substring(3).toLowerCase();
                    parameters.addValue(field, result, mapType(getter.getReturnType()));
                    msg.append(" -> ").append(field);
                } else {
                    msg.append("SKIPPING null value");
                }

                log.debug(msg.toString());
            }
        }


        return parameters;
    }

    public static HashSet<String> getReadMethods(Class cls) {
        final HashSet<String> names = new HashSet<>();

        try {
            for (PropertyDescriptor p : Introspector.getBeanInfo(cls).getPropertyDescriptors()) {
                if (p.getReadMethod() != null) {
                    names.add(p.getReadMethod().getName());
                }
            }
            return names;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static int mapType(Class cls) {
        switch (cls.getName()) {
            case "String" : return Types.VARCHAR;
            case "Timestamp" : return Types.TIMESTAMP;
            case "BigDecimal" : return Types.DECIMAL;
        }
        return Types.OTHER;
    }
}


