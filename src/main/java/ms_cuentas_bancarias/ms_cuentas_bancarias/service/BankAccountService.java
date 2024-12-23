package ms_cuentas_bancarias.ms_cuentas_bancarias.service;


import com.ms_cuentas_bancarias.model.AccountRequest;
import com.ms_cuentas_bancarias.model.AccountResponse;
import com.ms_cuentas_bancarias.model.BalanceResponse;
import com.ms_cuentas_bancarias.model.TransactionResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BankAccountService {
    Flux<AccountResponse> findAll();
    Mono<AccountResponse> findById(String id);
    Mono<AccountResponse> insert(AccountRequest accountRequest);
    Mono<AccountResponse> update(String id, AccountRequest accountRequest);
    Mono<Void> deleteById(String id);
    Mono<TransactionResponse> deposit(String idAccount);
    Mono<TransactionResponse> withdrawal(String idAccount);
    Mono<TransactionResponse> getTransactionsByAccountAndClient(String idAccount, String idClient);
    Mono<BalanceResponse> getBalanceByAccountAndClient(String idAccount, String idClient);
}
