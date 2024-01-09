#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.net.ssl.SSLContext;

import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DbConnectorCheckupApplicationTests {

	@Autowired
	private TestRestTemplate httpRestTemplate;

	@LocalServerPort
	private int port;

	private RestTemplate httpsRestTemplate;
	private String baseUrl;
	private ObjectMapper objectMapper;

	@BeforeEach
	@SneakyThrows
	public void BeforeEach() {
		httpsRestTemplate = buildRestTemplateForHttpsRequest();
		baseUrl = "https://localhost:" + port;
		objectMapper = new ObjectMapper();
	}

	@Test
	void contextLoads() {
	}

	@Test
	public void ifARequestUsesHttp_shouldReturnBadRequest() {
		ResponseEntity<String> response = httpRestTemplate.getForEntity("http://localhost:" + port + "/products", String.class);

		assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
	}

	@Test
	@SneakyThrows
	public void ifARequestUsesHttps_shouldReturnOk() {
		ResponseEntity<String> response = httpsRestTemplate.exchange(baseUrl + "/products", HttpMethod.GET, null, String.class);

		assertEquals(response.getStatusCode().value(), 200);
	}

	@Test
	@SneakyThrows
	public void endToEndUserJourney() {
		httpsRestTemplate = buildRestTemplateForHttpsRequest();

		getProductsShouldReturn("""
                [
                    {
                        "id": 1,
                        "name": "product-1"
                    },
                    {
                        "id": 2,
                        "name": "product-2"
                    }
                ]
                """);

		URI prod3Uri = createProduct("product-3");

		getProductsShouldReturn("""
                [
                    {
                        "id": 1,
                        "name": "product-1"
                    },
                    {
                        "id": 2,
                        "name": "product-2"
                    },
                    {
                        "id": 3,
                        "name": "product-3"
                    }
                ]
                """);

		getProductByIdShouldReturn(prod3Uri, """
                {
                    "id": 3,
                    "name": "product-3"
                }
                """);

		URI prod4Uri = createProduct("product-4");

		getProductsShouldReturn("""
                [
                    {
                        "id": 1,
                        "name": "product-1"
                    },
                    {
                        "id": 2,
                        "name": "product-2"
                    },
                    {
                        "id": 3,
                        "name": "product-3"
                    },
                    {
                        "id": 4,
                        "name": "product-4"
                    }
                ]
                """);

		getProductByIdShouldReturn(prod4Uri, """
                {
                    "id": 4,
                    "name": "product-4"
                }
                """);

		deleteProduct(prod3Uri);

		getProductsShouldReturn("""
                [
                    {
                        "id": 1,
                        "name": "product-1"
                    },
                    {
                        "id": 2,
                        "name": "product-2"
                    },
                    {
                        "id": 4,
                        "name": "product-4"
                    }
                ]
                """);

		getProductByIdShouldReturnNotFound(prod3Uri);

		updateProduct(prod4Uri, "product-4-mod");

		getProductsShouldReturn("""
                [
                    {
                        "id": 1,
                        "name": "product-1"
                    },
                    {
                        "id": 2,
                        "name": "product-2"
                    },
                    {
                        "id": 4,
                        "name": "product-4-mod"
                    }
                ]
                """);

		getProductByIdShouldReturn(prod4Uri, """
                {
                    "id": 4,
                    "name": "product-4-mod"
                }
                """);
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

	@SneakyThrows
	private void getProductsShouldReturn(String expectedJson) {
		assertEquals(objectMapper.readTree(expectedJson), objectMapper.readTree(httpsRestTemplate.getForObject(baseUrl + "/products", String.class)));
	}

	@SneakyThrows
	private void getProductByIdShouldReturn(URI productUrl, String expectedJson) {
		assertEquals(objectMapper.readTree(expectedJson), objectMapper.readTree(httpsRestTemplate.getForObject(baseUrl + productUrl, String.class)));
	}

	private void getProductByIdShouldReturnNotFound(URI productUrl) {
		try {
			httpsRestTemplate.exchange(baseUrl + productUrl, HttpMethod.GET, null, String.class);
			fail("Not found expected");
		} catch (HttpClientErrorException e) {
			assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
		}
	}

	private URI createProduct(String productName) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return httpsRestTemplate.postForLocation(baseUrl + "/products", new HttpEntity<>("""
                {
                    "name": "%s"
                }
                """.formatted(productName), headers));
	}

	private void updateProduct(URI productUri, String newProductName) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		httpsRestTemplate.put(baseUrl + productUri, new HttpEntity<>("""
                {
                    "name": "%s"
                }
                """.formatted(newProductName), headers));
	}

	private void deleteProduct(URI productUrl) {
		httpsRestTemplate.delete(baseUrl + productUrl);
	}
}
