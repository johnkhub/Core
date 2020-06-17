package za.co.imqs.coreservice.dataaccess;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.processor.PreAssignmentProcessor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
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

            @JsonSubTypes.Type(value = ClientDeptKv.class, name = "CLIENT_DEP"),
            @JsonSubTypes.Type(value = ChiefDirectorateKv.class, name = "CHIEF_DIR")
    })
    public static class Kv {
        @CsvBindByName(required = true) private String k;
        @CsvBindByName(required = true) private String v;
        private String creation_date; // TODO check date format http://opencsv.sourceforge.net/#locales_dates_numbers
        private String activated_at; // TODO check date format http://opencsv.sourceforge.net/#locales_dates_numbers
        private String deactivated_at; // TODO check date format http://opencsv.sourceforge.net/#locales_dates_numbers
        private Boolean allow_delete;// TODO check date format http://opencsv.sourceforge.net/#locales_dates_numbers


        @CsvBindByName(required = false)
        @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
        private String geom;

        private String type = "KV";

        public static Kv pair(String k, String v) {
            final Kv kv = new Kv();
            kv.setK(k);
            kv.setV(v);

            return kv;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper=true)
    @ToString(callSuper=true, includeFieldNames=true)
    public static class KvSuburb extends Kv {
        @CsvBindByName(required = false)
        @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
        private String geom;

        @CsvBindByName(required = true)
        @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
        private String town_k;

        @CsvBindByName(required = true)
        @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
        private String ward_k;
    }

    @Data
    @EqualsAndHashCode(callSuper=true)
    @ToString(callSuper=true, includeFieldNames=true)
    public static class KvWard extends Kv {
        @CsvBindByName(required = false)
        @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
        private String geom;

        @CsvBindByName(required = true)
        @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
        private String local_municipality_k;
    }

    @Data
    @EqualsAndHashCode(callSuper=true)
    @ToString(callSuper=true, includeFieldNames=true)
    public static class KvTown extends Kv {
        @CsvBindByName(required = false)
        @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
        private String geom;

        @CsvBindByName(required = true)
        @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
        private String local_municipality_k;
    }

    @Data
    @EqualsAndHashCode(callSuper=true)
    @ToString(callSuper=true, includeFieldNames=true)
    public static class KvMunicipality extends Kv {
        @CsvBindByName(required = false)
        @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
        private String geom;

        @CsvBindByName(required = true)
        @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
        private String district_k;
    }

    @Data
    @EqualsAndHashCode(callSuper=true)
    @ToString(callSuper=true, includeFieldNames=true)
    public static class KvDistrict extends Kv {
        @CsvBindByName(required = false)
        @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
        private String geom;
    }


    // TODO these need to move to dynamic mapping per client schema etc. https://stackoverflow.com/questions/34079050/add-subtype-information-at-runtime-using-jackson-for-polymorphism
    @Data
    @EqualsAndHashCode(callSuper=true)
    @ToString(callSuper=true, includeFieldNames=true)
    public static class ClientDeptKv extends Kv {
        @CsvBindByName(required = false) private String chief_directorate_code;
        @CsvBindByName(required = false) private String responsible_dept_classif;
    }

    @Data
    @EqualsAndHashCode(callSuper=true)
    @ToString(callSuper=true, includeFieldNames=true)
    public static class ChiefDirectorateKv extends Kv {
        @CsvBindByName(required = false) private String branch_code;
    }

    public List<KvDef> getKvTypes();

    // Get lookups based on map of field=value (AND)
    public List<Map<String,Object>> get(String viewName, Map<String,String> parameters);

    // Get lookups based on map of field['<','>','=','!=']value  (AND)
    public List<Map<String,Object>> getWithOperators(String viewName, Map<String,Field> parameters);


    public String getKv(String target, String key);

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
