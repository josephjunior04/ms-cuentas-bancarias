package ms_cuentas_bancarias.ms_cuentas_bancarias.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ms_cuentas_bancarias.model.Account;
import com.ms_cuentas_bancarias.model.AccountRequest;
import com.ms_cuentas_bancarias.model.AccountResponse;
import com.ms_cuentas_bancarias.model.BalanceResponse;
import com.ms_cuentas_bancarias.model.FilterRequest;
import com.ms_cuentas_bancarias.model.SummaryAccountResponse;
import com.ms_cuentas_bancarias.model.Transaction;
import com.ms_cuentas_bancarias.model.TransactionRequest;
import com.ms_cuentas_bancarias.model.TransactionResponse;
import com.ms_cuentas_bancarias.model.TransactionType;
import com.ms_cuentas_bancarias.model.TransferRequest;

import lombok.RequiredArgsConstructor;
import ms_cuentas_bancarias.ms_cuentas_bancarias.exceptions.BankAccountNotFoundException;
import ms_cuentas_bancarias.ms_cuentas_bancarias.repository.BankAccountRepository;
import ms_cuentas_bancarias.ms_cuentas_bancarias.repository.TransactionRepository;
import ms_cuentas_bancarias.ms_cuentas_bancarias.service.BankAccountService;
import ms_cuentas_bancarias.ms_cuentas_bancarias.service.impl.strategy.BankAccountStrategyFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;
    private final BankAccountStrategyFactory bankAccountStrategyFactory;

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
        return bankAccountStrategyFactory
                .getStrategy(accountRequest.getType())
                .save(accountRequest)
                .map(this::toResponseFromEntity);
    }

    /**
     * @param id             Current id account to delete
     * @param accountRequest Current account updated
     * @return Mono Account response when has been account updated
     */
    @Override
    public Mono<AccountResponse> update(final String id, final AccountRequest accountRequest) {
        return bankAccountRepository.findById(id)
                .flatMap(accountEntity -> {
                    accountEntity.setClientId(accountRequest.getClientId());
                    // accountEntity.setMaintenanceCommission(accountRequest.getMaintenanceCommission());
                    // accountEntity.setTransactionLimit(accountRequest.getTransactionLimit());
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
        return bankAccountRepository.findById(idAccount).map(this::toBalanceResponseFromBankAccount);
    }

    private BalanceResponse toBalanceResponseFromBankAccount(final Account account) {
        BalanceResponse balanceResponse = new BalanceResponse();
        balanceResponse.setProductId(account.getId());
        balanceResponse.setBalanceAccount(account.getBalance());
        balanceResponse.setNroAccount(account.getNroAccount());
        balanceResponse.setType(account.getType());
        return balanceResponse;
    }

    private AccountResponse toResponseFromEntity(final Account account) {
        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setId(account.getId());
        accountResponse.setOpeningDate(account.getOpeningDate());
        accountResponse.setType(account.getType());
        accountResponse.setBalance(account.getBalance());
        // accountResponse.setTransactionLimit(account.getTransactionLimit());
        accountResponse.setAuthorizedSigners(account.getAuthorizedSigners());
        // accountResponse.setMaintenanceCommission(account.getMaintenanceCommission());
        accountResponse.setHolders(account.getHolders());
        // accountResponse.setTransactions(account.getTransactions());
        accountResponse.setClientId(account.getClientId());
        accountResponse.setNroAccount(account.getNroAccount());
        return accountResponse;
    }

    /**
     * @param idAccount Id account
     * @return Mono of Transaction response to transfer
     */
    @Override
    public Mono<TransactionResponse> transfer(final TransferRequest transferRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'transfer'");
    }

    /**
     * @return Flux of BalanceResponse of daily average balances
     * @param clientId Current client id
     */
    @Override
    public Flux<BalanceResponse> getDailyAverageBalances(final String clientId) {
        return bankAccountRepository.findByClientId(clientId)
                .flatMap(account -> calculateAverageDailyBalance(account));
    }

    private Mono<BalanceResponse> calculateAverageDailyBalance(final Account account) {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate today = LocalDate.now();

        return transactionRepository.findByProductIdAndDateBetween(account.getId(), startOfMonth, today)
                .collectList()
                .map(transactions -> {
                    BigDecimal totalBalance = transactions.stream()
                            .map(Transaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal averageDailyBalance = totalBalance.divide(
                            BigDecimal.valueOf(transactions.size()), RoundingMode.HALF_UP);

                    BalanceResponse balanceResponse = new BalanceResponse();
                    balanceResponse.setProductId(account.getId());
                    balanceResponse.setNroAccount(account.getNroAccount());
                    balanceResponse.setType(account.getType());
                    balanceResponse.setBalanceAccount(averageDailyBalance);
                    return balanceResponse;
                });
    }

    /**
     * @return Flux of BalanceResponse of balances by client
     * @param clientId Current client id
     */
    @Override
    public Flux<BalanceResponse> getBalancesByClient(final String clientId) {
        return bankAccountRepository.findByClientId(clientId).map(this::toBalanceResponseFromBankAccount);
    }

    /**
     * @return Flux of BalanceResponse of daily average balances
     * @param clientId Current client id
     */
    @Override
    public Flux<SummaryAccountResponse> getSummaryByClient(final String clientId, final FilterRequest filterRequest) {
        return bankAccountRepository.findByClientId(clientId)
                .switchIfEmpty(Flux.empty())
                .flatMap(account -> transactionRepository.findByProductIdAndDateBetween(
                        account.getId(), filterRequest.getStartDate(), filterRequest.getEndDate())
                        .switchIfEmpty(Flux.empty())
                        .collectList()
                        .map(transactions -> toSummaryAccountFromAccount(account, transactions))
                );
    }

    private SummaryAccountResponse toSummaryAccountFromAccount(final Account account,
            final List<Transaction> transactions) {
        SummaryAccountResponse summaryAccountResponse = new SummaryAccountResponse();
        summaryAccountResponse.setType(account.getType());
        summaryAccountResponse.setBalance(account.getBalance());
        summaryAccountResponse.setOpeningDate(account.getOpeningDate());

        List<TransactionResponse> transactionResponses = transactions.stream()
                .map(transaction -> {
                    TransactionResponse transactionResponse = new TransactionResponse();
                    transactionResponse.setType(transaction.getType());
                    transactionResponse.setMotive(transaction.getMotive());
                    transactionResponse.setAmount(transaction.getAmount());
                    transactionResponse.setDate(transaction.getDate());
                    return transactionResponse;
                })
                .toList();

        summaryAccountResponse.setTransactions(transactionResponses);
        return summaryAccountResponse;
    }
}
