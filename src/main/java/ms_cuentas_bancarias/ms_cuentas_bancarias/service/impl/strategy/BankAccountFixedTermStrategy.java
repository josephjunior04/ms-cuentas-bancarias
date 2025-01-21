package ms_cuentas_bancarias.ms_cuentas_bancarias.service.impl.strategy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ms_cuentas_bancarias.model.Account;
import com.ms_cuentas_bancarias.model.AccountRequest;
import com.ms_cuentas_bancarias.model.BalanceResponse;
import com.ms_cuentas_bancarias.model.BankAccountType;
import com.ms_cuentas_bancarias.model.Transaction;
import com.ms_cuentas_bancarias.model.TransactionRequest;
import com.ms_cuentas_bancarias.model.TransactionResponse;
import com.ms_cuentas_bancarias.model.TransactionType;
import com.ms_cuentas_bancarias.model.TransferRequest;

import lombok.RequiredArgsConstructor;
import ms_cuentas_bancarias.ms_cuentas_bancarias.exceptions.BankAccountNotFoundException;
import ms_cuentas_bancarias.ms_cuentas_bancarias.repository.BankAccountRepository;
import ms_cuentas_bancarias.ms_cuentas_bancarias.repository.TransactionRepository;
import ms_cuentas_bancarias.ms_cuentas_bancarias.utils.ClientResponse;
import ms_cuentas_bancarias.ms_cuentas_bancarias.utils.CommonStrategy;
import ms_cuentas_bancarias.ms_cuentas_bancarias.utils.TypeClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Component
@RequiredArgsConstructor
public class BankAccountFixedTermStrategy implements BankAccountStrategy {

    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;
    private final CommonStrategy commonStrategy;
    private static final Logger LOGGER = LoggerFactory.getLogger(BankAccountFixedTermStrategy.class);
    private static final LocalDate DAY_ALLOWED_TRANSACTION = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
    private static final Integer MAX_LIMIT_TRANSACTION_PER_MONTH = 1;

    /**
     * @param AccountRequest account request
     * @return Account Mono saved
     */
    @Override
    public Mono<Account> save(final AccountRequest accountRequest) {
        LOGGER.info("Saving account: {}", accountRequest.getNroAccount());
        return commonStrategy.getClient(accountRequest.getClientId())
                .flatMap(client -> validateOnlyOneCreditByType(accountRequest, client))
                .then(Mono.just(commonStrategy.toEntityFromRequest(accountRequest)))
                .flatMap(bankAccountRepository::save);
    }

    private Mono<Void> validateOnlyOneCreditByType(final AccountRequest accountRequest,
            final ClientResponse clientResponse) {
        if (clientResponse.getType().equals(TypeClient.PERSONAL)) {
            return commonStrategy.validateOneHaveOneTypeAccountForPersonalClient(accountRequest);
        }

        if (clientResponse.getType().equals(TypeClient.BUSINESS)) {
            return commonStrategy.handleErrorClient(accountRequest.getType(), TypeClient.BUSINESS);
        }

        return Mono.empty();
    }

    /**
     * @param TransactionRequest transaction request
     * @param id                 Bank account ID
     * @return TransactionResponse Mono of current deposit
     */
    @Override
    public Mono<TransactionResponse> deposit(final String id, final TransactionRequest transactionRequest) {
        LOGGER.info("Starting deposit process to the following bank account id {}", id);
        return processTransaction(id, transactionRequest, TransactionType.DEPOSIT);
    }

    /**
     * @param TransactionRequest transaction request
     * @param id                 Bank account ID
     * @return TransactionResponse Mono of current withdraw
     */
    @Override
    public Mono<TransactionResponse> withdraw(final String id, final TransactionRequest transactionRequest) {
        LOGGER.info("Starting withdraw process to the following bank account id {}", id);
        return processTransaction(id, transactionRequest, TransactionType.WITHDRAWAL);
    }

    private Mono<TransactionResponse> processTransaction(
            final String id,
            final TransactionRequest transactionRequest,
            final TransactionType transactionType) {
        return commonStrategy.validateAndRetrieveAccount(id, transactionRequest, transactionType)
                .flatMap(bankAccount -> commonStrategy.countTransactionsThisMonth(id)
                        .flatMap(transactionCount -> validateAndProcessTransaction(bankAccount, transactionRequest,
                                transactionType, transactionCount)));
    }

    private Mono<TransactionResponse> validateAndProcessTransaction(
            final Account bankAccount,
            final TransactionRequest transactionRequest,
            final TransactionType transactionType,
            final int transactionCount) {

        if (LocalDate.now().equals(DAY_ALLOWED_TRANSACTION)) {
            return Mono.error(new RuntimeException(
                    String.format("Transaction can only be made on the day %s", DAY_ALLOWED_TRANSACTION.toString())));
        }

        if (transactionCount >= MAX_LIMIT_TRANSACTION_PER_MONTH) {
            return Mono.error(new RuntimeException("Only one transaction can be made on this day"));
        }

        return updateAccountAndSaveTransaction(bankAccount, transactionRequest, transactionType);
    }

