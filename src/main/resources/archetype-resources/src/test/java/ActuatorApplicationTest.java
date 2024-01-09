#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import lombok.SneakyThrows;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
class ActuatorCheckupApplicationTests {

	@Autowired
	private TestRestTemplate httpRestTemplate;

	private String baseUrl;

	@BeforeEach
	@SneakyThrows
	public void BeforeEach() {
		baseUrl = "http://localhost:8080";
	}

	@Test
	@SneakyThrows
	public void ifARequestActuatorUpHttps_shouldReturnOk() {
		ResponseEntity<String> response = httpRestTemplate.exchange(baseUrl + "/actuator/health/liveness", HttpMethod.GET, null, String.class);

		assertEquals(response.getStatusCode().value(), 200);
		
	}
		
}
