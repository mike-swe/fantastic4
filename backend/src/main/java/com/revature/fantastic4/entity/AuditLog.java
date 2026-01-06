package com.revature.fantastic4.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLog 
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID actorUserId;

    private String action;

    private String entityType;

    private UUID entityId;

    private Instant timestamp;

    @Column(length = 1000)
    private String details;

    public AuditLog() 
    {
        this.timestamp = Instant.now();
    }

    public Long getId() 
    {
        return id;
    }

    public void setId(Long id) 
    {
        this.id = id;
    }

    public UUID getActorUserId() 
    {
        return actorUserId;
    }

    public void setActorUserId(UUID actorUserId) 
    {
        this.actorUserId = actorUserId;
    }

    public String getAction() 
    {
        return action;
    }

    public void setAction(String action) 
    {
        this.action = action;
    }

    public String getEntityType() 
    {
        return entityType;
    }

    public void setEntityType(String entityType) 
    {
        this.entityType = entityType;
    }

    public UUID getEntityId() 
    {
        return entityId;
    }

    public void setEntityId(UUID entityId) 
    {
        this.entityId = entityId;
    }

    public Instant getTimestamp() 
    {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) 
    {
        this.timestamp = timestamp;
    }

    public String getDetails() 
    {
        return details;
    }

    public void setDetails(String details) 
    {
        this.details = details;
    }
}
