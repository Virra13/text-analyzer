package ru.virra.textanalyzer.api.controller;

import ru.virra.textanalyzer.api.service.AnalysisService;
import ru.virra.textanalyzer.security.SecurityConfig;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(AnalysisController.class)
@ActiveProfiles("rest")
@Import(SecurityConfig.class)
class AnalysisControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalysisService analysisService;

    @Test
    void shouldReturnUnauthorizedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/results"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(analysisService);
    }

    @Test
    @WithMockUser(username = "User1", roles = "USER")
    void shouldAllowAuthenticatedUser() throws Exception {
        when(analysisService.findAll(any(Authentication.class))).thenReturn(List.of());

        mockMvc.perform(get("/api/results"))
                .andExpect(status().isOk());

        verify(analysisService).findAll(any(Authentication.class));
    }
}
