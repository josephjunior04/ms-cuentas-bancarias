package ms_cuentas_bancarias.ms_cuentas_bancarias.utils;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class CreditResponse {
    private String id;

    private String nroCredit;

    private CreditType type;

    private BigDecimal creditLimit;

    private BigDecimal currentBalance;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate openingDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expirationDate;

    private String clientId;

    private Integer numbersOfQuota;

    private BigDecimal interestRate;

    private CreditStatus status;
}
