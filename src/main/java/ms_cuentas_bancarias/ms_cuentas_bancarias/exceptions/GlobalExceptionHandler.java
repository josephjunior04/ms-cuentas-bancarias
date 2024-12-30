package ms_cuentas_bancarias.ms_cuentas_bancarias.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.ms_cuentas_bancarias.model.ErrorResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @return Response Entity of Error response with status
     * @param ex Custom Excepction Bank Account not found
     */
    @ExceptionHandler(BankAccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFoundException(final BankAccountNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setError("Cuenta not found");
        errorResponse.setMessage(ex.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
}
