package com.okane.security;

import com.okane.config.TestSecurityConfig;
import com.okane.config.MvcConfig;
import com.okane.controller.SecurityTestController;
import com.okane.entity.User;
import com.okane.entity.enums.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { TestSecurityConfig.class, SecurityTestController.class })
class RoleAuthorizationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(Role role) {
        User user = User.builder()
                .email("test@okane.com")
                .password("password")
                .nom("Test")
                .prenom("User")
                .role(role)
                .active(true)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
                )
        );
    }

    // ── ADMIN ────────────────────────────────────────────────

    @Test
    void admin_canAccess_adminEndpoint() throws Exception {
        authenticateAs(Role.ADMIN);
        mockMvc.perform(get("/api/test/admin")).andExpect(status().isOk());
    }

    @Test
    void client_cannotAccess_adminEndpoint() throws Exception {
        authenticateAs(Role.CLIENT);
        mockMvc.perform(get("/api/test/admin")).andExpect(status().isForbidden());
    }

    @Test
    void agent_cannotAccess_adminEndpoint() throws Exception {
        authenticateAs(Role.AGENT);
        mockMvc.perform(get("/api/test/admin")).andExpect(status().isForbidden());
    }

    // ── MANAGER ──────────────────────────────────────────────

    @Test
    void manager_canAccess_managerEndpoint() throws Exception {
        authenticateAs(Role.MANAGER);
        mockMvc.perform(get("/api/test/manager")).andExpect(status().isOk());
    }

    @Test
    void client_cannotAccess_managerEndpoint() throws Exception {
        authenticateAs(Role.CLIENT);
        mockMvc.perform(get("/api/test/manager")).andExpect(status().isForbidden());
    }

    // ── AGENT ────────────────────────────────────────────────

    @Test
    void agent_canAccess_agentEndpoint() throws Exception {
        authenticateAs(Role.AGENT);
        mockMvc.perform(get("/api/test/agent")).andExpect(status().isOk());
    }

    @Test
    void client_cannotAccess_agentEndpoint() throws Exception {
        authenticateAs(Role.CLIENT);
        mockMvc.perform(get("/api/test/agent")).andExpect(status().isForbidden());
    }

    // ── CLIENT ───────────────────────────────────────────────

    @Test
    void client_canAccess_clientEndpoint() throws Exception {
        authenticateAs(Role.CLIENT);
        mockMvc.perform(get("/api/test/client")).andExpect(status().isOk());
    }

    @Test
    void admin_cannotAccess_clientEndpoint() throws Exception {
        authenticateAs(Role.ADMIN);
        mockMvc.perform(get("/api/test/client")).andExpect(status().isForbidden());
    }

    // ── ALL_STAFF ────────────────────────────────────────────

    @Test
    void admin_canAccess_staffEndpoint() throws Exception {
        authenticateAs(Role.ADMIN);
        mockMvc.perform(get("/api/test/staff")).andExpect(status().isOk());
    }

    @Test
    void manager_canAccess_staffEndpoint() throws Exception {
        authenticateAs(Role.MANAGER);
        mockMvc.perform(get("/api/test/staff")).andExpect(status().isOk());
    }

    @Test
    void agent_canAccess_staffEndpoint() throws Exception {
        authenticateAs(Role.AGENT);
        mockMvc.perform(get("/api/test/staff")).andExpect(status().isOk());
    }

    @Test
    void client_cannotAccess_staffEndpoint() throws Exception {
        authenticateAs(Role.CLIENT);
        mockMvc.perform(get("/api/test/staff")).andExpect(status().isForbidden());
    }

    // ── ALL ──────────────────────────────────────────────────

    @Test
    void admin_canAccess_allEndpoint() throws Exception {
        authenticateAs(Role.ADMIN);
        mockMvc.perform(get("/api/test/all")).andExpect(status().isOk());
    }

    @Test
    void client_canAccess_allEndpoint() throws Exception {
        authenticateAs(Role.CLIENT);
        mockMvc.perform(get("/api/test/all")).andExpect(status().isOk());
    }
}