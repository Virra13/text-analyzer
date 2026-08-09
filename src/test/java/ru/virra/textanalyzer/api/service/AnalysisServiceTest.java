package ru.virra.textanalyzer.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import ru.virra.textanalyzer.api.dto.AnalysisRequest;
import ru.virra.textanalyzer.api.dto.AnalysisResponse;
import ru.virra.textanalyzer.api.mapper.AnalysisMapper;
import ru.virra.textanalyzer.exception.AnalysisNotFoundException;
import ru.virra.textanalyzer.persistence.entity.AnalysisEntity;
import ru.virra.textanalyzer.persistence.entity.AnalysisStatus;
import ru.virra.textanalyzer.persistence.entity.UserEntity;
import ru.virra.textanalyzer.persistence.repository.AnalysisRepository;
import ru.virra.textanalyzer.persistence.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceTest {

    @Mock
    private AnalysisRepository analysisRepository;
    @Mock
    private AsyncAnalysisService asyncAnalysisService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AnalysisMapper analysisMapper;
    @Mock
    private Authentication authentication;

    private AnalysisService analysisService;
    private static final UUID ANALYSIS_ID = UUID.fromString("f5d01536-3394-446a-a845-83b97ff83045");
    private static final UUID ANALYSIS_ID_2 = UUID.fromString("f5d01536-3394-446a-a845-83b97ff83046");

    @BeforeEach
    void setUp() {
        analysisService = new AnalysisService(
                analysisRepository,
                asyncAnalysisService,
                userRepository,
                analysisMapper
        );
    }

    @Test
    void shouldReturnAnalysisById() {
        AnalysisEntity analysis = new AnalysisEntity();
        AnalysisResponse expectedResponse = new AnalysisResponse(
                ANALYSIS_ID,
                AnalysisStatus.COMPLETED,
                null);

        when(analysisRepository.findById(ANALYSIS_ID)).thenReturn(Optional.of(analysis));
        when(analysisMapper.toResponse(analysis)).thenReturn(expectedResponse);

        AnalysisResponse actualResponse = analysisService.findById(ANALYSIS_ID);

        assertSame(expectedResponse, actualResponse);

        verify(analysisRepository).findById(ANALYSIS_ID);
        verify(analysisMapper).toResponse(analysis);
    }

    @Test
    void shouldThrowExceptionWhenAnalysisNotFound() {
        when(analysisRepository.findById(ANALYSIS_ID)).thenReturn(Optional.empty());

        assertThrows(
                AnalysisNotFoundException.class,
                () -> analysisService.findById(ANALYSIS_ID)
        );

        verify(analysisRepository).findById(ANALYSIS_ID);
        verifyNoInteractions(analysisMapper);
    }

    @Test
    void shouldReturnAllAnalysis() {

        AnalysisEntity analysis = new AnalysisEntity();
        AnalysisEntity analysis2 = new AnalysisEntity();

        AnalysisResponse expectedResponse = new AnalysisResponse(
                ANALYSIS_ID,
                AnalysisStatus.COMPLETED,
                null);

        AnalysisResponse expectedResponse2 = new AnalysisResponse(
                ANALYSIS_ID_2,
                AnalysisStatus.COMPLETED,
                null);

        when(analysisRepository.findAll()).thenReturn(List.of(analysis, analysis2));
        when(analysisMapper.toResponse(analysis)).thenReturn(expectedResponse);
        when(analysisMapper.toResponse(analysis2)).thenReturn(expectedResponse2);

        List<AnalysisResponse> actualResponse = analysisService.findAll();

        assertEquals(List.of(expectedResponse, expectedResponse2), actualResponse);

        verify(analysisRepository).findAll();
        verify(analysisMapper).toResponse(analysis);
        verify(analysisMapper).toResponse(analysis2);
    }

    @Test
    void shouldReturnEmptyListWhenNoAnalysisFound() {

        when(analysisRepository.findAll()).thenReturn(List.of());

        List<AnalysisResponse> actualResponse = analysisService.findAll();

        assertTrue(actualResponse.isEmpty());

        verify(analysisRepository).findAll();
        verifyNoInteractions(analysisMapper);
    }

    @Test
    void shouldCreateEntity() {

        AnalysisRequest request = new AnalysisRequest("texts", 3, 10, "multi", 4);

        UserEntity user = new UserEntity();
        user.setUsername("User");

        when(authentication.getName()).thenReturn("User");
        when(userRepository.findByUsername("User")).thenReturn(Optional.of(user));

        when(analysisRepository.save(any(AnalysisEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AnalysisResponse response = analysisService.create(request, authentication);

        ArgumentCaptor<AnalysisEntity> captor = ArgumentCaptor.forClass(AnalysisEntity.class);
        verify(analysisRepository).save(captor.capture());

        AnalysisEntity savedAnalysis = captor.getValue();

        assertAll(
                () -> assertNotNull(response.id()),
                () -> assertEquals(AnalysisStatus.PENDING, response.status()),
                () -> assertNull(response.result()),
                () -> assertEquals(savedAnalysis.getId(), response.id()));

        assertAll(
                () -> assertEquals("texts", savedAnalysis.getDirectory()),
                () -> assertEquals(3, savedAnalysis.getMinWordLength()),
                () -> assertEquals(10, savedAnalysis.getTopCount()),
                () -> assertEquals("multi", savedAnalysis.getMode()),
                () -> assertEquals(4, savedAnalysis.getThreads()),
                () -> assertEquals(AnalysisStatus.PENDING, savedAnalysis.getStatus()),
                () -> assertSame(user, savedAnalysis.getUser()),
                () -> assertNotNull(savedAnalysis.getId()),
                () -> assertNotNull(savedAnalysis.getCreatedAt()));


        verify(authentication).getName();
        verify(userRepository).findByUsername("User");
        verify(asyncAnalysisService).analyze(response.id());

    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {

        AnalysisRequest request = new AnalysisRequest("texts", 3, 10, "multi", 4);

        when(authentication.getName()).thenReturn("User");
        when(userRepository.findByUsername("User")).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> analysisService.create(request, authentication)
        );

        verify(authentication).getName();
        verify(userRepository).findByUsername("User");

        verifyNoInteractions(analysisRepository);
        verifyNoInteractions(asyncAnalysisService);
    }
}