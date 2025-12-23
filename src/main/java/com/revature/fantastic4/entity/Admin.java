package com.revature.fantastic4.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "admins")
public class Admin {
    @Id
    @Column(name = "admin_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID adminID;
    @Column
    private String username;
    @Column
    private String password;

}



