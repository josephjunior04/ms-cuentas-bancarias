package ms_cuentas_bancarias.ms_cuentas_bancarias.utils;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public enum CreditType {
    PERSONAL("PERSONAL"),

    BUSINESS("BUSINESS"),

    CARD("CARD");

    private String value;

    @JsonValue
    public String getValue() {
        return value;
    }
}
