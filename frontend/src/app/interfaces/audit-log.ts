export interface AuditLog {
    id: number;
    actorUserId: string;
    action: string;
    entityType: string;
    entityId: string;
    timestamp: string;
    details: string;
}

