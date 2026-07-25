package sarangit.semin5.worklog.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import sarangit.semin5.worklog.entity.department;
import java.util.List;
import java.util.Optional;
public interface DepartmentRepository extends JpaRepository<department, Integer> { List<department> findAllByOrderByNameAsc(); List<department> findAllByOrderByIdAsc(); Optional<department> findByName(String name); }
