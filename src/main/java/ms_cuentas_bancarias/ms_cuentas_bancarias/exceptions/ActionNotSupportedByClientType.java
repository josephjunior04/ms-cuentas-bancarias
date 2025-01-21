package ms_cuentas_bancarias.ms_cuentas_bancarias.exceptions;

public class ActionNotSupportedByClientType extends RuntimeException {
    public ActionNotSupportedByClientType(final String message) {
        super(message);
    }
}
