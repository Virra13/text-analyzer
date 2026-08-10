package ru.virra.textanalyzer.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import ru.virra.textanalyzer.analysis.application.ExecutionMode;
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
    private UserEntity user;
    private UserEntity otherUser;

    @BeforeEach
    void setUp() {
        analysisService = new AnalysisService(
                analysisRepository,
                asyncAnalysisService,
                userRepository,
                analysisMapper
        );

        user = new UserEntity();
        user.setUsername("User1");

        otherUser = new UserEntity();
        otherUser.setUsername("User2");
    }

    @Test
    void shouldReturnOwnAnalysisById() {
        AnalysisEntity analysis = new AnalysisEntity();
        analysis.setUser(user);

        AnalysisResponse expectedResponse = new AnalysisResponse(ANALYSIS_ID, AnalysisStatus.COMPLETED,null);

        when(authentication.getName()).thenReturn("User1");
        when(authentication.getAuthorities()).thenReturn(List.of());
        when(analysisRepository.findById(ANALYSIS_ID)).thenReturn(Optional.of(analysis));
        when(analysisMapper.toResponse(analysis)).thenReturn(expectedResponse);

        AnalysisResponse actualResponse = analysisService.findById(ANALYSIS_ID, authentication);

        assertSame(expectedResponse, actualResponse);

        verify(analysisRepository).findById(ANALYSIS_ID);
        verify(analysisMapper).toResponse(analysis);
    }

    @Test
    void shouldThrowExceptionWhenUserRequestsAnotherUsersAnalysis() {
        AnalysisEntity analysis = new AnalysisEntity();
        analysis.setUser(otherUser);

        when(authentication.getName()).thenReturn("User1");
        when(authentication.getAuthorities()).thenReturn(List.of());
        when(analysisRepository.findById(ANALYSIS_ID)).thenReturn(Optional.of(analysis));

        assertThrows(
                AnalysisNotFoundException.class,
                () -> analysisService.findById(ANALYSIS_ID, authentication)
        );

        verify(analysisRepository).findById(ANALYSIS_ID);
        verifyNoInteractions(analysisMapper);
    }

    @Test
    void shouldReturnAnyAnalysisByIdForAdmin() {
        AnalysisEntity analysis = new AnalysisEntity();
        analysis.setUser(otherUser);

        AnalysisResponse expectedResponse = new AnalysisResponse(ANALYSIS_ID, AnalysisStatus.COMPLETED, null);

        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication).getAuthorities();

        when(analysisRepository.findById(ANALYSIS_ID)).thenReturn(Optional.of(analysis));

        when(analysisMapper.toResponse(analysis)).thenReturn(expectedResponse);

        AnalysisResponse actualResponse = analysisService.findById(ANALYSIS_ID, authentication);

        assertSame(expectedResponse, actualResponse);

        verify(analysisRepository).findById(ANALYSIS_ID);
        verify(analysisMapper).toResponse(analysis);
    }

    @Test
    void shouldThrowExceptionWhenAnalysisNotFound() {
        when(analysisRepository.findById(ANALYSIS_ID)).thenReturn(Optional.empty());

        assertThrows(
                AnalysisNotFoundException.class,
                () -> analysisService.findById(ANALYSIS_ID, authentication)
        );

        verify(analysisRepository).findById(ANALYSIS_ID);
        verifyNoInteractions(analysisMapper);
    }

    @Test
    void shouldReturnAllAnalysisForAdmin() {

        AnalysisEntity analysis = new AnalysisEntity();
        AnalysisEntity analysis2 = new AnalysisEntity();

        AnalysisResponse expectedResponse = new AnalysisResponse(ANALYSIS_ID, AnalysisStatus.COMPLETED,null);
        AnalysisResponse expectedResponse2 = new AnalysisResponse(ANALYSIS_ID_2, AnalysisStatus.COMPLETED,null);

        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication).getAuthorities();

        when(analysisRepository.findAll()).thenReturn(List.of(analysis, analysis2));
        when(analysisMapper.toResponse(analysis)).thenReturn(expectedResponse);
        when(analysisMapper.toResponse(analysis2)).thenReturn(expectedResponse2);

        List<AnalysisResponse> actualResponse = analysisService.findAll(authentication);

        assertEquals(List.of(expectedResponse, expectedResponse2), actualResponse);

        verify(analysisRepository).findAll();
        verify(analysisMapper).toResponse(analysis);
        verify(analysisMapper).toResponse(analysis2);
        verify(analysisRepository, never()).findAllByUserUsername(anyString());
    }

    @Test
    void shouldReturnOnlyOwnAnalysisForUser() {
        AnalysisEntity analysis = new AnalysisEntity();

        AnalysisResponse expectedResponse = new AnalysisResponse(ANALYSIS_ID, AnalysisStatus.COMPLETED,null);

        when(authentication.getName()).thenReturn("User1");
        when(authentication.getAuthorities()).thenReturn(List.of());

        when(analysisRepository.findAllByUserUsername("User1")).thenReturn(List.of(analysis));
        when(analysisMapper.toResponse(analysis)).thenReturn(expectedResponse);

        List<AnalysisResponse> actualResponse = analysisService.findAll(authentication);

        assertEquals(List.of(expectedResponse), actualResponse);

        verify(analysisRepository).findAllByUserUsername("User1");
        verify(analysisRepository, never()).findAll();
        verify(analysisMapper).toResponse(analysis);
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoAnalysis() {
        when(authentication.getName()).thenReturn("User1");
        when(authentication.getAuthorities()).thenReturn(List.of());

        when(analysisRepository.findAllByUserUsername("User1")).thenReturn(List.of());

        List<AnalysisResponse> actualResponse = analysisService.findAll(authentication);

        assertTrue(actualResponse.isEmpty());

        verify(analysisRepository).findAllByUserUsername("User1");
        verify(analysisRepository, never()).findAll();
        verifyNoInteractions(analysisMapper);
    }

    @Test
    void shouldCreateEntity() {

        AnalysisRequest request = new AnalysisRequest("texts", 3, 10,
                ExecutionMode.MULTI, 4, "config/stopwords.txt");

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
                () -> assertEquals(ExecutionMode.MULTI, savedAnalysis.getMode()),
                () -> assertEquals(4, savedAnalysis.getThreads()),
                () -> assertEquals(AnalysisStatus.PENDING, savedAnalysis.getStatus()),
                () -> assertSame(user, savedAnalysis.getUser()),
                () -> assertNotNull(savedAnalysis.getId()),
                () -> assertEquals("config/stopwords.txt", savedAnalysis.getStopWords()),
                () -> assertNotNull(savedAnalysis.getCreatedAt()));


        verify(authentication).getName();
        verify(userRepository).findByUsername("User");
        verify(asyncAnalysisService).analyze(response.id());

    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {

        AnalysisRequest request = new AnalysisRequest("texts", 3, 10,
                ExecutionMode.MULTI, 4, null);

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