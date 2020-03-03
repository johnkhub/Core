package za.co.imqs.coreservice.dto;

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
}
