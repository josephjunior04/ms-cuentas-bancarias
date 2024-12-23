package ms_cuentas_bancarias.ms_cuentas_bancarias.controller;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.ms_cuentas_bancarias.api.V1Api;
import com.ms_cuentas_bancarias.model.AccountRequest;
import com.ms_cuentas_bancarias.model.AccountResponse;
import com.ms_cuentas_bancarias.model.BalanceResponse;
import com.ms_cuentas_bancarias.model.TransactionRequest;
import com.ms_cuentas_bancarias.model.TransactionResponse;

import lombok.RequiredArgsConstructor;
import ms_cuentas_bancarias.ms_cuentas_bancarias.service.BankAccountService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class BankAccountController implements V1Api{

    private final BankAccountService bankAccountService;

    @Override
    public Mono<ResponseEntity<Void>> delete(String idCuenta, ServerWebExchange exchange) {
        return bankAccountService.deleteById(idCuenta)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    @Override
    public Mono<ResponseEntity<TransactionResponse>> deposit(String idAccount,
            @Valid Mono<TransactionRequest> transactionRequest, ServerWebExchange exchange) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deposito'");
    }

    @Override
    public Mono<ResponseEntity<Flux<AccountResponse>>> findAllAccounts(ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(bankAccountService.findAll()));
    }

    @Override
    public Mono<ResponseEntity<AccountResponse>> findById(String idAccount, ServerWebExchange exchange) {
        return bankAccountService.findById(idAccount)
                .map(accountResponse -> {
                    return ResponseEntity
                            .status(HttpStatus.OK)
                            .body(accountResponse);
                });
    }

    @Override
    public Mono<ResponseEntity<AccountResponse>> insert(@Valid Mono<AccountRequest> accountRequest,
            ServerWebExchange exchange) {
        return accountRequest
                .flatMap(request -> bankAccountService.insert(request)
                        .map(accountResponse -> {
                            return ResponseEntity
                                    .status(HttpStatus.CREATED)
                                    .body(accountResponse);
                        }));
    }

    @Override
    public Mono<ResponseEntity<TransactionResponse>> getTransactionsByAccountAndClient(String idAccount,
            String idClient, ServerWebExchange exchange) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTransactionsByAccountAndClient'");
    }

    @Override
    public Mono<ResponseEntity<BalanceResponse>> getBalanceByAccountAndClient(String idAccount, String idClient,
            ServerWebExchange exchange) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBalanceByAccountAndClient'");
    }

    @Override
    public Mono<ResponseEntity<TransactionResponse>> withdrawal(String idAccount,
            @Valid Mono<TransactionRequest> transactionRequest, ServerWebExchange exchange) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'withdrawal'");
    }

    @Override
    public Mono<ResponseEntity<AccountResponse>> update(String idAccount, @Valid Mono<AccountRequest> accountRequest,
            ServerWebExchange exchange) {
                return accountRequest
                .flatMap(request -> bankAccountService.update(idAccount, request)
                        .map(accountResponse -> {
                            return ResponseEntity
                                    .status(HttpStatus.CREATED)
                                    .body(accountResponse);
                        }));
    }

}
