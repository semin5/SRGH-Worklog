package sarangit.semin5.worklog.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import sarangit.semin5.worklog.entity.minor_category;
import java.util.List;
import java.util.Optional;
public interface MinorCategoryRepository extends JpaRepository<minor_category, Integer> { List<minor_category> findAllByOrderByNameAsc(); Optional<minor_category> findByName(String name); }
