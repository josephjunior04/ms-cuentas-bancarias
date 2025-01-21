package ms_cuentas_bancarias.ms_cuentas_bancarias.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.ms_cuentas_bancarias.model.Transaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionRepository extends ReactiveMongoRepository<Transaction, String> {
    Flux<Transaction> findAllByProductId(String productId);

    Mono<Integer> countByProductIdAndDateBetween(String productId, LocalDateTime startDate, LocalDateTime endDate);

    Flux<Transaction> findByProductIdAndDateBetween(String productId, LocalDate startDate, LocalDate endDate);

}
