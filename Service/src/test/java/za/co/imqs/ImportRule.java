package za.co.imqs;

import org.junit.rules.ExternalResource;
import za.co.imqs.coreservice.imports.Importer;

public class ImportRule extends ExternalResource {

    private String type;
    private String file;
    private String[] flags;
    private String config;
    private String lookupType;
    
    public ImportRule usingConfig(String config) {
        this.config = config;
        return this;
    }

    public ImportRule asType(String type) {
        this.type = type;
        return this;
    }

    public ImportRule fromFile(String file) {
        this.file = file;
        return this;
    }
    

    public ImportRule withFlags(String ...flags) {
        this.flags = flags;
        return this;
    }

    public ImportRule asLookups(String lookupType) {
        this.lookupType = lookupType;
        this.type = "lookups";
        return this;
    }

    public ImportRule asAssets() {
        this.type = "assets";
        return this;
    }

    public void importFile() throws Exception {
        importFile(this.file);
    }

    public void importFile(String file) throws Exception {
        if (type.equalsIgnoreCase("lookups")) {
            Importer.main(new String[]{config, type, file, lookupType});
        } else {
            Importer.main(new String[]{config, type, file, String.join(",", flags)});
        }
    }

    @Override
    protected void before() throws Throwable {
        importFile();
    }
}
