package com.fraternityos.server.auth.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraternityos.server.auth.application.dto.LoginRequest;
import com.fraternityos.server.auth.application.dto.RegisterRequest;
import com.fraternityos.server.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * End-to-end tests for the authentication endpoints, exercising the full HTTP
 * stack (validation, security, controller, service, real database).
 */
class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_thenLogin_returnsBearerToken() throws Exception {
        String email = "login-ok@x.com";
        mockMvc.perform(register(new RegisterRequest("Alice", email, "password123")))
                .andExpect(status().isOk());

        mockMvc.perform(login(new LoginRequest(email, "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.houseId").doesNotExist());
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        String email = "wrong-pw@x.com";
        mockMvc.perform(register(new RegisterRequest("Bob", email, "password123")))
                .andExpect(status().isOk());

        mockMvc.perform(login(new LoginRequest(email, "not-the-password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_unknownEmail_returns401() throws Exception {
        mockMvc.perform(login(new LoginRequest("nobody@x.com", "password123")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        String email = "dupe@x.com";
        RegisterRequest request = new RegisterRequest("Carol", email, "password123");
        mockMvc.perform(register(request)).andExpect(status().isOk());

        mockMvc.perform(register(request)).andExpect(status().isConflict());
    }

    @Test
    void register_shortPassword_returns400() throws Exception {
        mockMvc.perform(register(new RegisterRequest("Dan", "short-pw@x.com", "short")))
                .andExpect(status().isBadRequest());
    }

    private MockHttpServletRequestBuilder register(RegisterRequest body) throws Exception {
        return post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
    }

    private MockHttpServletRequestBuilder login(LoginRequest body) throws Exception {
        return post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
    }
}
