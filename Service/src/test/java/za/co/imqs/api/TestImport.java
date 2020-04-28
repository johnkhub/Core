package za.co.imqs.api;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import za.co.imqs.coreservice.dto.CoreAssetDto;
import za.co.imqs.coreservice.dto.imports.CsvImporter;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class TestImport {
    @Test
    public void testIt() throws Exception  {
        final CsvImporter<CoreAssetDto> importer = new CsvImporter<>();
        try (Reader reader = Files.newBufferedReader(Paths.get("/home/frank/Development/Core/DTPW Data/DTPW_Location Breakdown_V13B_20200212 (copy).csv"))) {
            importer.stream(reader, new CoreAssetDto()).forEach(
                    (dto) -> {
                        log.info(dto.toString());
                    }
            );
        }
    }
}
