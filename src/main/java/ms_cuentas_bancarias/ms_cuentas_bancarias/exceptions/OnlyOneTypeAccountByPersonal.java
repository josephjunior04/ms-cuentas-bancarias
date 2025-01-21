package ms_cuentas_bancarias.ms_cuentas_bancarias.exceptions;

public class OnlyOneTypeAccountByPersonal extends RuntimeException {
    public OnlyOneTypeAccountByPersonal(final String message) {
        super(message);
    }
}
