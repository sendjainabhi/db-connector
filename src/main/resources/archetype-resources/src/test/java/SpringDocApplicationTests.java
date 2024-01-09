#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustSelfSignedStrategy;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import lombok.SneakyThrows;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
class SpringDocApplicationTests {

	private String baseUrl;
	
	private RestTemplate httpsRestTemplate;

	@BeforeEach
	@SneakyThrows
	public void BeforeEach() {
		httpsRestTemplate = buildRestTemplateForHttpsRequest();
		baseUrl = "https://localhost:8443";
	}

	@Test
	@SneakyThrows
	public void ifARequestActuatorUpHttps_shouldReturnOk() {
		ResponseEntity<String> response = httpsRestTemplate.exchange(baseUrl + "/swagger-ui/index.html", HttpMethod.GET, null, String.class);

		assertEquals(response.getStatusCode().value(), 200);
		
	}
	
	private static RestTemplate buildRestTemplateForHttpsRequest() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
		SSLContext sslContext = new SSLContextBuilder()
				.loadTrustMaterial(null,
						TrustSelfSignedStrategy.INSTANCE
				).build();
		SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
		final Registry<ConnectionSocketFactory> socketFactoryRegistry =
				RegistryBuilder.<ConnectionSocketFactory>create()
						.register("https", socketFactory)
						.register("http", new PlainConnectionSocketFactory())
						.build();
		final BasicHttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager(socketFactoryRegistry);
		final CloseableHttpClient httpClient = HttpClients.custom()
				.setConnectionManager(connectionManager)
				.build();
		final HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
		return new RestTemplate(requestFactory);
	}
		
}
