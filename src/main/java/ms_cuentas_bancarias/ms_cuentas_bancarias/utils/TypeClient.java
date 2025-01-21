package ms_cuentas_bancarias.ms_cuentas_bancarias.utils;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum TypeClient {
    PERSONAL("PERSONAL"),
    BUSINESS("BUSINESS");

    private String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
