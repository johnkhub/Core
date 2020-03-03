package za.co.imqs.coreservice.dataaccess;

import lombok.Data;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/28
 */
public interface LookupProvider {
    @Data
    public static class KvDef {
        private String code;
        private String name;
        private String description;
        private String owner;
        private String table;
    }

    @Data
    public static class Field {
        private String operator;
        private Object value;
    }

    @Data
    public static class Kv {
        private String k;
        private String v;
        private String creation_date;
        private String activated_at;
        private String deactivated_at;
        private Boolean allow_delete;
    }

    public List<KvDef> getKvTypes();

    // Get lookups based on map of field=value (AND)
    public List<Map<String,Object>> get(String viewName, Map<String,String> parameters);

    // Get lookups based on map of field['<','>','=','!=']value  (AND)
    public List<Map<String,Object>> getWithOperators(String viewName, Map<String,Field> parameters);

    public void acceptKv(String target, List<Kv> kv);

    default void acceptKv(String target, Kv ...kv) {
        acceptKv(target, Arrays.asList(kv));
    }

    // For integration testing
    public void obliterateKv(String target);
}
