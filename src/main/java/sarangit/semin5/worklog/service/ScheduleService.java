package sarangit.semin5.worklog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sarangit.semin5.worklog.entity.work_schedule;
import sarangit.semin5.worklog.repository.WorkScheduleRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final WorkScheduleRepository schedules;
    private final ApplicationEventPublisher eventPublisher;

    public MonthData month(int year, int month) {
        resetExpiredCompletions();
        YearMonth target = YearMonth.of(year, month);
        List<Occurrence> items = schedules.findAll().stream().flatMap(schedule -> target.atDay(1).datesUntil(target.plusMonths(1).atDay(1))
                .filter(day -> occursOn(schedule, day)).map(day -> new Occurrence(schedule.getId(), day, schedule.getTitle(), schedule.isCompleted()))).toList();
        return new MonthData(items);
    }

    public List<ScheduleData> all() {
        resetExpiredCompletions();
        return schedules.findAll().stream().map(this::toData).toList();
    }
    @Transactional
    public void create(String title, LocalDate startDate, String recurrence, Integer periodMonths) {
        work_schedule schedule = new work_schedule(); apply(schedule, title, startDate, recurrence, periodMonths); schedules.save(schedule);
        changed();
    }
    @Transactional
    public void update(int id, String title, LocalDate startDate, String recurrence, Integer periodMonths) {
        work_schedule schedule = schedules.findById(id).orElseThrow();
        apply(schedule, title, startDate, recurrence, periodMonths);
        schedules.save(schedule);
        changed();
    }
    @Transactional
    public void delete(int id) { schedules.deleteById(id); changed(); }
    @Transactional
    public void complete(int id) {
        work_schedule schedule = schedules.findById(id).orElseThrow();
        if (!schedule.isCompleted()) {
            schedule.setCompleted(true);
            schedule.setCompleted_date(LocalDate.now());
            schedules.save(schedule);
            changed();
        }
    }
    @Transactional
    public void setCompleted(int id, boolean completed) {
        work_schedule schedule = schedules.findById(id).orElseThrow();
        schedule.setCompleted(completed);
        schedule.setCompleted_date(completed ? LocalDate.now() : null);
        schedules.save(schedule);
        changed();
    }

    private void changed() { eventPublisher.publishEvent(new ScheduleChangedEvent()); }

    private void apply(work_schedule schedule, String title, LocalDate startDate, String recurrence, Integer periodMonths) {
        schedule.setTitle(title); schedule.setStart_date(startDate); schedule.setRecurrence(periodMonths != null && periodMonths > 0 ? "MONTHLY" : (recurrence == null || recurrence.isBlank() ? "NONE" : recurrence)); schedule.setPeriod_months(periodMonths != null && periodMonths > 0 ? periodMonths : null);
    }
    private void resetExpiredCompletions() {
        YearMonth currentMonth = YearMonth.now();
        List<work_schedule> expired = schedules.findAll().stream()
                .filter(schedule -> schedule.isCompleted() && schedule.getCompleted_date() != null
                        && YearMonth.from(schedule.getCompleted_date()).isBefore(currentMonth))
                .toList();
        expired.forEach(schedule -> { schedule.setCompleted(false); schedule.setCompleted_date(null); });
        if (!expired.isEmpty()) schedules.saveAll(expired);
    }
    private ScheduleData toData(work_schedule schedule) { return new ScheduleData(schedule.getId(), schedule.getTitle(), schedule.getStart_date(), schedule.getRecurrence(), schedule.getPeriod_months(), schedule.isCompleted(), schedule.getCompleted_date()); }
    private boolean occursOn(work_schedule schedule, LocalDate day) {
        if (day.isBefore(schedule.getStart_date())) return false;
        if (schedule.getPeriod_months() != null) {
            long months = ChronoUnit.MONTHS.between(java.time.YearMonth.from(schedule.getStart_date()), java.time.YearMonth.from(day));
            return months >= 0 && months % schedule.getPeriod_months() == 0 && day.getDayOfMonth() == Math.min(schedule.getStart_date().getDayOfMonth(), day.lengthOfMonth());
        }
        return switch (schedule.getRecurrence()) {
            case "DAILY" -> true;
            case "WEEKLY" -> ChronoUnit.DAYS.between(schedule.getStart_date(), day) % 7 == 0;
            case "MONTHLY" -> day.getDayOfMonth() == Math.min(schedule.getStart_date().getDayOfMonth(), day.lengthOfMonth());
            case "YEARLY" -> day.getMonthValue() == schedule.getStart_date().getMonthValue() && day.getDayOfMonth() == schedule.getStart_date().getDayOfMonth();
            default -> day.equals(schedule.getStart_date());
        };
    }
    public record Occurrence(int id, LocalDate date, String title, boolean completed) { }
    public record MonthData(List<Occurrence> items) { }
    public record ScheduleData(int id, String title, LocalDate start_date, String recurrence, Integer period_months, boolean completed, LocalDate completed_date) { }
}
