package ru.virra.textanalyzer.api.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import ru.virra.textanalyzer.api.dto.AnalysisRequest;
import ru.virra.textanalyzer.api.dto.AnalysisResponse;
import ru.virra.textanalyzer.exception.AnalysisNotFoundException;
import ru.virra.textanalyzer.api.mapper.AnalysisMapper;
import ru.virra.textanalyzer.persistence.entity.AnalysisEntity;
import ru.virra.textanalyzer.persistence.entity.AnalysisStatus;
import ru.virra.textanalyzer.persistence.entity.UserEntity;
import ru.virra.textanalyzer.persistence.repository.AnalysisRepository;
import ru.virra.textanalyzer.persistence.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalysisService {
    private final AnalysisRepository analysisRepository;
    private final AsyncAnalysisService asyncAnalysisService;
    private final UserRepository userRepository;
    private final AnalysisMapper analysisMapper;

    public AnalysisResponse create(AnalysisRequest request, Authentication authentication) {

        AnalysisEntity analysis = new AnalysisEntity();

        analysis.setId(UUID.randomUUID());
        analysis.setStatus(AnalysisStatus.PENDING);
        analysis.setDirectory(request.directory());
        analysis.setMinWordLength(request.minWordLength());
        analysis.setTopCount(request.topCount());
        analysis.setMode(request.mode());
        analysis.setThreads(request.threads());

        UserEntity user = userRepository
                .findByUsername(authentication.getName())
                .orElseThrow();

        analysis.setUser(user);
        analysis.setCreatedAt(LocalDateTime.now());

        AnalysisEntity saved = analysisRepository.save(analysis);
        asyncAnalysisService.analyze(saved.getId());

        return new AnalysisResponse(
                saved.getId(),
                saved.getStatus(),
                null
        );
    }

    @Transactional(readOnly = true)
    public AnalysisResponse findById(UUID id) {
        AnalysisEntity analysis = analysisRepository.findById(id)
                .orElseThrow(() -> new AnalysisNotFoundException(id));

        return analysisMapper.toResponse(analysis);
    }

    @Transactional(readOnly = true)
    public List<AnalysisResponse> findAll() {
        return analysisRepository.findAll().stream()
                .map(analysisMapper::toResponse)
                .toList();
    }
}