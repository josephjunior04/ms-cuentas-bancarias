package ms_cuentas_bancarias.ms_cuentas_bancarias.utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

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
import ms_cuentas_bancarias.ms_cuentas_bancarias.exceptions.ActionNotSupportedByClientType;
import ms_cuentas_bancarias.ms_cuentas_bancarias.exceptions.BankAccountNotFoundException;
import ms_cuentas_bancarias.ms_cuentas_bancarias.exceptions.ClientNotFoundException;
import ms_cuentas_bancarias.ms_cuentas_bancarias.exceptions.OnlyOneTypeAccountByPersonal;
import ms_cuentas_bancarias.ms_cuentas_bancarias.repository.BankAccountRepository;
import ms_cuentas_bancarias.ms_cuentas_bancarias.repository.TransactionRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CommonStrategy {

    private final WebClient webClient;
    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;

    /**
     * @param AccountRequest account request
     * @return Credit Void of validations
     */
    public Mono<ClientResponse> getClient(final String clientId) {
        return webClient.get()
                .uri("http://localhost:8080/v1/clients/{id}", clientId)
                .retrieve()
                .bodyToMono(ClientResponse.class)
                .onErrorResume(WebClientResponseException.NotFound.class,
                        ex -> Mono.error(new ClientNotFoundException("Client not found for ID: " + clientId)));
    }

    private Flux<CreditResponse> getCreditsByClient(final String clientId) {
        return webClient.get()
                .uri("http://localhost:8082/v1/credits/{clientId}", clientId)
                .retrieve()
                .bodyToFlux(CreditResponse.class)
                .onErrorResume(WebClientResponseException.NotFound.class,
                        ex -> Mono.error(new ClientNotFoundException("Credits not found for Client id: " + clientId)));
    }

    /**
     * @param clientId Id client
     * @return Mono Void of validation active credit card
     */
    public Mono<Void> validateActiveCreditCard(final String clientId) {
        return getCreditsByClient(clientId)
                .filter(creditResponse -> creditResponse.getType().equals(CreditType.CARD))
                .hasElements()
                .flatMap(hasActiveCredit -> {
                    if (hasActiveCredit) {
                        return Mono.empty();
                    }
                    return Mono.error(new RuntimeException("No active credit card found for client ID: " + clientId));
                });
    }

    /**
     * @param AccountRequest account request
     * @return Credit Void of validations
     */
    public Mono<Void> validateOneHaveOneTypeAccountForPersonalClient(final AccountRequest accountRequest) {
        return bankAccountRepository
                .findByTypeAndClientId(accountRequest.getType().getValue(), accountRequest.getClientId())
                .hasElements()
                .flatMap(hasAccount -> {
                    if (hasAccount) {
                        return Mono.error(new OnlyOneTypeAccountByPersonal(
                                String.format("You can only have one %s account",
                                        accountRequest.getType().getValue().toLowerCase())));
                    }
                    return Mono.empty();
                });
    }

    /**
     * @param AccountRequest account request
     * @return Credit Void of validations
     */
    public Mono<Void> handleErrorClient(final BankAccountType bankAccountType, final TypeClient typeClient) {
        return Mono.error(new ActionNotSupportedByClientType(
                String.format("%s type customer cannot create %s account",
                        typeClient.getValue(), bankAccountType.getValue())));
    }

    /**
     * @param AccountRequest account request
     * @return Credit Void of validations
     */
    public Account toEntityFromRequest(final AccountRequest accountRequest) {
        Account account = new Account();
        account.setNroAccount(accountRequest.getNroAccount());
        account.setClientId(accountRequest.getClientId());
        account.setType(accountRequest.getType());
        account.setOpeningDate(LocalDate.now());
        account.setHolders(accountRequest.getHolders());
        account.setAuthorizedSigners(accountRequest.getAuthorizedSigners());
        account.setBalance(accountRequest.getBalance());
        return account;
    }

    /**
     * @param AccountRequest account request
     * @return Credit Void of validations
     */
    public Mono<Integer> countTransactionsThisMonth(final String productId) {
        YearMonth currentYearMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentYearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentYearMonth.atEndOfMonth().atTime(LocalTime.MAX);

        return transactionRepository.countByProductIdAndDateBetween(productId, startOfMonth, endOfMonth);
    }

    /**
     * @param AccountRequest account request
     * @return Credit Void of validations
     */
    public Mono<Account> validateAndRetrieveAccount(
            final String id,
            final TransactionRequest transactionRequest,
            final TransactionType transactionType) {
        return bankAccountRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Bank account not found")))
                .flatMap(bankAccount -> validateAndReturnAccount(bankAccount, transactionRequest, transactionType));
    }

    private Mono<Account> validateAndReturnAccount(final Account account,
            final TransactionRequest transactionRequest, final TransactionType transactionType) {
        boolean amountIsLessThanOrEqualToZero = transactionRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0;
        if (amountIsLessThanOrEqualToZero) {
            return Mono.error(new RuntimeException("Deposit amount must be greater than zero"));
        }

        boolean isTransactionTypeWithdraw = transactionType == TransactionType.WITHDRAWAL;
        boolean amountIsLessThanBalance = account.getBalance().compareTo(transactionRequest.getAmount()) < 0;
        if (isTransactionTypeWithdraw && amountIsLessThanBalance) {
            return Mono.error(new RuntimeException("Insufficient balance"));
        }

        return Mono.just(account);
    }

    /**
     * @param AccountRequest account request
     * @return Credit Void of validations
     */
    public Mono<BalanceResponse> getBalanceByIdAccount(final String id) {
        return bankAccountRepository.findById(id)
                .switchIfEmpty(Mono.error(
                        new BankAccountNotFoundException(String.format("Bank account with id: %s not found", id))))
                .map(this::mapToResponseFromTransaction);
    }

    private BalanceResponse mapToResponseFromTransaction(final Account account) {
        BalanceResponse balanceResponse = new BalanceResponse();
        balanceResponse.setBalanceAccount(account.getBalance());
        balanceResponse.setNroAccount(account.getNroAccount());
        balanceResponse.setType(account.getType());
        balanceResponse.setProductId(account.getId());
        return balanceResponse;
    }

    /**
     * @param id Account id
     * @return Credit Void of validations
     */
    public Flux<TransactionResponse> getTransactionsByIdAccount(final String id) {
        return transactionRepository.findAllByProductId(id)
                .map(this::mapToResponseFromEntity);
    }

    private TransactionResponse mapToResponseFromEntity(final Transaction transaction) {
        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setAmount(transaction.getAmount());
        transactionResponse.setDate(transaction.getDate());
        transaction.setMotive(transaction.getMotive());
        transaction.setType(transaction.getType());
        return transactionResponse;
    }

    /**
     * @param transferRequest account request
     * @return Transfer response after transfer
     */
    public Mono<TransactionResponse> validateAndProcessTransfer(final TransferRequest transferRequest) {
        return bankAccountRepository.findById(transferRequest.getSourceAccountId())
                .switchIfEmpty(Mono.error(new RuntimeException("Source account not found")))
                .zipWith(bankAccountRepository.findById(transferRequest.getTargetAccountId())
                        .switchIfEmpty(Mono.error(new RuntimeException("Target account not found"))))
                .flatMap(tuple -> {
                    Account sourceAccount = tuple.getT1();
                    Account targetAccount = tuple.getT2();

                    if (sourceAccount.getBalance().compareTo(transferRequest.getTransactionRequest().getAmount()) < 0) {
                        return Mono.error(new RuntimeException("Insufficient balance in source account"));
                    }

                    return processTransfer(sourceAccount, targetAccount, transferRequest.getTransactionRequest());
                });
    }

    private Mono<TransactionResponse> processTransfer(
            final Account sourceAccount,
            final Account targetAccount,
            final TransactionRequest transactionRequest) {

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(transactionRequest.getAmount()));
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
                .then(Mono.just(buildTransferResponse(targetAccount, transactionRequest)));
    }

    /**
     * @param targetAccount target account
     * @return TransactionResponse mapper
     */
    public TransactionResponse buildTransferResponse(
            final Account targetAccount,
            final TransactionRequest transactionRequest) {

        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setAmount(transactionRequest.getAmount());
        transactionResponse.setMotive(buildMotiveFromTransfer(targetAccount));
        transactionResponse.setDate(LocalDate.now());
        transactionResponse.setType(TransactionType.TRANSFER);

        return transactionResponse;
    }

    private String buildMotiveFromTransfer(final Account targetAccount) {
        return String.format("Transfer made to account %s on %s", targetAccount.getNroAccount(),
                LocalDate.now().toString());
    }
}