    private Mono<TransactionResponse> updateAccountAndSaveTransaction(
            final Account bankAccount,
            final TransactionRequest transactionRequest,
            final TransactionType transactionType) {
        if (transactionType == TransactionType.DEPOSIT) {
            bankAccount.setBalance(bankAccount.getBalance().add(transactionRequest.getAmount()));
        } else if (transactionType == TransactionType.WITHDRAWAL) {
            bankAccount.setBalance(bankAccount.getBalance().subtract(transactionRequest.getAmount()));
        }

        return bankAccountRepository.save(bankAccount)
                .flatMap(updatedAccount -> {
                    Transaction transaction = new Transaction();
                    transaction.productId(updatedAccount.getId());
                    transaction.setAmount(transactionRequest.getAmount());
                    transaction.setType(transactionType);
                    transaction.setDate(LocalDate.now());

                    return transactionRepository.save(transaction)
                            .map(savedTransaction -> {
                                TransactionResponse response = new TransactionResponse();
                                response.setType(transactionType);
                                response.setAmount(savedTransaction.getAmount());
                                response.setDate(savedTransaction.getDate());
                                return response;
                            });
                });
    }

    /**
     * @param id Bank account ID
     * @return BalanceResponse Mono of current bank account
     */
    @Override
    public Mono<BalanceResponse> balance(final String id) {
        LOGGER.info("Get balance by Id account {}", id);
        return commonStrategy.getBalanceByIdAccount(id);
    }

    /**
     * @param idAccount Bank account ID
     * @return TransactionResponse Flux of transactions
     */
    @Override
    public Flux<TransactionResponse> getTransactionsByAccount(final String idAccount) {
        LOGGER.info("Get transactions by Id account {}", idAccount);
        return commonStrategy.getTransactionsByIdAccount(idAccount);
    }

    /**
     * @param TransferRequest transfer request
     * @return TransactionResponse Mono of transfer
     */
    @Override
    public Mono<TransactionResponse> transfer(final TransferRequest transferRequest) {
        LOGGER.info("Making transfer from the following account {}", transferRequest.getSourceAccountId());

        return validateTransferAmount(transferRequest)
                .then(validateAccountsExistence(transferRequest))
                .flatMap(tuple -> {
                    Account sourceAccount = tuple.getT1();
                    Account targetAccount = tuple.getT2();

                    if (!hasSufficientBalance(sourceAccount, transferRequest.getTransactionRequest().getAmount())) {
                        return Mono.error(new RuntimeException("Insufficient balance in source account"));
                    }

                    return validateTransactionLimitsAndReturnComission(transferRequest, targetAccount)
                            .then(processTransfer(sourceAccount, targetAccount,
                            transferRequest.getTransactionRequest()));
                });
    }

    private Mono<Void> validateTransferAmount(final TransferRequest transferRequest) {
        if (transferRequest.getTransactionRequest().getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new RuntimeException("Transfer amount must be greater than zero"));
        }
        return Mono.empty();
    }

    private Mono<Tuple2<Account, Account>> validateAccountsExistence(final TransferRequest transferRequest) {
        return bankAccountRepository.findById(transferRequest.getSourceAccountId())
                .switchIfEmpty(Mono.error(new BankAccountNotFoundException(
                        String.format("Source account with id: %s not found", transferRequest.getSourceAccountId()))))
                .zipWith(bankAccountRepository.findById(transferRequest.getTargetAccountId())
                        .switchIfEmpty(Mono.error(new BankAccountNotFoundException(
                                String.format("Target account with id: %s not found",
                                        transferRequest.getTargetAccountId())))));
    }

    private boolean hasSufficientBalance(final Account sourceAccount, final BigDecimal amount) {
        return sourceAccount.getBalance().compareTo(amount) >= 0;
    }

    private Mono<Void> validateTransactionLimitsAndReturnComission(final TransferRequest transferRequest,
            final Account targetAccount) {
        Mono<Void> validateSourceTransactions = commonStrategy
                .countTransactionsThisMonth(transferRequest.getSourceAccountId())
                .flatMap(transactionCountSource -> {
                    if (transactionCountSource == MAX_LIMIT_TRANSACTION_PER_MONTH) {
                        return Mono.error(new RuntimeException(
                                "Exceeded number of transactions allowed per month for fixed-term account"));
                    }
                    return Mono.empty();
                });

        if (BankAccountType.FIXED_TERM.equals(targetAccount.getType())) {
            return commonStrategy.countTransactionsThisMonth(transferRequest.getTargetAccountId())
                    .flatMap(transactionCountTarget -> {
                        if (transactionCountTarget == 1) {
                            return Mono.error(new RuntimeException(
                                    "Exceeded number of transactions allowed per month for fixed-term account"));
                        }
                        return validateSourceTransactions;
                    });
        }

        return Mono.empty();
    }

    private Mono<TransactionResponse> processTransfer(
            final Account sourceAccount,
            final Account targetAccount,
            final TransactionRequest transactionRequest) {

        sourceAccount
                .setBalance(sourceAccount.getBalance().subtract(transactionRequest.getAmount()));
        targetAccount.setBalance(targetAccount.getBalance().add(transactionRequest.getAmount()));

        Transaction withdrawalTransaction = new Transaction();
        withdrawalTransaction.setProductId(sourceAccount.getId());
        withdrawalTransaction.setAmount(transactionRequest.getAmount());
        withdrawalTransaction.setType(TransactionType.TRANSFER);
        withdrawalTransaction.setDate(LocalDate.now());

        Transaction depositTransaction = new Transaction();
        depositTransaction.setProductId(targetAccount.getId());
        depositTransaction.setAmount(transactionRequest.getAmount());
        depositTransaction.setType(TransactionType.DEPOSIT);
        depositTransaction.setDate(LocalDate.now());

        return bankAccountRepository.save(sourceAccount)
                .then(bankAccountRepository.save(targetAccount))
                .then(transactionRepository.save(withdrawalTransaction))
                .then(transactionRepository.save(depositTransaction))
                .then(Mono.just(commonStrategy.buildTransferResponse(targetAccount, transactionRequest)));
    }

}
