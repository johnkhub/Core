package za.co.imqs.coreservice.imports;

import com.opencsv.bean.*;
import za.co.imqs.coreservice.model.ORM;

import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
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
        private final String type;
        private final HashSet<String> typeNamesUpper = new HashSet<>();

        public Filter(String type, MappingStrategy ms) {
            this.type = type;
            ORM.SUB_CLASSES.forEach(s -> typeNamesUpper.add(s.toUpperCase()));
        }

        @Override
        public boolean allowLine(String[] strings) {
            if (!typeNamesUpper.contains(strings[1].toUpperCase())) {
                throw new IllegalArgumentException("Unknown asset type "+strings[1]);
            }
           return strings[1].equalsIgnoreCase(type);
        }
    }


    private static class NullFilter implements CsvToBeanFilter {

        @Override
        public boolean allowLine(String[] strings) {
            return true;
        }
    }

}
