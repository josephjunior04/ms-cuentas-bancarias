package ms_cuentas_bancarias.ms_cuentas_bancarias.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    /**
     * @return Instance web client to use
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}
