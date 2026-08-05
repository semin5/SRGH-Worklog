package sarangit.semin5.worklog.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sarangit.semin5.worklog.entity.request;
import sarangit.semin5.worklog.service.WorklogService;
import sarangit.semin5.worklog.service.MeetingMinutesService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/worklogs")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class WorklogApiController {
    private final WorklogService worklogService;
    private final MeetingMinutesService meetingMinutesService;

    @GetMapping
    public WorklogService.WorklogPageData list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Integer processor,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer major,
            @RequestParam(required = false) Integer minor,
            @RequestParam(required = false) Integer department,
            @RequestParam(required = false) String requestContent) {
        return worklogService.getPageData(from, to, processor, keyword, major, minor, department, requestContent);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public request create(@RequestBody CreateWorklogRequest body, @RequestHeader(value = "X-Worklog-Client", required = false) String clientId) {
        return worklogService.create(body.request_date(), body.major_category(), body.minor_category(), body.department(),
                body.requester(), body.requester_extension(), body.request_content(), clientId);
    }

    @PutMapping("/{id}")
    public request updateRequest(@PathVariable int id, @RequestBody CreateWorklogRequest body, @RequestHeader(value = "X-Worklog-Client", required = false) String clientId) {
        return worklogService.updateRequest(id, body.request_date(), body.major_category(), body.minor_category(), body.department(),
                body.requester(), body.requester_extension(), body.request_content(), clientId);
    }

    @PutMapping("/{id}/processing")
    public request updateProcessing(@PathVariable int id, @RequestBody ProcessingRequest body, @RequestHeader(value = "X-Worklog-Client", required = false) String clientId) {
        return worklogService.updateProcessing(id, body.processor(), body.processing_date(), body.processing_content(), clientId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int id, @RequestHeader(value = "X-Worklog-Client", required = false) String clientId) { worklogService.delete(id, clientId); }

    @PostMapping(value = "/meeting-minutes/pdf", produces = "application/pdf")
    public org.springframework.http.ResponseEntity<byte[]> meetingMinutes(@RequestBody MeetingMinutesRequest body) {
        byte[] pdf = meetingMinutesService.generate(new MeetingMinutesService.MeetingMinutes(body.meeting_date(), body.author(),
                body.attendees().stream().map(a -> new MeetingMinutesService.Attendee(a.name(), a.present(), a.reason())).toList(), body.notice()));
        return org.springframework.http.ResponseEntity.ok().header("Content-Disposition", "attachment; filename=meeting-minutes.pdf").body(pdf);
    }

    public record CreateWorklogRequest(LocalDate request_date, String major_category, String minor_category, String department,
                                       String requester, String requester_extension, String request_content) { }
    public record ProcessingRequest(Integer processor, LocalDate processing_date, String processing_content) { }
    public record MeetingMinutesRequest(LocalDate meeting_date, String author, List<AttendeeRequest> attendees, String notice) { }
    public record AttendeeRequest(String name, boolean present, String reason) { }
}
