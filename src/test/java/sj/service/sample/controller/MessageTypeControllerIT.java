package sj.service.sample.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import sj.service.sample.entity.MessageType;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = MessageTypeControllerIT.Initializer.class)
public class MessageTypeControllerIT {

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.2")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass")
            .withInitScript("init.sql"); // Agrega el script de inicialización

    @Autowired
    private TestRestTemplate restTemplate;

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext context) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgres.getJdbcUrl(),
                    "spring.datasource.username=" + postgres.getUsername(),
                    "spring.datasource.password=" + postgres.getPassword()
            ).applyTo(context.getEnvironment());
        }
    }

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @Test
    void testCreateAndGetMessageType() {
        MessageType mt = new MessageType();
        mt.setName("TestType");

        ResponseEntity<MessageType> postResponse = restTemplate.postForEntity("/api/message-types", mt, MessageType.class);
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        MessageType created = postResponse.getBody();
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("TestType");

        ResponseEntity<MessageType> getResponse = restTemplate.getForEntity("/api/message-types/" + created.getId(), MessageType.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        MessageType fetched = getResponse.getBody();
        assertThat(fetched).isNotNull();
        assertThat(fetched.getName()).isEqualTo("TestType");
    }

    @Test
    void testUpdateAndDeleteMessageType() {
        MessageType mt = new MessageType();
        mt.setName("ToUpdate");

        MessageType created = restTemplate.postForEntity("/api/message-types", mt, MessageType.class).getBody();
        assertThat(created).isNotNull();

        created.setName("UpdatedName");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MessageType> entity = new HttpEntity<>(created, headers);

        ResponseEntity<MessageType> putResponse = restTemplate.exchange(
                "/api/message-types/" + created.getId(),
                HttpMethod.PUT,
                entity,
                MessageType.class
        );
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(putResponse.getBody().getName()).isEqualTo("UpdatedName");

        restTemplate.delete("/api/message-types/" + created.getId());
        ResponseEntity<MessageType> getResponse = restTemplate.getForEntity("/api/message-types/" + created.getId(), MessageType.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
