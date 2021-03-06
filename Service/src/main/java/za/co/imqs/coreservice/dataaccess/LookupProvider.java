package za.co.imqs.coreservice.dataaccess;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.processor.PreAssignmentProcessor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import za.co.imqs.coreservice.dto.lookup.*;
import za.co.imqs.coreservice.imports.Rules;

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

        public static KvDef def(String code, String name, String table, String owner) {
            final LookupProvider.KvDef d = new LookupProvider.KvDef();
            d.setTable(table);
            d.setOwner(owner);
            d.setName(name);
            d.setCode(code);
            return d;
        }
    }

    @Data
    public static class Field {
        private String operator;
        private Object value;

        public static Field of(String operator, String value) {
            final Field f = new Field();
            f.setOperator(operator);
            f.setValue(value);
            return f;
        }
    }



    @Data
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.EXISTING_PROPERTY,
            property = "type",
            visible =  true
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = Kv.class, name = "KV"),
            @JsonSubTypes.Type(value = KvWard.class, name = "WARD"),
            @JsonSubTypes.Type(value = KvDistrict.class, name = "DISTRICT"),
            @JsonSubTypes.Type(value = KvSuburb.class, name = "SUBURB"),
            @JsonSubTypes.Type(value = KvTown.class, name = "TOWN"),
            @JsonSubTypes.Type(value = KvMunicipality.class, name = "MUNIC"),
            @JsonSubTypes.Type(value = KvRegion.class, name = "REGION"),

            @JsonSubTypes.Type(value = ClientDeptKv.class, name = "CLIENT_DEP"),
            @JsonSubTypes.Type(value = ChiefDirectorateKv.class, name = "CHIEF_DIR"),

            @JsonSubTypes.Type(value = KvConditionRating.class, name = "CONDITION_RATING"),
            @JsonSubTypes.Type(value = KvConfidenceRating.class, name = "CONFIDENCE_RATING"),
            @JsonSubTypes.Type(value = KvPerformanceRating.class, name = "PERFORMANCE_RATING")
    })
    public static class Kv {
        @CsvBindByName(required = true)
        @PreAssignmentProcessor(processor = Rules.Trim.class)
        private String k;

        @CsvBindByName(required = true)
        @PreAssignmentProcessor(processor = Rules.Trim.class)
        private String v;

        private String creation_date; // TODO check date format http://opencsv.sourceforge.net/#locales_dates_numbers
        private String activated_at; // TODO check date format http://opencsv.sourceforge.net/#locales_dates_numbers
        private String deactivated_at; // TODO check date format http://opencsv.sourceforge.net/#locales_dates_numbers
        private Boolean allow_delete;// TODO check date format http://opencsv.sourceforge.net/#locales_dates_numbers

        private String type = "KV";

        public static Kv pair(String k, String v) {
            final Kv kv = new Kv();
            kv.setK(k);
            kv.setV(v);

            return kv;
        }
    }


    // TODO these need to move to dynamic mapping per client schema etc. https://stackoverflow.com/questions/34079050/add-subtype-information-at-runtime-using-jackson-for-polymorphism
    @Data
    @EqualsAndHashCode(callSuper=true)
    @ToString(callSuper=true, includeFieldNames=true)
    public static class ClientDeptKv extends Kv {
        @CsvBindByName(required = false)
        @PreAssignmentProcessor(processor = Rules.Trim.class)
        private String chief_directorate_code;

        @CsvBindByName(required = false)
        @PreAssignmentProcessor(processor = Rules.Trim.class)
        private String responsible_dept_classif;
    }

    @Data
    @EqualsAndHashCode(callSuper=true)
    @ToString(callSuper=true, includeFieldNames=true)
    public static class ChiefDirectorateKv extends Kv {
        @CsvBindByName(required = false)
        @PreAssignmentProcessor(processor = Rules.Trim.class)
        private String branch_code;
    }

    public List<KvDef> getKvTypes();

    // Get lookups based on map of field=value (AND)
    public List<Map<String,Object>> get(String viewName, Map<String,String> parameters);

    // Get lookups based on map of field['<','>','=','!=']value  (AND)
    public List<Map<String,Object>> getWithOperators(String viewName, Map<String,Field> parameters);


    public String getKvValue(String target, String key);

    public <T extends Kv> T getKv(String target, String key);

    public List<Kv> getEntireKvTable(String target);

    // upsert
    public <T extends Kv> void acceptKv(String target, List<T> kvs) ;

    // upsert
    default <T extends Kv> void acceptKv(String target, T ...kv) {
        acceptKv(target, Arrays.asList(kv));
    }

    // For integration testing
    public void obliterateKv(String target);
}
