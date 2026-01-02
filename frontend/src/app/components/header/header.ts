import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { User } from '../../interfaces/user';

@Component({
  selector: 'app-header',
  imports: [RouterLink],
  templateUrl: './header.html',
  styleUrl: './header.css'
})
export class Header {
  private router = inject(Router);
  
  getCurrentUser(): User | null {
    // TODO: Get from AuthService when implemented
    return null;
  }
  
  getUserRole(): string | null {
    // TODO: Get from AuthService when implemented
    return null;
  }
  
  logout(): void {
    // TODO: Implement logout via AuthService
    this.router.navigate(['/login']);
  }
}

