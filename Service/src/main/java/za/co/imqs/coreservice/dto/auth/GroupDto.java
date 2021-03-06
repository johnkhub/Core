package za.co.imqs.coreservice.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.UUID;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/07
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GroupDto {
    private UUID group_id;
    private String name;
    private String description;
    private Boolean reserved;

    public static GroupDto of(UUID group_id, String  name) {
        final GroupDto g = new GroupDto();
        g.setGroup_id(group_id);
        g.setName((name));
        return g;
    }
}
