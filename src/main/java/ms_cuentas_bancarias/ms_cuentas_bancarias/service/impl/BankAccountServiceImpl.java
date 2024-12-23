package ms_cuentas_bancarias.ms_cuentas_bancarias.service.impl;

import java.util.Collections;

import org.springframework.stereotype.Service;

import com.ms_cuentas_bancarias.model.Account;
import com.ms_cuentas_bancarias.model.AccountRequest;
import com.ms_cuentas_bancarias.model.AccountResponse;
import com.ms_cuentas_bancarias.model.BalanceResponse;
import com.ms_cuentas_bancarias.model.TransactionResponse;

import lombok.RequiredArgsConstructor;
import ms_cuentas_bancarias.ms_cuentas_bancarias.exceptions.BankAccountNotFoundException;
import ms_cuentas_bancarias.ms_cuentas_bancarias.repository.BankAccountRepository;
import ms_cuentas_bancarias.ms_cuentas_bancarias.service.BankAccountService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository bankAccountRepository;

    @Override
    public Flux<AccountResponse> findAll() {
        return bankAccountRepository.findAll().map(this::toResponseFromEntity);
    }

    @Override
    public Mono<AccountResponse> findById(String id) {
        return bankAccountRepository.findById(id)
                .switchIfEmpty(Mono
                        .error(new BankAccountNotFoundException("Bank account with ID " + id + " not found")))
                .map(this::toResponseFromEntity);
    }

    @Override
    public Mono<AccountResponse> insert(AccountRequest accountRequest) {
        return bankAccountRepository.save(toEntityFromRequest(accountRequest)).map(this::toResponseFromEntity);
    }

    @Override
    public Mono<AccountResponse> update(String id, AccountRequest accountRequest) {
        return bankAccountRepository.findById(id)
                .flatMap(accountEntity -> {
                    accountEntity.setClientId(accountRequest.getClientId());
                    accountEntity.setMaintenanceCommission(accountRequest.getMaintenanceCommission());
                    accountEntity.setTransactionLimit(accountRequest.getTransactionLimit());
                    accountEntity.setBalance(accountRequest.getBalance());
                    accountEntity.setType(accountRequest.getType());
                    accountEntity.setNroAccount(accountRequest.getNroAccount());
                    return bankAccountRepository.save(accountEntity);
                })
                .switchIfEmpty(Mono.error(
                        new BankAccountNotFoundException("Bank account with Id " + id + " not found")))
                .map(this::toResponseFromEntity);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return bankAccountRepository.findById(id)
                .switchIfEmpty(Mono.error(new BankAccountNotFoundException("Bank account with Id " + id + " not found")))
                .flatMap(accountEntity -> {
                    return bankAccountRepository.deleteById(accountEntity.getId());
                });
    }

    @Override
    public Mono<TransactionResponse> deposit(String idAccount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deposit'");
    }

    @Override
    public Mono<TransactionResponse> withdrawal(String idAccount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'withdrawal'");
    }

    @Override
    public Mono<TransactionResponse> getTransactionsByAccountAndClient(String idAccount, String idClient) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTransactionsByAccountAndClient'");
    }

    @Override
    public Mono<BalanceResponse> getBalanceByAccountAndClient(String idAccount, String idClient) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBalanceByAccountAndClient'");
    }

    private AccountResponse toResponseFromEntity(Account account) {
        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setId(account.getId());
        accountResponse.setOpeningDate(account.getOpeningDate());
        accountResponse.setType(account.getType());
        accountResponse.setBalance(account.getBalance());
        accountResponse.setTransactionLimit(account.getTransactionLimit());
        accountResponse.setAuthorizedSigners(account.getAuthorizedSigners());
        accountResponse.setMaintenanceCommission(account.getMaintenanceCommission());
        accountResponse.setHolders(account.getHolders());
        accountResponse.setTransactions(account.getTransactions());
        accountResponse.setClientId(account.getClientId());
        accountResponse.setNroAccount(account.getNroAccount());
        return accountResponse;
    }

    private Account toEntityFromRequest(AccountRequest accountRequest) {
        Account account = new Account();
        account.setNroAccount(accountRequest.getNroAccount());
        account.setType(accountRequest.getType());
        account.setMaintenanceCommission(accountRequest.getMaintenanceCommission());
        account.setTransactionLimit(accountRequest.getTransactionLimit());
        account.setBalance(accountRequest.getBalance());
        account.setClientId(accountRequest.getClientId());
        account.setOpeningDate(accountRequest.getOpeningDate());
        account.setAuthorizedSigners(Collections.emptyList());
        account.setHolders(Collections.emptyList());
        account.setTransactions(Collections.emptyList());
        return account;
    }

}
