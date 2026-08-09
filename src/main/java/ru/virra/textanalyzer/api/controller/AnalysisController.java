package ru.virra.textanalyzer.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.virra.textanalyzer.api.dto.AnalysisRequest;
import ru.virra.textanalyzer.api.dto.AnalysisResponse;
import ru.virra.textanalyzer.persistence.repository.AnalysisRepository;
import ru.virra.textanalyzer.api.service.AnalysisService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping("/analyze")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AnalysisResponse startAnalysis(@Valid @RequestBody AnalysisRequest request, Authentication authentication) {
        return analysisService.create(request, authentication);
    }

    @GetMapping("/results")
    public List<AnalysisResponse> getAll() {
        return analysisService.findAll();
    }

    @GetMapping("/results/{id}")
    public AnalysisResponse findById(@PathVariable UUID id) {
        return analysisService.findById(id);
    }
}
