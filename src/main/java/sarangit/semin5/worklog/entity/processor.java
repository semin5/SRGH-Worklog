package sarangit.semin5.worklog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name = "processor") @Getter @Setter @NoArgsConstructor
public class processor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private int id;
    @Column(nullable = false, unique = true) private String name;
    @Column(name = "is_active", nullable = false) private boolean active = true;
}
