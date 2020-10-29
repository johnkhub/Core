package za.co.imqs.coreservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface ErrorProvider {
    @JsonIgnore
    public String getError();

    @JsonIgnore
    public void setError(String error);

}
