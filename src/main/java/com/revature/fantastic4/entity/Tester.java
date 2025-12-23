package com.revature.fantastic4.entity;

@Data
@NoArgsConstructor
@Entity
@Table(name = 'tester')
public class Tester {
    @Id
    @Column(name = 'tester_id', nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID testerID;
    @Column
    private String testerUsername;
    @Column
    private String testerPswd;
}