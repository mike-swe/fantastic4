import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterOutlet, RouterLink } from "@angular/router";
import { AuthService } from '../../services/auth-service';
import { UserService } from '../../services/user-service';

@Component({
  selector: 'app-create-account',
  imports: [RouterOutlet, FormsModule, RouterLink],
  templateUrl: './create-account.html',
  styleUrl: './create-account.css',
})
export class CreateAccount {

emailInput = '';
usernameInput = '';
passwordInput = '';
roleInput = '';

constructor(private userService: UserService){}




createUser() {
  this.userService.insertUser(this.usernameInput, this.passwordInput, this.emailInput, this.roleInput)
    .subscribe({
      next: (response) => {
        console.log('User created!', response);
        // Redirect to login or show success message
      },
      error: (err) => {
        console.error('Registration failed', err);
        // Show error message to user (e.g., "Username already taken")
      }
    });
}

}