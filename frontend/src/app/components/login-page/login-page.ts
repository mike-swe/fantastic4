import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-login-page',
  imports: [FormsModule],
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
//button call login user 
//in function hard code if user input = ect 

}
