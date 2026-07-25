package sarangit.semin5.worklog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name = "major_category") @Getter @Setter @NoArgsConstructor
public class major_category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private int id;
    @Column(nullable = false, unique = true) private String name;
}
