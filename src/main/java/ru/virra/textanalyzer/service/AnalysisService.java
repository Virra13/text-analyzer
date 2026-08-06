package ru.virra.textanalyzer.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import ru.virra.textanalyzer.dto.AnalysisRequest;
import ru.virra.textanalyzer.dto.AnalysisStartResponse;
import ru.virra.textanalyzer.exception.AnalysisNotFoundException;
import ru.virra.textanalyzer.persistence.entity.AnalysisEntity;
import ru.virra.textanalyzer.persistence.entity.AnalysisStatus;
import ru.virra.textanalyzer.persistence.repository.AnalysisRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalysisService {
    private final AnalysisRepository analysisRepository;
    private final AsyncAnalysisService asyncAnalysisService;

    public AnalysisStartResponse create(AnalysisRequest request, Authentication authentication) {
        AnalysisEntity analysis = new AnalysisEntity();

        analysis.setId(UUID.randomUUID());
        analysis.setStatus(AnalysisStatus.PENDING);
        analysis.setDirectory(request.directory());
        analysis.setMinWordLength(request.minWordLength());
        analysis.setTopCount(request.topCount());
        analysis.setMode(request.mode());
        analysis.setThreads(request.threads());
        analysis.setUsername(authentication.getName());
        analysis.setCreatedAt(LocalDateTime.now());

        AnalysisEntity saved = analysisRepository.save(analysis);
        asyncAnalysisService.analyze(saved.getId());

        return new AnalysisStartResponse(
                saved.getId(),
                saved.getStatus()
        );
    }

    @Transactional(readOnly = true)
    public AnalysisEntity findById(UUID id) {
        return analysisRepository.findById(id)
                .orElseThrow(() -> new AnalysisNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<AnalysisEntity> findAll() {
        return analysisRepository.findAll();
    }

}
