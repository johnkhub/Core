package za.co.imqs.coreservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class AssetFloor extends CoreAsset {

    public void validate(boolean create) {
        super.validate(create);
    }
}
