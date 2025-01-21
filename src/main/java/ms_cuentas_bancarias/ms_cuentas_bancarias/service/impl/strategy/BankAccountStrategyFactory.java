package ms_cuentas_bancarias.ms_cuentas_bancarias.service.impl.strategy;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ms_cuentas_bancarias.model.BankAccountType;

@Service
public class BankAccountStrategyFactory {
    private final Map<Class<? extends BankAccountStrategy>, BankAccountType> strategyTypeMap = Map.of(
            BankAccountSavingStrategy.class, BankAccountType.SAVING,
            BankAccountCurrentStrategy.class, BankAccountType.CURRENT,
            BankAccountFixedTermStrategy.class, BankAccountType.FIXED_TERM);

    private final Map<BankAccountType, BankAccountStrategy> strategies;

    public BankAccountStrategyFactory(final List<BankAccountStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        strategy -> determineCreditType(strategy),
                        strategy -> strategy));
    }

    private BankAccountType determineCreditType(final BankAccountStrategy strategy) {
        return Optional.ofNullable(strategyTypeMap.get(strategy.getClass()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown strategy type: " + strategy.getClass().getSimpleName()));
    }

    /**
     * @param creditType CreditType enum
     * @return CreditValidationStrategy strategy to use
     */
    public BankAccountStrategy getStrategy(final BankAccountType creditType) {
        return Optional.ofNullable(strategies.get(creditType))
                .orElseThrow(() -> new UnsupportedOperationException("Unsupported credit type"));
    }
}