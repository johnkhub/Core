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
public class KvSuburb extends LookupProvider.Kv  implements Geometry  {
    @CsvBindByName(required = false)
    @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
    private String geom;

    @CsvBindByName(required = true)
    @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
    private String town_k;

    @CsvBindByName(required = true)
    @PreAssignmentProcessor(processor = Rules.ConvertEmptyOrBlankStringsToNull.class)
    private String ward_k;

    public KvSuburb() {
        setType("SUBURB");
    }

    public static KvSuburb tripple(String k, String v, String geom) {
        final KvSuburb kv = new KvSuburb();
        kv.setK(k);
        kv.setV(v);
        kv.setGeom(geom);

        return kv;
    }
}
