package sarangit.semin5.worklog.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sarangit.semin5.worklog.service.ScheduleService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleApiController {
    private final ScheduleService scheduleService;
    @GetMapping public ScheduleService.MonthData month(@RequestParam int year, @RequestParam int month) { return scheduleService.month(year, month); }
    @GetMapping("/admin") public List<ScheduleService.ScheduleData> all() { return scheduleService.all(); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public void create(@RequestBody ScheduleRequest body) { scheduleService.create(body.title(), body.start_date(), body.recurrence(), body.period_months()); }
    @PutMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void update(@PathVariable int id, @RequestBody ScheduleRequest body) { scheduleService.update(id, body.title(), body.start_date(), body.recurrence(), body.period_months()); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable int id) { scheduleService.delete(id); }
    @PatchMapping("/{id}/complete") @ResponseStatus(HttpStatus.NO_CONTENT) public void complete(@PathVariable int id) { scheduleService.complete(id); }
    @PatchMapping("/{id}/completion") @ResponseStatus(HttpStatus.NO_CONTENT) public void setCompletion(@PathVariable int id, @RequestBody CompletionRequest body) { scheduleService.setCompleted(id, body.completed()); }
    public record ScheduleRequest(String title, LocalDate start_date, String recurrence, Integer period_months) { }
    public record CompletionRequest(boolean completed) { }
}
