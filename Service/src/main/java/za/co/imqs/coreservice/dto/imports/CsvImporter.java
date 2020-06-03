package za.co.imqs.coreservice.dto.imports;

import com.opencsv.bean.*;

import java.io.Reader;
import java.util.Collections;
import java.util.stream.Stream;

public class CsvImporter<T> {

    public Stream<T> stream(Reader reader, T object) throws Exception {
        return stream(reader, object, null, null);
    }

    public Stream<T> stream(Reader reader, T object, BeanVerifier skipper, String type) throws Exception {
        final HeaderColumnNameMappingStrategy ms = new HeaderColumnNameMappingStrategy();
        ms.setType(object.getClass());

        final CsvToBean cb = new CsvToBeanBuilder(reader)
                .withType(object.getClass())
                .withMappingStrategy(ms)
                .withFilter( type == null ? new NullFilter() : new Filter(type, ms)) // TODO This is less than ideal see comment in the filter itself (below)
                .build();

        if (skipper != null) cb.setVerifiers(Collections.singletonList(skipper));
        return cb.stream();
    }

    // TODO opencsv leaves us in a pickle here. We process the different asset types individually with multiple passes through the file
    // The BeanFilter stuff unfortunately only executes after the bean has been created and the constraints defined by the annotations
    // have been applied, so we need a way to filter out what we don't want before we start creating beans
    // There appears to be no direct way of getting the column names here so we fudge it and hope that one of the other fields do not contain values
    // that match the asset_type_codes
    //
    // MAYBE INSIST THAT THE TYPE BE THE FIRST COLUMN>???
    //
    private static class Filter implements CsvToBeanFilter {
        private String type;
        private MappingStrategy ms;

        public Filter(String type,MappingStrategy ms) {
            this.type = type;
            this.ms = ms;
        }

        @Override
        public boolean allowLine(String[] strings) {
            for (String s : strings) {
                if (s.equals(type)) return true;
            }
            return false;
        }
    }

    private static class NullFilter implements CsvToBeanFilter {


        @Override
        public boolean allowLine(String[] strings) {
            return true;
        }
    }

}
