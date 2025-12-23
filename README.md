# fantastic4
Team Fantastic4



This application should support three types of users: com.revature.fantastic4.entity.Admin in charge of creating projects, Testers that create issues when they find defects in a project, and Developers that update issues as they deploy fixes. Since you are building this application from the ground up there is flexibility in many design choices, as long as the core MVP requirements are met

#Application Features

Client-Side MVP

Implement Angular components for:

Login/Logout feature
Dashboard to view projects and issues
Project Management
create, update, view
Issue management
create, update, view
Server-Side MVP

Implement Spring Boot for the following:

API
/users
authentication (JWT)
adding testers/developers to projects
/projects
create, update, view
/issues
create, update, view

Business Rules
com.revature.fantastic4.entity.Admin can create projects and add Testers/Developers to projects
Testers can open new issues in a project
Developers can move issues to in progress and resolved when they finish their work
Testers can close issues or move them back to open if needed
All project and issue actions should be logged for auditing purposes
Persistence

An SQLite database should be used for persistence in this first sprint

API
Service
Repository
The API handles incoming HTTP requests and returns responses. The Service Layer handles enforcing all business rules and formatting response data. The Repository layer handles interacting with the database


Entities MVP

1. User

id (Primary Key)
username (unique)
password 
email
role (ADMIN, TESTER, DEVELOPER - enum)

Relationships:

Many-to-Many with Project (for Testers/Developers assigned to projects)
One-to-Many with Issue (as creator)
One-to-Many with Comment (as author)

2. Project

id (Primary Key)
name
description
status (ACTIVE, ARCHIVED)
createdBy (Foreign Key to User - the com.revature.fantastic4.entity.Admin who created it) or user_id 
createdAt
updatedAt

Relationships:

Many-to-One with User (creator - com.revature.fantastic4.entity.Admin)
Many-to-Many with User (assigned Testers/Developers)
One-to-Many with Issue

3. Issue

id (Primary Key)
title
description
status (OPEN, IN_PROGRESS, RESOLVED, CLOSED - enum)
severity (LOW, MEDIUM, HIGH, CRITICAL - enum)
priority (LOW, MEDIUM, HIGH, CRITICAL - enum)
project_id (Foreign Key to Project)
createdBy (Foreign Key to User - the com.revature.fantastic4.entity.Tester who created it) user_id
assignedTo (Foreign Key to User - the Developer assigned, nullable) user_id
createdAt
updatedAt
resolvedAt (nullable)
closedAt (nullable)

Relationships:

Many-to-One with Project
Many-to-One with User (creator)
Many-to-One with User (assigned developer)
One-to-Many with Comment

4. Comment

id (Primary Key)
content (text)
issue_id (Foreign Key to Issue)
author_id (Foreign Key to User)
createdAt
updatedAt

Relationships:

Many-to-One with Issue
Many-to-One with User (author)


6. ProjectAssignment (or ProjectUser)
id (Primary Key)
projectId (Foreign Key to Project)
userId (Foreign Key to User)
assignedAt

