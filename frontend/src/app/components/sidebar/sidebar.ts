import { Component, OnInit, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { filter, Subscription } from 'rxjs';
import { Role } from '../../enum/role';
import { User } from '../../interfaces/user';
import { AuthService } from '../../services/auth-service';

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css'
})
export class Sidebar implements OnInit, OnDestroy {
  readonly Role = Role;
  currentUser: User | null = null;
  userRole: Role | null = null;
  private routerSubscription?: Subscription;

  constructor(
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadUser();
    
    this.routerSubscription = this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.loadUser();
      });
  }

  ngOnDestroy(): void {
    if (this.routerSubscription) {
      this.routerSubscription.unsubscribe();
    }
  }

  private loadUser(): void {
    const token = this.authService.getToken();
    console.log('Sidebar - Token exists:', !!token);
    
    if (token && this.authService.isAuthenticated()) {
      this.currentUser = this.authService.getCurrentUser();
      this.userRole = this.currentUser?.role ?? null;
      console.log('Sidebar - Current User:', this.currentUser);
      console.log('Sidebar - User Role:', this.userRole);
      this.cdr.detectChanges();
    } else {
      console.log('Sidebar - No token or not authenticated, showing Dashboard only');
      this.currentUser = null;
      this.userRole = null;
      this.cdr.detectChanges();
    }
  }
}
