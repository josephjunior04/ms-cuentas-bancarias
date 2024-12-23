package ms_cuentas_bancarias.ms_cuentas_bancarias.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.ms_cuentas_bancarias.model.Account;

public interface BankAccountRepository extends ReactiveMongoRepository<Account, String>{

}
