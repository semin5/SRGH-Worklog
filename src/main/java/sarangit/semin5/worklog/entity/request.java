package sarangit.semin5.worklog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "request")
@Getter @Setter @NoArgsConstructor
public class request {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now(KST);
    @Column(nullable = false)
    private LocalDate request_date;
    private LocalDate processing_date;
    @Column(nullable = false) private int major_category;
    @Column(nullable = false) private int minor_category;
    @Column(nullable = false) private int department;
    @Column(nullable = false) private String requester;
    @Column(length = 50, nullable = false) private String requester_extension;
    @Column(nullable = false, columnDefinition = "TEXT") private String request_content;
    private Integer processor;
    @Column(columnDefinition = "TEXT") private String processing_content;

    @PrePersist
    void createTimestamp() { timestamp = LocalDateTime.now(KST); }

    @PreUpdate
    void updateTimestamp() { timestamp = LocalDateTime.now(KST); }
}
