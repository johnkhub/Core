package za.co.imqs.coreservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuantityDto {
    public UUID asset_id;
    public String name;
    public String unit_code;
    public String num_units;
}
