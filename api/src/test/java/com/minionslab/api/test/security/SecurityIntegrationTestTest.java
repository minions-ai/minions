package com.minionslab.api.test.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityIntegrationTest.class)
class SecurityIntegrationTestTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicEndpoint_ShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/api/public/test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/protected/test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void userEndpoint_WithUserRole_ShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/api/user/test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminEndpoint_WithUserRole_ShouldBeForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminEndpoint_WithAdminRole_ShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/api/admin/test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void cors_WithAllowedOrigin_ShouldSucceed() throws Exception {
        mockMvc.perform(options("/api/public/test")
                .header("Origin", "http://allowed-origin.com")
                .header("Access-Control-Request-Method", "GET")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://allowed-origin.com"));
    }

    @Test
    void cors_WithDisallowedOrigin_ShouldFail() throws Exception {
        mockMvc.perform(options("/api/public/test")
                .header("Origin", "http://disallowed-origin.com")
                .header("Access-Control-Request-Method", "GET")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void csrf_WithValidToken_ShouldSucceed() throws Exception {
        mockMvc.perform(post("/api/protected/test")
                .header("X-CSRF-TOKEN", "valid-token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void csrf_WithoutToken_ShouldFail() throws Exception {
        mockMvc.perform(post("/api/protected/test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void securityHeaders_ShouldBePresent() throws Exception {
        mockMvc.perform(get("/api/public/test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-XSS-Protection", "1; mode=block"))
                .andExpect(header().string("Strict-Transport-Security", "max-age=31536000; includeSubDomains"));
    }
} 