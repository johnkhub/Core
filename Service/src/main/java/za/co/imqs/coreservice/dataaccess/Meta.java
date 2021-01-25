package za.co.imqs.coreservice.dataaccess;

import java.util.List;
import java.util.Set;

public interface Meta {
    public Set<String> userSchemas();
    public Set<String> systemSchemas();

    public List<String> getTablesAndViewsForUser(String userName);

    public String getServerIP();
    public String getDbName();
    public List<String> listExtentions();

    public List<String> getUserTypes();
}
