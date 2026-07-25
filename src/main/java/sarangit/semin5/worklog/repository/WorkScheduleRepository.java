package sarangit.semin5.worklog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sarangit.semin5.worklog.entity.work_schedule;

public interface WorkScheduleRepository extends JpaRepository<work_schedule, Integer> { }
