package ms_cuentas_bancarias.ms_cuentas_bancarias.utils;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public enum SubTypeClient {
    VIP("VIP"),

    PYME("PYME");

    private String value;

    @JsonValue
    public String getValue() {
        return value;
    }
}
