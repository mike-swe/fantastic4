import { Role } from "../enum/role";

export interface User {
    id: string;
    username: string;
    email: string;
    role: Role;
    createdAt?: string;
    updatedAt?: string;
}
