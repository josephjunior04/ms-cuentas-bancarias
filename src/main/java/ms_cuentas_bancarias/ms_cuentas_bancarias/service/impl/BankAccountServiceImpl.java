package ms_cuentas_bancarias.ms_cuentas_bancarias.service.impl;

import java.time.LocalDate;
import java.util.Collections;

import org.springframework.stereotype.Service;

import com.ms_cuentas_bancarias.model.Account;
import com.ms_cuentas_bancarias.model.AccountRequest;
import com.ms_cuentas_bancarias.model.AccountResponse;
import com.ms_cuentas_bancarias.model.BalanceResponse;
import com.ms_cuentas_bancarias.model.Transaction;
import com.ms_cuentas_bancarias.model.TransactionRequest;
import com.ms_cuentas_bancarias.model.TransactionResponse;
import com.ms_cuentas_bancarias.model.TransactionType;

import lombok.RequiredArgsConstructor;
import ms_cuentas_bancarias.ms_cuentas_bancarias.exceptions.BankAccountNotFoundException;
import ms_cuentas_bancarias.ms_cuentas_bancarias.repository.BankAccountRepository;
import ms_cuentas_bancarias.ms_cuentas_bancarias.repository.TransactionRepository;
import ms_cuentas_bancarias.ms_cuentas_bancarias.service.BankAccountService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;

    /**
     * @return Flux Account response of all accounts
     */
    @Override
    public Flux<AccountResponse> findAll() {
        return bankAccountRepository.findAll().map(this::toResponseFromEntity);
    }

    /**
     * @param id Current id account to search
     * @return Mono Account response when has been account founded
     */
    @Override
    public Mono<AccountResponse> findById(final String id) {
        return bankAccountRepository.findById(id)
                .switchIfEmpty(Mono
                        .error(new BankAccountNotFoundException("Bank account with ID " + id + " not found")))
                .map(this::toResponseFromEntity);
    }

    /**
     * @param accountRequest Current account saved
     * @return Mono Account response when has been account saved
     */
    @Override
    public Mono<AccountResponse> insert(final AccountRequest accountRequest) {
        return bankAccountRepository.save(toEntityFromRequest(accountRequest)).map(this::toResponseFromEntity);
    }

    /**
     * @param id Current id account to delete
     * @param accountRequest Current account updated
     * @return Mono Account response when has been account updated
     */
    @Override
    public Mono<AccountResponse> update(final String id, final AccountRequest accountRequest) {
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

    /**
     * @param id Current id account to delete
     * @return Mono void when has been account deleted
     */
    @Override
    public Mono<Void> deleteById(final String id) {
        return bankAccountRepository.findById(id)
                .switchIfEmpty(
                    Mono.error(new BankAccountNotFoundException("Bank account with Id " + id + " not found")))
                .flatMap(accountEntity -> {
                    return bankAccountRepository.deleteById(accountEntity.getId());
                });
    }

    /**
     * @param idAccount Id account
     * @return Mono of Transactions response by account
     */
    @Override
    public Mono<TransactionResponse> deposit(final String idAccount, final TransactionRequest transactionRequest) {
        return transactionRepository
                .save(toEntityFromRequestTransaction(transactionRequest, idAccount, TransactionType.DEPOSIT))
                .map(this::toResponseFromEntity);
    }

    /**
     * @param idAccount Id account
     * @return Mono of Transactions response by account
     */
    @Override
    public Mono<TransactionResponse> withdraw(final String idAccount, final TransactionRequest transactionRequest) {
        return transactionRepository
                .save(toEntityFromRequestTransaction(transactionRequest, idAccount, TransactionType.WITHDRAWAL))
                .map(this::toResponseFromEntity);
    }

    private Transaction toEntityFromRequestTransaction(
        final TransactionRequest transactionRequest,
        final String id, final TransactionType transactionType) {
        Transaction transaction = new Transaction();
        transaction.setProductId(id);
        transaction.setAmount(transactionRequest.getAmount());
        transaction.setDate(LocalDate.now());
        transaction.setMotive(transactionRequest.getMotive());
        transaction.setType(transactionType);
        return transaction;
    }

    private TransactionResponse toResponseFromEntity(final Transaction transaction) {
        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setAmount(transaction.getAmount());
        transactionResponse.setDate(transaction.getDate());
        transactionResponse.setMotive(transaction.getMotive());
        transactionResponse.setType(transaction.getType());
        return transactionResponse;
    }

    /**
     * @param idAccount Id account
     * @return Mono of Transactions response by account
     */
    @Override
    public Flux<TransactionResponse> getTransactionsByAccount(final String idAccount) {
        Flux<Transaction> transactionFlux = transactionRepository.findAllByProductId(idAccount);
        return transactionFlux.map(this::toResponseFromEntity);
    }

    /**
     * @param idAccount Id account
     * @return Mono of Balance response by account
     */
    @Override
    public Mono<BalanceResponse> getBalanceByAccount(final String idAccount) {
        return bankAccountRepository.findById(idAccount).map(this::toBalanceResponseFromCredit);
    }

    private BalanceResponse toBalanceResponseFromCredit(final Account credit) {
        BalanceResponse balanceResponse = new BalanceResponse();
        balanceResponse.setProductId(credit.getId());
        balanceResponse.setBalanceAccount(credit.getBalance());
        balanceResponse.setNroAccount(credit.getNroAccount());
        balanceResponse.setType(credit.getType());
        return balanceResponse;
    }

    private AccountResponse toResponseFromEntity(final Account account) {
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

    private Account toEntityFromRequest(final AccountRequest accountRequest) {
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
