package br.com.analizza.analizzatube.support;

import br.com.analizza.analizzatube.AnalizzaTubeApiApplication;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Base de todo teste de integracao: toda classe terminada em IT a estende.
 *
 * <p>Um container de banco para a suite inteira, iniciado na mao e sem
 * {@code @Container}: o ciclo do {@code @Container} e por classe e reiniciaria
 * o banco a cada IT. O Ryuk do Testcontainers derruba o container no fim do build.
 */
@SpringBootTest(
        classes = AnalizzaTubeApiApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import(TestConfig.class)
public abstract class BaseIntegrationTest {

    @ServiceConnection
    protected static final PostgreSQLContainer DATABASE = new PostgreSQLContainer("postgres:17-alpine");

    static {
        DATABASE.start();
    }

    @DynamicPropertySource
    static void segredoDoWebhook(DynamicPropertyRegistry registry) {
        registry.add("yeroon.webhook-secret", () -> "whsec_teste");
    }

    @LocalServerPort
    private int port;

    protected RestTestClient http;

    @Autowired
    protected FakeYeroonClient yeroon;

    @BeforeEach
    void resetarDubles() {
        yeroon.reset();
    }

    @BeforeEach
    void prepararCliente() {
        // O JdkClientHttpRequestFactory vai explicito porque bindToServer() sem
        // argumento escolhe o cliente HTTP pelo classpath: uma dependencia nova
        // pode trocar o transporte do teste -- e o retry dele -- sem aviso.
        http = RestTestClient.bindToServer(new JdkClientHttpRequestFactory())
                .baseUrl("http://localhost:" + port)
                .build();
    }
}
