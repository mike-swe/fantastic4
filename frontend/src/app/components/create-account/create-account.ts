import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterOutlet, RouterLink } from "@angular/router";
import { AuthService } from '../../services/auth-service';

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

constructor(private authService: AuthService){}
createUserAccount(){
  this.authService.createUserAccount(this.usernameInput, this.passwordInput);
}
}
