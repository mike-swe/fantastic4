import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';

@Component({
  selector: 'app-login-page',
  imports: [FormsModule, RouterLink],
  templateUrl: './login-page.html',
  styleUrl: './login-page.css',
})
export class LoginPage {

  private router = inject(Router);

usernameInput = ""
passwordInput = ""
failedLoginMessage = signal("");

/*validateLogin() {
  if (this.usernameInput != "test" || this.passwordInput != "test"){
    this.failedLoginMessage.set("the number you have dialed is not in service")
    this.router.navigate(['/register'])
  }
  else {
    this.failedLoginMessage.set("")
    this.router.navigate(['/dashboard'])
  }
}*/
validateLogin() {
  this.httpClient.post("http://localhost:8080/login",
  {
    username: this.usernameInput,
    password: this.passwordInput
  },{
    observe: "response"
  }).subscribe(
  {
    next: response => {
      console.log(response.status)
      this.router.navigate(['/dashboard'])
    },
    error: err => {
      this.failedLoginMessage.set("the number you have dialed is not in service");
      console.log(this.failedLoginMessage);
    }
  }
  );
}
/*
todo
route to dashboard if not failed login
route to register

*/

}
