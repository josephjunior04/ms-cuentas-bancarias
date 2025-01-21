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
import com.ms_cuentas_bancarias.model.FilterRequest;
import com.ms_cuentas_bancarias.model.SummaryAccountResponse;
import com.ms_cuentas_bancarias.model.TransactionRequest;
import com.ms_cuentas_bancarias.model.TransactionResponse;
import com.ms_cuentas_bancarias.model.TransferRequest;

import lombok.RequiredArgsConstructor;
import ms_cuentas_bancarias.ms_cuentas_bancarias.service.BankAccountService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class BankAccountController implements V1Api {

        private final BankAccountService bankAccountService;

        /**
         * @return Mono Response Entity with status
         * @param idAccount Request for insert client
         */
        @Override
        public Mono<ResponseEntity<Void>> delete(final String idAccount, final ServerWebExchange exchange) {
                return bankAccountService.deleteById(idAccount)
                                .then(Mono.just(ResponseEntity.noContent().build()));
        }

        /**
         * @return Mono Response Entity with status and Transaction Response
         * @param idAccount ID account to deposit
         */
        @Override
        public Mono<ResponseEntity<TransactionResponse>> deposit(final String idAccount,
                        final @Valid Mono<TransactionRequest> transactionRequest, final ServerWebExchange exchange) {
                return transactionRequest
                                .flatMap(request -> bankAccountService.deposit(idAccount, request)
                                                .map(bankAccountResponse -> {
                                                        return ResponseEntity
                                                                        .status(HttpStatus.CREATED)
                                                                        .body(bankAccountResponse);
                                                }));
        }

        /**
         * @return Response Entity of Flux Account with status
         */
        @Override
        public Mono<ResponseEntity<Flux<AccountResponse>>> findAllAccounts(final ServerWebExchange exchange) {
                return Mono.just(ResponseEntity.ok(bankAccountService.findAll()));
        }

        /**
         * @return Mono Response Entity of Account response with status
         * @param idAccount ID account to search
         */
        @Override
        public Mono<ResponseEntity<AccountResponse>> findById(final String idAccount,
                        final ServerWebExchange exchange) {
                return bankAccountService.findById(idAccount)
                                .map(accountResponse -> {
                                        return ResponseEntity
                                                        .status(HttpStatus.OK)
                                                        .body(accountResponse);
                                });
        }

        /**
         * @return Mono Response Entity of Account response to saved with status
         * @param accountRequest Account request to saved
         */
        @Override
        public Mono<ResponseEntity<AccountResponse>> insert(final @Valid Mono<AccountRequest> accountRequest,
                        final ServerWebExchange exchange) {
                return accountRequest
                                .flatMap(request -> bankAccountService.insert(request)
                                                .map(accountResponse -> {
                                                        return ResponseEntity
                                                                        .status(HttpStatus.CREATED)
                                                                        .body(accountResponse);
                                                }));
        }

        /**
         * @return Mono Response Entity of Transaction response to withdraw with status
         * @param idAccount          Id Account to withdraw
         * @param transactionRequest Transaction request to withdraw
         */
        @Override
        public Mono<ResponseEntity<TransactionResponse>> withdraw(final String idAccount,
                        final @Valid Mono<TransactionRequest> transactionRequest, final ServerWebExchange exchange) {
                return transactionRequest
                                .flatMap(request -> bankAccountService.withdraw(idAccount, request)
                                                .map(creditResponse -> {
                                                        return ResponseEntity
                                                                        .status(HttpStatus.CREATED)
                                                                        .body(creditResponse);
                                                }));
        }

        /**
         * @return Mono Response Entity of Account response to update with status
         * @param idAccount      Current Id Account to update
         * @param accountRequest Current account request to update
         */
        @Override
        public Mono<ResponseEntity<AccountResponse>> update(final String idAccount,
                        final @Valid Mono<AccountRequest> accountRequest,
                        final ServerWebExchange exchange) {
                return accountRequest
                                .flatMap(request -> bankAccountService.update(idAccount, request)
                                                .map(accountResponse -> {
                                                        return ResponseEntity
                                                                        .status(HttpStatus.CREATED)
                                                                        .body(accountResponse);
                                                }));
        }

        /**
         * @return Mono Response Entity of Account response to update with status
         * @param idAccount      Current Id Account to update
         * @param accountRequest Current account request to update
         */
        @Override
        public Mono<ResponseEntity<BalanceResponse>> getBalanceByAccount(
                        final String idAccount, final ServerWebExchange exchange) {
                return bankAccountService.getBalanceByAccount(idAccount)
                                .map(balanceResponse -> {
                                        return ResponseEntity
                                                        .status(HttpStatus.OK)
                                                        .body(balanceResponse);
                                });
        }

        /**
         * @return Mono Response Entity of Account response to update with status
         * @param idAccount      Current Id Account to update
         * @param accountRequest Current account request to update
         */
        @Override
        public Mono<ResponseEntity<Flux<TransactionResponse>>> getTransactionsByAccount(final String idAccount,
                        final ServerWebExchange exchange) {
                return Mono.just(ResponseEntity.ok(bankAccountService.getTransactionsByAccount(idAccount)));
        }

        /**
         * @return Mono Response Entity of TransactionResponse response of transfer
         * @param TransferRequest Transfer request to transfer
         */
        @Override
        public Mono<ResponseEntity<TransactionResponse>> bankTransfer(
                        final @Valid Mono<TransferRequest> transferRequest,
                        final ServerWebExchange exchange) {
                return transferRequest
                                .flatMap(request -> bankAccountService.transfer(request)
                                                .map(transactionResponse -> {
                                                        return ResponseEntity
                                                                        .status(HttpStatus.OK)
                                                                        .body(transactionResponse);
                                                }));
        }

        /**
         * @return Mono Response Entity of BalanceResponse of daily average balances
         * @param clientId Current client id
         */
        @Override
        public Mono<ResponseEntity<Flux<BalanceResponse>>> getDailyAverageBalances(final String clientId,
                        final ServerWebExchange exchange) {
                return Mono.just(ResponseEntity.ok(bankAccountService.getDailyAverageBalances(clientId)));
        }

        /**
         * @return Mono Response Entity of Flux Balances response of one client
         * @param clientId Current client id
         */
        @Override
        public Mono<ResponseEntity<Flux<BalanceResponse>>> getBalancesByClient(final String clientId,
                        final ServerWebExchange exchange) {
                return Mono.just(ResponseEntity.ok(bankAccountService.getBalancesByClient(clientId)));
        }

        /**
         * @return Mono Response Entity of SummaryAccountResponse of daily average
         *         balances
         * @param clientId      Current client id
         * @param filterRequest Mono Filter Request to summary
         */
        @Override
        public Mono<ResponseEntity<Flux<SummaryAccountResponse>>> getSummaryByClient(final String clientId,
                        @Valid final Mono<FilterRequest> filterRequest, final ServerWebExchange exchange) {
                return filterRequest.flatMap(request -> Mono
                                .just(ResponseEntity.ok(bankAccountService.getSummaryByClient(clientId, request))));
        }
}
