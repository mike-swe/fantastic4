import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { Role } from '../../enum/role';
import { User } from '../../interfaces/user';

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css'
})
export class Sidebar {
  Role = Role;
  currentUser: User | null = null;
  userRole: Role | null = null;

  // TODO 
  // constructor(private authService: AuthService){
  //   // this.currentUser = this.authService.getCurrentUser();
  //   this.userRole = this.currentUser?.role;
  // }
}

