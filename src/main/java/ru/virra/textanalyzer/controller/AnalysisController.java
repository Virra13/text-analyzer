package ru.virra.textanalyzer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.virra.textanalyzer.dto.AnalysisRequest;
import ru.virra.textanalyzer.dto.AnalysisStartResponse;
import ru.virra.textanalyzer.persistence.entity.AnalysisEntity;
import ru.virra.textanalyzer.persistence.entity.AnalysisStatus;
import ru.virra.textanalyzer.persistence.repository.AnalysisRepository;
import ru.virra.textanalyzer.service.AnalysisService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisRepository analysisRepository;
    private final AnalysisService analysisService;

    @PostMapping("/analyze")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AnalysisStartResponse startAnalysis(@Valid @RequestBody AnalysisRequest request, Authentication authentication) {
        return analysisService.create(request, authentication);
    }

    @GetMapping("/results")
    public List<AnalysisEntity> getAll() {
        return analysisService.findAll();
    }

    @GetMapping("/results/{id}")
    public AnalysisEntity findById(@PathVariable UUID id) {
        return analysisService.findById(id);
    }
}
