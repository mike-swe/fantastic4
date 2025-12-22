@Data
@NoArgsConstructor
@Entity
@Table(name = 'admins')
Public class Admin {
    @Id
    @Column(name = 'admin_id', nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID adminID;
    @Column
    private String username;
    @Column
    private String password;
<<<<<<< HEAD
}
=======
}
>>>>>>> origin/main
