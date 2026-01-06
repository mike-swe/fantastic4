import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../services/auth-service';

@Component({
  selector: 'app-login-page',
  imports: [FormsModule, RouterLink],
  templateUrl: './login-page.html',
  styleUrl: './login-page.css',
})
export class LoginPage {
  private router = inject(Router);
  private authService = inject(AuthService);

  usernameInput = "";
  passwordInput = "";
  failedLoginMessage = signal("");

  validateLogin() {
    this.failedLoginMessage.set("");
    
    if (!this.usernameInput || !this.passwordInput) {
      this.failedLoginMessage.set("Please enter both username and password");
      return;
    }

    this.authService.login(this.usernameInput, this.passwordInput).subscribe({
      next: () => {
        this.failedLoginMessage.set("");
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        console.error('Login error:', err);
        this.failedLoginMessage.set("Invalid username or password");
      }
    });
  }
}


