package za.co.imqs.coreservice.dto.imports;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;

import java.io.Reader;
import java.util.stream.Stream;

public class CsvImporter<T> {

    public Stream<T> stream(Reader reader, T object) throws Exception {
        final HeaderColumnNameMappingStrategy ms = new HeaderColumnNameMappingStrategy();
        ms.setType(object.getClass());

        final CsvToBean cb = new CsvToBeanBuilder(reader)
                .withType(object.getClass())
                .withMappingStrategy(ms)
                .build();

        return cb.stream();
    }

}
