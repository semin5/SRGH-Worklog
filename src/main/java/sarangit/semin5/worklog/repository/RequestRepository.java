package sarangit.semin5.worklog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sarangit.semin5.worklog.entity.request;

public interface RequestRepository extends JpaRepository<request, Integer> { }
