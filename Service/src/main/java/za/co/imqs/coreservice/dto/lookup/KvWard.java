package za.co.imqs.coreservice.dto.lookup;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.processor.PreAssignmentProcessor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import za.co.imqs.coreservice.dataaccess.LookupProvider;
import za.co.imqs.coreservice.imports.Rules;

@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true, includeFieldNames=true)
public class KvWard extends LookupProvider.Kv implements Geometry {
    @CsvBindByName(required = false)
    @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
    private String geom;

    @CsvBindByName(required = true)
    @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
    private String local_municipality_k;

    public KvWard() {
        setType("WARD");
    }

    public static KvWard tripple(String k, String v, String geom) {
        final KvWard kv = new KvWard();
        kv.setK(k);
        kv.setV(v);
        kv.setGeom(geom);

        return kv;
    }
}
