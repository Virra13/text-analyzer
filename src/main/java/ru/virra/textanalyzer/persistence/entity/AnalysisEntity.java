package ru.virra.textanalyzer.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static jakarta.persistence.CascadeType.ALL;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table (name = "analyses")
public class AnalysisEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnalysisStatus status;

    @Column(nullable = false)
    private String directory;

    @Column(nullable = false)
    private int minWordLength;

    @Column(nullable = false)
    private int topCount;

    @Column(nullable = false)
    private String mode;

    @Column(nullable = false)
    private int threads;

    private String stopWords;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Long executionTimeMs;
    private Integer processedFiles;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileErrorEntity> errors = new ArrayList<>();

    @OneToMany(mappedBy = "analysis", cascade = ALL, orphanRemoval = true)
    private List<WordResultEntity> words = new ArrayList<>();

    public void addWord(WordResultEntity word) {
        words.add(word);
        word.setAnalysis(this);
    }

    public void addError(FileErrorEntity error) {
        errors.add(error);
        error.setAnalysis(this);
    }

}
