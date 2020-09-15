package za.co.imqs.coreservice.dataaccess;

import java.util.Set;

public interface Meta {
    public Set<String> userSchemas();
    public Set<String> systemSchemas();
}
