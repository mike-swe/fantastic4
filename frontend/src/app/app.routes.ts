import { Routes } from '@angular/router';
import { LoginPage } from './components/login-page/login-page';
import { CreateAccount } from './components/create-account/create-account';
import { Dashboard } from './components/dashboard/dashboard';

export const routes: Routes = [
  { path: 'login', component: LoginPage },
  { path: 'create-account', component: CreateAccount },
  { path: 'dashboard', component: Dashboard },
  // { path: 'projects', component: ProjectsComponent },
  // { path: 'issues', component: IssuesComponent },
  { path: '', redirectTo: '/login', pathMatch: 'full' }
];