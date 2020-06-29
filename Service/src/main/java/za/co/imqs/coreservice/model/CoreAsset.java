package za.co.imqs.coreservice.model;

import lombok.Data;
import za.co.imqs.coreservice.dataaccess.exception.ValidationFailureException;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/05
 */
@Data
public class CoreAsset {
    public static final boolean CREATE = true;
    public static final boolean UPDATE = false;

    private UUID asset_id;
    private String asset_type_code;
    private String code;
    private String name;
    private String adm_path;
    private String func_loc_path;
    private Timestamp creation_date;
    private Timestamp deactivated_at;

    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;

    private String barcode;
    private String serial_number;

    private String geom;

    private String responsible_dept_code;
    private Boolean is_owned;

    public void validate(boolean create) {
        if (asset_id == null) throw new ValidationFailureException("asset_id is null");
    }

    protected void validateCode(String code) {
        // TODO
    }

    protected void validateFunLocPath(String code) {
        // TODO
    }
}