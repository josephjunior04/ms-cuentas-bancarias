package ms_cuentas_bancarias.ms_cuentas_bancarias;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MsCuentasBancariasApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsCuentasBancariasApplication.class, args);
	}

}
