package ms_cuentas_bancarias.ms_cuentas_bancarias.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.ms_cuentas_bancarias.model.Transaction;

import reactor.core.publisher.Flux;

public interface TransactionRepository extends ReactiveMongoRepository<Transaction, String> {
    Flux<Transaction> findAllByProductId(String productId);
}
