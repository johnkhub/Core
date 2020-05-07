package za.co.imqs.coreservice.dto.imports;

import com.opencsv.bean.*;

import java.io.Reader;
import java.util.Collections;
import java.util.stream.Stream;

public class CsvImporter<T> {

    public Stream<T> stream(Reader reader, T object) throws Exception {
        return stream(reader, object, null);
    }

    public Stream<T> stream(Reader reader, T object, BeanVerifier skipper) throws Exception {
        final HeaderColumnNameMappingStrategy ms = new HeaderColumnNameMappingStrategy();
        ms.setType(object.getClass());

        final CsvToBean cb = new CsvToBeanBuilder(reader)
                .withType(object.getClass())
                .withMappingStrategy(ms)
                .build();

        if (skipper != null) cb.setVerifiers(Collections.singletonList(skipper));
        return cb.stream();
    }
}
