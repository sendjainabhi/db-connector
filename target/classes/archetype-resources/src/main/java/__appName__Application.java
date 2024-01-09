#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import it.ipzs.dapcommons.http.exceptions.RestExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@SpringBootApplication
public class ${appName}Application {

	public static void main(String[] args) {
		SpringApplication.run(${appName}Application.class, args);
	}

	@Bean
	public ResponseEntityExceptionHandler exceptionHandler() {
		return new RestExceptionHandler();
	}
}
