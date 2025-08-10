package sj.service.sample.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import sj.service.sample.entity.MessageType;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static sj.service.sample.MockMvcHelper.json;
import static sj.service.sample.MockMvcHelper.token;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ContextConfiguration(initializers = MessageTypeControllerIT.Initializer.class)
public class MessageTypeControllerIT {

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.2")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass")
            .withInitScript("init.sql");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    void testCreateAndGetMessageType() throws Exception {
        MessageType messageType = MessageType.builder()
                .name("TestType")
                .build();

        // Crear
        String postResponse = mockMvc.perform(post("/api/message-types")
                        .with(token("user"))
                        .with(json(messageType)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MessageType created = objectMapper.readValue(postResponse, MessageType.class);
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("TestType");

        // Obtener
        String getResponse = mockMvc.perform(get("/api/message-types/" + created.getId())
                        .with(token("user")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MessageType fetched = objectMapper.readValue(getResponse, MessageType.class);
        assertThat(fetched).isNotNull();
        assertThat(fetched.getName()).isEqualTo("TestType");
    }

    @Test
    void testUpdateAndDeleteMessageType() throws Exception {
        MessageType messageType = MessageType.builder()
                .name("ToUpdate")
                .build();

        // Crear
        String postResponse = mockMvc.perform(post("/api/message-types")
                        .with(token("user"))
                        .with(json(messageType)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MessageType created = objectMapper.readValue(postResponse, MessageType.class);
        assertThat(created).isNotNull();

        // Actualizar
        created.setName("UpdatedName");

        String putResponse = mockMvc.perform(put("/api/message-types/" + created.getId())
                        .with(token("user"))
                        .with(json(created)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MessageType updated = objectMapper.readValue(putResponse, MessageType.class);
        assertThat(updated.getName()).isEqualTo("UpdatedName");

        // Eliminar
        mockMvc.perform(delete("/api/message-types/" + created.getId())
                        .with(token("user")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/message-types/" + created.getId())
                        .with(token("user")))
                .andExpect(status().isNotFound());
    }
}
