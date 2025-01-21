package ms_cuentas_bancarias.ms_cuentas_bancarias.service.impl.strategy;

import com.ms_cuentas_bancarias.model.Account;
import com.ms_cuentas_bancarias.model.AccountRequest;
import com.ms_cuentas_bancarias.model.BalanceResponse;
import com.ms_cuentas_bancarias.model.TransactionRequest;
import com.ms_cuentas_bancarias.model.TransactionResponse;
import com.ms_cuentas_bancarias.model.TransferRequest;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BankAccountStrategy {
    Mono<Account> save(AccountRequest accountRequest);

    Mono<TransactionResponse> deposit(String id, TransactionRequest transactionRequest);

    Mono<TransactionResponse> withdraw(String id, TransactionRequest transactionRequest);

    Mono<BalanceResponse> balance(String id);

    Flux<TransactionResponse> getTransactionsByAccount(String idAccount);

    Mono<TransactionResponse> transfer(TransferRequest transferRequest);

}
