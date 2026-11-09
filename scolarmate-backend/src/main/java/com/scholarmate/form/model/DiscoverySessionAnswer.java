package com.scholarmate.form.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "discovery_session_answers")
public class DiscoverySessionAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private DiscoverySession session;

    @Column(name = "field_key", nullable = false)
    private String fieldKey;

    @Column(name = "answer_value", columnDefinition = "TEXT")
    private String answerValue;

    @Column(name = "answered_at")
    private OffsetDateTime answeredAt = OffsetDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public DiscoverySession getSession() { return session; }
    public void setSession(DiscoverySession session) { this.session = session; }

    public String getFieldKey() { return fieldKey; }
    public void setFieldKey(String fieldKey) { this.fieldKey = fieldKey; }

    public String getAnswerValue() { return answerValue; }
    public void setAnswerValue(String answerValue) { this.answerValue = answerValue; }

    public OffsetDateTime getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(OffsetDateTime answeredAt) { this.answeredAt = answeredAt; }
}
