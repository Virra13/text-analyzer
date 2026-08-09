package ru.virra.textanalyzer.api.controller;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ru.virra.textanalyzer.api.dto.AnalysisRequest;
import ru.virra.textanalyzer.api.dto.AnalysisResponse;
import ru.virra.textanalyzer.api.service.AnalysisService;
import ru.virra.textanalyzer.persistence.entity.AnalysisStatus;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnalysisController.class)
@AutoConfigureMockMvc(addFilters = false)
class AnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalysisService analysisService;

    private static final UUID ANALYSIS_ID = UUID.fromString("f5d01536-3394-446a-a845-83b97ff83045");
    private static final UUID ANALYSIS_ID_2 = UUID.fromString("f5d01536-3394-446a-a845-83b97ff83046");

    @Test
    void shouldStartAnalysis() throws Exception {

        AnalysisResponse response = new AnalysisResponse(ANALYSIS_ID, AnalysisStatus.PENDING,null);
        when(analysisService.create(any(AnalysisRequest.class),nullable(Authentication.class))).thenReturn(response);

        mockMvc.perform(post("/api/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "directory": "texts",
                              "minWordLength": 3,
                              "topCount": 10,
                              "mode": "multi",
                              "threads": 4
                            }
                            """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(ANALYSIS_ID.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.result").doesNotExist());

        verify(analysisService).create(any(AnalysisRequest.class), nullable(Authentication.class));
    }

    @Test
    void shouldReturnBadRequestForInvalidRequest() throws Exception {

        mockMvc.perform(post("/api/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "directory": "",
                              "minWordLength": 3,
                              "topCount": 0,
                              "mode": "multi",
                              "threads": 4
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(analysisService);
    }

    @Test
    void shouldReturnAllAnalysis() throws Exception {

        AnalysisResponse response1 = new AnalysisResponse(ANALYSIS_ID, AnalysisStatus.COMPLETED,null);
        AnalysisResponse response2 = new AnalysisResponse(ANALYSIS_ID_2, AnalysisStatus.PENDING,null);
        when(analysisService.findAll()).thenReturn(List.of(response1, response2));

        mockMvc.perform(get("/api/results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(ANALYSIS_ID.toString()))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$[1].id").value(ANALYSIS_ID_2.toString()))
                .andExpect(jsonPath("$[1].status").value("PENDING"));

        verify(analysisService).findAll();
    }

    @Test
    void shouldReturnAnalysisById() throws Exception {
        AnalysisResponse response = new AnalysisResponse(ANALYSIS_ID, AnalysisStatus.COMPLETED,null);
        when(analysisService.findById(ANALYSIS_ID)).thenReturn(response);

        mockMvc.perform(get("/api/results/{id}", ANALYSIS_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ANALYSIS_ID.toString()))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.result").doesNotExist());

        verify(analysisService).findById(ANALYSIS_ID);
    }
}