package za.co.imqs.coreservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import za.co.imqs.coreservice.dataaccess.CsvDownload;
import za.co.imqs.coreservice.reports.DtpwReport;

import javax.sql.DataSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static za.co.imqs.coreservice.WebMvcConfiguration.*;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_PRODUCTION;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_TEST;

@SuppressWarnings("rawtypes")
@Profile({PROFILE_PRODUCTION, PROFILE_TEST, PROFILE_ADMIN})
@RestController
@Slf4j
@RequestMapping(DOWNLOAD_PATH)
public class ExportController {

    public interface FilenameStrategy {
        public String getName();
    }

    private final CsvDownload download;
    private final FilenameStrategy nameStrat;
    private final TaskScheduler exec;

    public ExportController(
            @Qualifier("core_ds") DataSource assetDs,
            FilenameStrategy fNameStrat,
            TaskScheduler exec
    ) {
        this.download = new CsvDownload(assetDs);
        this.nameStrat = fNameStrat;
        this.exec = exec;
    }

    @RequestMapping(
            method = RequestMethod.GET, value = "/exporter"
    )
    public ResponseEntity exportCore() throws Exception {
        final String query = DtpwReport.CORE_QUERY;

        final String extension = ".csv";
        return zipExport(extension, query);
    }

    @RequestMapping(
            method = RequestMethod.GET, value = "/exporter/duplicate-check"
    )
    public ResponseEntity exportDuplicateAssets() throws Exception {
        final String query = DtpwReport.DUPLICATE_ASSET_QUERY;
        final String extension = ".csv";

        return zipExport(extension, query);
    }


    private ResponseEntity serveFile(Path filePath) throws Exception {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=/download/importer/" + filePath.getFileName().toString())
                .contentType(MediaType.asMediaType(MimeType.valueOf("application/zip")))
                .body(null);
    }


    private ResponseEntity zipExport(String extension, String query) throws Exception{
        final String fileName = nameStrat.getName();
        final String filePath = fileName+".zip";
        final ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(filePath));
        final ZipEntry zipEntry = new ZipEntry(fileName + extension);
        zipOut.putNextEntry(zipEntry);
        download.get(zipOut, query);
        zipOut.finish();
        zipOut.close();

        exec.schedule(()->{
            try {
                Files.deleteIfExists(Paths.get(filePath));
            } catch (Exception e) {
                log.error("Unable to delete file " + filePath, e);
            }
        }, new Date(System.currentTimeMillis()+ TimeUnit.HOURS.toMillis(2)));

        return serveFile(Paths.get(filePath));
    }
}
