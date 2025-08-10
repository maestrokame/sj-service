package sj.service.sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.nio.charset.StandardCharsets;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

public class MockMvcHelper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static RequestPostProcessor token(String authority) {
        return jwt().authorities(new SimpleGrantedAuthority(authority));
    }

    public static RequestPostProcessor json(Object body) throws Exception {
        byte[] jsonString = OBJECT_MAPPER.writeValueAsBytes(body);
        return request -> {
            request.setContentType(MediaType.APPLICATION_JSON_VALUE);
            request.setCharacterEncoding(StandardCharsets.UTF_8.name());
            request.setContent(jsonString);
            return request;
        };
    }

}
