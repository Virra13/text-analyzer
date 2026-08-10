package ru.virra.textanalyzer.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.virra.textanalyzer.api.dto.AnalysisRequest;
import ru.virra.textanalyzer.api.dto.AnalysisResponse;
import ru.virra.textanalyzer.persistence.repository.AnalysisRepository;
import ru.virra.textanalyzer.api.service.AnalysisService;

import java.util.List;
import java.util.UUID;

/**
 * REST-контроллер для запуска анализа текстов и получения результатов.
 */
@Profile("rest")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    /**
     * Запускает новый анализ текстовых файлов.
     *
     * @param request параметры запуска анализа
     * @param authentication данные авторизованного пользователя
     * @return информация о созданном анализе и его текущем статусе
     */
    @PostMapping("/analyze")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AnalysisResponse startAnalysis(@Valid @RequestBody AnalysisRequest request, Authentication authentication) {
        return analysisService.create(request, authentication);
    }

    /**
     * Возвращает список всех сохранённых анализов.
     *
     * @return список анализов
     */
    @GetMapping("/results")
    public List<AnalysisResponse> getAll(Authentication authentication) {
        return analysisService.findAll(authentication);
    }

    /**
     * Возвращает анализ по его уникальному идентификатору.
     *
     * @param id идентификатор анализа
     * @return данные анализа и его текущий статус
     */
    @GetMapping("/results/{id}")
    public AnalysisResponse findById(@PathVariable UUID id, Authentication authentication) {
        return analysisService.findById(id, authentication);
    }
}
