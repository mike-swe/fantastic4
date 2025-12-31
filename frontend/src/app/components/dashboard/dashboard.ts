import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { Role } from '../../enum/role';
import { Project } from '../../interfaces/project';
import { Issues} from '../../interfaces/issues';
import { User } from '../../interfaces/user';
import { ProjectService } from '../../services/project-service';
import { IssueService } from '../../services/issue-service';


@Component({
  selector: 'app-dashboard',
  imports: [RouterOutlet, RouterLink, FormsModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {
  currentUser: User | null = null;
  userRole: Role | null = null;
  projects: Project[] = [];
  issues: Issues[] = [];

  constructor(private projectService: ProjectService,
    private issueService: IssueService
  ){}


  ngOnInit(): void {
    // Todo:
    // get authentication for the currentUser. 
    // this.currentUser ;
    // this.userRole = this.currentUser?.role || null;
      this.loadDashBoard();
      this.loadIssues();
  }


  loadDashBoard(): void{
    if (this.userRole == Role.ADMIN){
      this.projectService.getAllProjects().subscribe(projects => {
        this.projects = projects;
      })
    } else if (this.userRole === Role.TESTER || this.userRole === Role.DEVELOPER){
      if (this.currentUser?.id){
        this.projectService.getUserProjects(this.currentUser.id).subscribe(projects => {
          this.projects = projects;
       })
      }
    }
  }

  loadIssues(): void {
    if (this.userRole === Role.ADMIN){
      this.issueService.getAllIssues().subscribe(issues => this.issues = issues);
    } else if (this.userRole === Role.TESTER || this.userRole === Role.DEVELOPER) {
      // Testers/Developers see issues for their projects or issues they created
      if (this.currentUser?.id) {
        this.issueService.getIssuesByUser(this.currentUser.id).subscribe(issues => {
          this.issues = issues;
        });
      }
    }

  }



//
}
