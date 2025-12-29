import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterOutlet, RouterLink } from "@angular/router";

@Component({
  selector: 'app-create-account',
  imports: [RouterOutlet, FormsModule, RouterLink],
  templateUrl: './create-account.html',
  styleUrl: './create-account.css',
})
export class CreateAccount {

usernameInput = '';
passwordInput = '';


}
