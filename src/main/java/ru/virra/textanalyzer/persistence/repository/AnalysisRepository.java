package ru.virra.textanalyzer.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.virra.textanalyzer.persistence.entity.AnalysisEntity;

import java.util.List;
import java.util.UUID;

public interface AnalysisRepository extends JpaRepository<AnalysisEntity, UUID> {

    List<AnalysisEntity> findAllByUserUsername(String username);
}