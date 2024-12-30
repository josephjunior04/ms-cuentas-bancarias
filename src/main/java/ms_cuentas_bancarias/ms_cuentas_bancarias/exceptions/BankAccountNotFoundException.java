package ms_cuentas_bancarias.ms_cuentas_bancarias.exceptions;

public class BankAccountNotFoundException extends RuntimeException {

    public BankAccountNotFoundException(final String message) {
        super(message);
    }

}
