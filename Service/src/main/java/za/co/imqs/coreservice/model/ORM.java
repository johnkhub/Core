package za.co.imqs.coreservice.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import za.co.imqs.coreservice.dto.CoreAssetDto;

import java.util.*;

/**
 * The whole spiel is based on naming conventions
 *
 */
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
}


