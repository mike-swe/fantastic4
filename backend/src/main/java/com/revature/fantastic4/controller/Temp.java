package com.revature.fantastic4.controller;


import com.revature.fantastic4.repository.IssueRepository;
import com.revature.fantastic4.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Temp {
    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private ProjectRepository projectRepository;



}