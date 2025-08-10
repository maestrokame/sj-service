package sj.service.sample.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import sj.service.sample.entity.MessageType;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    private final SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor userJwt =
            jwt().authorities(new SimpleGrantedAuthority("user"));

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
        MessageType mt = new MessageType();
        mt.setName("TestType");

        String mtJson = objectMapper.writeValueAsString(mt);

        // Crear
        String postResponse = mockMvc.perform(post("/api/message-types")
                        .with(userJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mtJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MessageType created = objectMapper.readValue(postResponse, MessageType.class);
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("TestType");

        // Obtener
        String getResponse = mockMvc.perform(get("/api/message-types/" + created.getId())
                        .with(userJwt))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MessageType fetched = objectMapper.readValue(getResponse, MessageType.class);
        assertThat(fetched).isNotNull();
        assertThat(fetched.getName()).isEqualTo("TestType");
    }

    @Test
    void testUpdateAndDeleteMessageType() throws Exception {
        MessageType mt = new MessageType();
        mt.setName("ToUpdate");

        String mtJson = objectMapper.writeValueAsString(mt);

        // Crear
        String postResponse = mockMvc.perform(post("/api/message-types")
                        .with(userJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mtJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MessageType created = objectMapper.readValue(postResponse, MessageType.class);
        assertThat(created).isNotNull();

        // Actualizar
        created.setName("UpdatedName");
        String updatedJson = objectMapper.writeValueAsString(created);

        String putResponse = mockMvc.perform(put("/api/message-types/" + created.getId())
                        .with(userJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MessageType updated = objectMapper.readValue(putResponse, MessageType.class);
        assertThat(updated.getName()).isEqualTo("UpdatedName");

        // Eliminar
        mockMvc.perform(delete("/api/message-types/" + created.getId())
                        .with(userJwt))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/message-types/" + created.getId())
                        .with(userJwt))
                .andExpect(status().isNotFound());
    }
}
