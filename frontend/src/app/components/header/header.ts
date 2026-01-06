import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router, RouterLink, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs';
import { User } from '../../interfaces/user';
import { AuthService } from '../../services/auth-service';
import { Role } from '../../enum/role';

@Component({
  selector: 'app-header',
  imports: [RouterLink],
  templateUrl: './header.html',
  styleUrl: './header.css'
})
export class Header implements OnInit {
  private router = inject(Router);
  private authService = inject(AuthService);
  private cdr = inject(ChangeDetectorRef);
  currentUser: User | null = null;
  readonly Role = Role;
  
  ngOnInit(): void {
    this.loadUser();
    
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.loadUser();
      });
  }
  
  private loadUser(): void {
    if (this.authService.isAuthenticated()) {
      this.currentUser = this.authService.getCurrentUser();
      this.cdr.detectChanges();
    } else {
      this.currentUser = null;
      this.cdr.detectChanges();
    }
  }
  
  getCurrentUser(): User | null {
    return this.currentUser || this.authService.getCurrentUser();
  }
  
  getUserRole(): string {
    const user = this.getCurrentUser();
    if (!user || user.role === null || user.role === undefined) {
      return 'GUEST';
    }
    const roleMap: { [key: number]: string } = {
      [Role.ADMIN]: 'ADMIN',
      [Role.TESTER]: 'TESTER',
      [Role.DEVELOPER]: 'DEVELOPER'
    };
    return roleMap[user.role] || 'GUEST';
  }
  
  logout(): void {
    this.authService.logout();
    this.currentUser = null;
    this.cdr.detectChanges();
    this.router.navigate(['/login']);
  }
}

