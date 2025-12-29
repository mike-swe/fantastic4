import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-login-page',
  imports: [FormsModule, RouterLink],
  templateUrl: './login-page.html',
  styleUrl: './login-page.css',
})
export class LoginPage {

usernameInput = ""
passwordInput = ""
failedLoginMessage = signal("");

validateLogin() {
  if (this.usernameInput != "test" || this.passwordInput != "test"){
    this.failedLoginMessage.set("the number you have dialed is not in service")
  }
  else {
    this.failedLoginMessage.set("")
    console.log('ok')
  }
}
/*
todo
route to dashboard if not failed login
route to register

*/

}
