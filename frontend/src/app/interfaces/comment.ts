import { User } from "./user";

export interface Comment {
    id: string;
    content: string;
    issue: {
        id: string;
    };
    author: User;
    createdAt: string;
    updatedAt: string;
}

