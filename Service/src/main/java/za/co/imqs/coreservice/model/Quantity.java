package za.co.imqs.coreservice.model;

import lombok.Data;
import za.co.imqs.coreservice.dto.asset.QuantityDto;

import java.math.BigDecimal;
import java.util.UUID;

import static za.co.imqs.coreservice.model.Validation.assertSet;



@Data
public class Quantity {
    public UUID asset_id;
    public String name;
    public String unit_code;
    public BigDecimal num_units;

    public static Quantity of(QuantityDto dto, boolean create) {
        assertSet(dto.getAsset_id(), "asset_id", dto);
        assertSet(dto.getName(), "name", dto);
        if (create) {
            assertSet(dto.getUnit_code(), "unit_code", dto);
        }

        final Quantity q = new Quantity();
        q.setAsset_id(dto.getAsset_id());
        q.setName(dto.name);
        if (dto.getUnit_code() != null) q.setUnit_code(dto.getUnit_code());
        if (dto.getNum_units() != null) q.setNum_units(new BigDecimal(dto.getNum_units()));
        return q;
    }

    public QuantityDto toDto() {
        final QuantityDto q = new QuantityDto();
        q.setAsset_id(getAsset_id());
        q.setName(getName());
        q.setNum_units(getNum_units().toPlainString());
        q.setUnit_code(getUnit_code());
        return q;
    }
}