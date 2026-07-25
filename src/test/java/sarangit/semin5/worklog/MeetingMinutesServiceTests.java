package sarangit.semin5.worklog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sarangit.semin5.worklog.service.MeetingMinutesService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class MeetingMinutesServiceTests {
    @Autowired MeetingMinutesService meetingMinutesService;

    @Test
    void createsPdfWithKoreanText() {
        byte[] pdf = meetingMinutesService.generate(new MeetingMinutesService.MeetingMinutes(
                LocalDate.now(), "테스터", List.of(new MeetingMinutesService.Attendee("홍길동", true, "")), "알림사항"));
        assertTrue(pdf.length > 4);
        assertTrue(new String(pdf, 0, 4).startsWith("%PDF"));
    }
}
