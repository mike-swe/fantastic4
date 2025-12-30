package com.revature.fantastic4.entity;

import java.time.Instant;
import java.util.UUID;

import com.revature.fantastic4.enums.ChangeType;
import com.revature.fantastic4.enums.IssueFieldName;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="issue_history")
public class IssueHistory {
    
    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch=FetchType.LAZY, optional = false)
    @JoinColumn(name="issue_id", nullable=false)
    private Issue issue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="changed_by_user", nullable=false)
    private User changedByUser;

    @Column(name="changed_at", nullable= false)
    private Instant changedAt;

    @Enumerated(EnumType.STRING)
    @Column(name="field_name", length = 50)
    private IssueFieldName fieldName;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 20)
    private ChangeType changeType;
    

}
