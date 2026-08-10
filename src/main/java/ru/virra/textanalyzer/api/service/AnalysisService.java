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

/**
 * Сервис управления анализами через REST API.
 *
 * <p>Отвечает за создание новых анализов, сохранение параметров запуска
 * и получение ранее созданных анализов.</p>
 */
@Service
@RequiredArgsConstructor
public class AnalysisService {
    private final AnalysisRepository analysisRepository;
    private final AsyncAnalysisService asyncAnalysisService;
    private final UserRepository userRepository;
    private final AnalysisMapper analysisMapper;

    /**
     * Создаёт новый анализ и запускает его асинхронное выполнение.
     *
     * <p>Параметры запроса и данные авторизованного пользователя
     * сохраняются в базе данных. Новый анализ создаётся
     * со статусом {@link AnalysisStatus#PENDING}.</p>
     *
     * @param request параметры анализа
     * @param authentication данные авторизованного пользователя
     * @return созданный анализ с текущим статусом
     */
    public AnalysisResponse create(AnalysisRequest request, Authentication authentication) {

        AnalysisEntity analysis = new AnalysisEntity();

        analysis.setId(UUID.randomUUID());
        analysis.setStatus(AnalysisStatus.PENDING);
        analysis.setDirectory(request.directory());
        analysis.setMinWordLength(request.minWordLength());
        analysis.setTopCount(request.topCount());
        analysis.setMode(request.mode());
        analysis.setThreads(request.resolvedThreads());
        analysis.setStopWords(request.stopWords());

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

    /**
     * Возвращает анализ по его идентификатору.
     *
     * @param id идентификатор анализа
     * @return данные и текущий статус анализа
     * @throws AnalysisNotFoundException если анализ не найден
     */
    @Transactional(readOnly = true)
    public AnalysisResponse findById(UUID id) {
        AnalysisEntity analysis = analysisRepository.findById(id)
                .orElseThrow(() -> new AnalysisNotFoundException(id));

        return analysisMapper.toResponse(analysis);
    }

    /**
     * Возвращает список всех сохранённых анализов.
     *
     * @return список анализов
     */
    @Transactional(readOnly = true)
    public List<AnalysisResponse> findAll() {
        return analysisRepository.findAll().stream()
                .map(analysisMapper::toResponse)
                .toList();
    }
}