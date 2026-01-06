import { User } from "./user";
import { Issues } from "./issues";

export interface IssueHistory {
    id: string;
    issue: Issues;
    changedByUser: User;
    changedAt: string;
    fieldName: 'TITLE' | 'DESCRIPTION' | 'STATUS' | 'SEVERITY' | 'PRIORITY' | null;
    oldValue: string | null;
    newValue: string | null;
    changeType: 'CREATED' | 'STATUS_CHANGE' | 'FIELD_UPDATE';
}

