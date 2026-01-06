import { Component, signal, inject, computed } from '@angular/core';
import { RouterOutlet, Router } from '@angular/router';
import { Header } from './components/header/header';
import { Sidebar } from './components/sidebar/sidebar';
import { CreateAccount } from './components/create-account/create-account';
import { Dashboard } from './components/dashboard/dashboard';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Header, Sidebar, CreateAccount, Dashboard],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('frontend');
  private router = inject(Router);
  
  private noNavbarRoutes = ['/login', '/create-account'];
  
  protected showNavbar = computed(() => {
    const currentRoute = this.router.url;
    return !this.noNavbarRoutes.some(route => currentRoute.includes(route));
  });
}
