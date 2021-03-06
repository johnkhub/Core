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
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class UserDto {
    private UUID principal_id;
    private String name;
    private String description;
    private Boolean reserved;

    public static UserDto of(UUID principal_id, String name) {
        final UserDto u = new UserDto();
        u.setPrincipal_id(principal_id);
        u.setName(name);
        return u;
    }
}
