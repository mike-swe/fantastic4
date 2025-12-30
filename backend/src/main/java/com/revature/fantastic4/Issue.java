
@Entity
@Data
@NoArgsConstructor
@Table(name = "issues")
public class Issue {
    @Id
    @Column(name = "issue_id")
    private UUID issueId;
    @Column(name = "issue_title", nullable = false)
    private String issueTitle;
    @Column(name = "issue_description", nullable = false)
    private String issueDesc;
    @Column(name = "issue_severity", nullable = false)
    private String issueSeverity;
    @Column(name = "issue_handled")
    private boolean issueHandled;
    @ManyToOne
    @JoinColumn(name = "tester_id", nullable = false)
    private Tester tester;
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
}