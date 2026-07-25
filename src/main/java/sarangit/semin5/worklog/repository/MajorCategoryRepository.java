package sarangit.semin5.worklog.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import sarangit.semin5.worklog.entity.major_category;
import java.util.List;
import java.util.Optional;
public interface MajorCategoryRepository extends JpaRepository<major_category, Integer> { List<major_category> findAllByOrderByNameAsc(); Optional<major_category> findByName(String name); }
