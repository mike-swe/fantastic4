
@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID>
{
    List<Project> findByCreator(Admin admin);
}