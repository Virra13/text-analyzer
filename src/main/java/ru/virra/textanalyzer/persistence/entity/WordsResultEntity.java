package ru.virra.textanalyzer.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "words")
public class WordsResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String word;

    @Column(nullable = false)
    private Integer wordCount;

    @ManyToOne
    @JoinColumn(name = "analysis_id", nullable = false)
    private AnalysisEntity analysis;
}
