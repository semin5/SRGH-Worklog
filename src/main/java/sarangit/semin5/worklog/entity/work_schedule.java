package sarangit.semin5.worklog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "work_schedule")
@Getter @Setter @NoArgsConstructor
public class work_schedule {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private int id;
    @Column(nullable = false, length = 150) private String title;
    @Column(nullable = false) private LocalDate start_date;
    @Column(nullable = false, length = 20) private String recurrence = "NONE";
    private Integer period_months;
    @Column(nullable = false) private boolean completed = false;
    private LocalDate completed_date;
}
