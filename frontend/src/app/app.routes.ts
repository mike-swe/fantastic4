import { Routes } from '@angular/router';
import { authGuardGuard } from './guards/auth-guard-guard';

import { CreateAccount } from './components/create-account/create-account';
import { Dashboard } from './components/dashboard/dashboard';
import { LoginPage } from './components/login-page/login-page';
import { Projects } from './components/projects/projects';
import { IssuesComponent } from './components/issues/issues';
import { AssignProject } from './components/assign-project/assign-project';
import { MyProjects } from './components/my-projects/my-projects';
import { MyIssues } from './components/my-issues/my-issues';
import { AssignedIssues } from './components/assigned-issues/assigned-issues';
import { AuditLogs } from './components/audit-logs/audit-logs';

export const routes: Routes = [
  { path: 'login', component: LoginPage },
  { path: 'create-account', component: CreateAccount },
  { path: 'dashboard', component: Dashboard, canActivate: [authGuardGuard] },
  { path: 'projects', component: Projects, canActivate: [authGuardGuard] },
  { path: 'issues', component: IssuesComponent, canActivate: [authGuardGuard] },
  { path: 'users', component: AssignProject, canActivate: [authGuardGuard] },
  { path: 'my-projects', component: MyProjects, canActivate: [authGuardGuard] },
  { path: 'my-issues', component: MyIssues, canActivate: [authGuardGuard] },
  { path: 'assigned-issues', component: AssignedIssues, canActivate: [authGuardGuard] },
  { path: 'audit-logs', component: AuditLogs, canActivate: [authGuardGuard] },
  { path: '', redirectTo: '/login', pathMatch: 'full' }
];
