@Entity
@Data
@NoArgsConstructor
@Table(name = "projects")
public class Project {
    @Id
    @Column(name = "project_id")
    private UUID project_id;
    @Column(name = "project_title", nullable = false)
    private String projectTitle;
    @Column(name = "project_description", nullable = false)
    private String projectDesc;
    @Column(name = "project_status", nullable = false)
    private String projectStatus;
    @Column(name = "time_create")
    private Time createdAt;
    @Column(name = "time_update")
    private Time updatedAt;
    @ManyToOne
    @JoinColumn(name = "admin_id")
    private Admin createdBy;
    @ManyToMany
    @JoinColumn(name = "tester_id")
    private Tester assignedTo;

}
