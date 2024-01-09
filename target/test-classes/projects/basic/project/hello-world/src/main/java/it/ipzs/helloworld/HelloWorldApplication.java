package it.ipzs.helloworld;

import it.ipzs.dapcommons.http.exceptions.RestExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@SpringBootApplication
public class HelloWorldApplication {

	public static void main(String[] args) {
		SpringApplication.run(HelloWorldApplication.class, args);
	}

	@Bean
	public ResponseEntityExceptionHandler exceptionHandler() {
		return new RestExceptionHandler();
	}
}
