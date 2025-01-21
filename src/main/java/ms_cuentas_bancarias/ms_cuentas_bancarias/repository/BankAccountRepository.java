package ms_cuentas_bancarias.ms_cuentas_bancarias.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.ms_cuentas_bancarias.model.Account;

import reactor.core.publisher.Flux;

public interface BankAccountRepository extends ReactiveMongoRepository<Account, String> {
    Flux<Account> findByTypeAndClientId(String type, String clientId);
    Flux<Account> findByClientId(String clientId);
}
