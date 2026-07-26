package sarangit.semin5.worklog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sarangit.semin5.worklog.entity.department;
import sarangit.semin5.worklog.entity.major_category;
import sarangit.semin5.worklog.entity.minor_category;
import sarangit.semin5.worklog.entity.request;
import sarangit.semin5.worklog.repository.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorklogService {
    private final RequestRepository requests;
    private final ProcessorRepository processors;
    private final MajorCategoryRepository majors;
    private final MinorCategoryRepository minors;
    private final DepartmentRepository departments;
    private final ApplicationEventPublisher eventPublisher;

    public WorklogPageData getPageData(LocalDate from, LocalDate to, Integer processorId, String keyword) {
        Map<Integer, String> majorNames = majors.findAll().stream().collect(Collectors.toMap(major_category::getId, major_category::getName));
        Map<Integer, String> minorNames = minors.findAll().stream().collect(Collectors.toMap(minor_category::getId, minor_category::getName));
        Map<Integer, String> departmentNames = departments.findAll().stream().collect(Collectors.toMap(department::getId, department::getName));
        Map<Integer, String> processorNames = processors.findAll().stream().collect(Collectors.toMap(p -> p.getId(), p -> p.getName()));
        List<request> items = requests.findAll().stream()
                .filter(r -> isInProgress(r) || matchesFilters(r, from, to, processorId, keyword, majorNames, minorNames, departmentNames))
                .sorted(Comparator.comparing(this::isInProgress).reversed()
                        .thenComparing(request::getRequest_date, Comparator.reverseOrder())
                        .thenComparing(request::getId, Comparator.reverseOrder()))
                .toList();
        return new WorklogPageData(items, majors.findAllByOrderByIdAsc(), minors.findAllByOrderByIdAsc(),
                departments.findAllByOrderByNameAsc().stream().sorted(Comparator.comparing(item -> "기타".equals(item.getName()))).toList(), processors.findAll().stream().filter(p -> p.isActive()).sorted(Comparator.comparing(p -> p.getName())).toList(),
                majorNames, minorNames, departmentNames, processorNames);
    }

    @Transactional
    public void create(LocalDate requestDate, String majorCategory, String minorCategory, String departmentName,
                       String requester, String extension, String requestContent) {
        request item = new request();
        item.setRequest_date(requestDate);
        item.setMajor_category(resolveMajor(majorCategory));
        item.setMinor_category(resolveMinor(minorCategory));
        item.setDepartment(resolveDepartment(departmentName));
        item.setRequester(requester);
        item.setRequester_extension(extension);
        item.setRequest_content(requestContent);
        item.setProcessor(null);
        item.setProcessing_date(null);
        item.setProcessing_content(null);
        requests.save(item);
        eventPublisher.publishEvent(new WorklogChangedEvent());
    }

    @Transactional
    public void updateRequest(int id, LocalDate requestDate, String majorCategory, String minorCategory, String departmentName,
                              String requester, String extension, String requestContent) {
        request item = requests.findById(id).orElseThrow();
        item.setRequest_date(requestDate);
        item.setMajor_category(resolveMajor(majorCategory));
        item.setMinor_category(resolveMinor(minorCategory));
        item.setDepartment(resolveDepartment(departmentName));
        item.setRequester(requester);
        item.setRequester_extension(extension);
        item.setRequest_content(requestContent);
        eventPublisher.publishEvent(new WorklogChangedEvent());
    }

    @Transactional
    public void updateProcessing(int id, Integer processor, LocalDate processingDate, String processingContent) {
        request item = requests.findById(id).orElseThrow();
        item.setProcessor(processor);
        item.setProcessing_date(processingDate);
        item.setProcessing_content(processingContent);
        eventPublisher.publishEvent(new WorklogChangedEvent());
    }

    @Transactional
    public void delete(int id) { requests.deleteById(id); eventPublisher.publishEvent(new WorklogChangedEvent()); }

    private boolean matches(request r, String keyword, Map<Integer, String> majorNames, Map<Integer, String> minorNames, Map<Integer, String> departmentNames) {
        if (keyword == null || keyword.isBlank()) return true;
        String value = keyword.toLowerCase(Locale.ROOT);
        return List.of(majorNames.getOrDefault(r.getMajor_category(), ""), minorNames.getOrDefault(r.getMinor_category(), ""), departmentNames.getOrDefault(r.getDepartment(), ""), r.getRequester(),
                r.getRequester_extension(), r.getRequest_content(), Optional.ofNullable(r.getProcessing_content()).orElse(""))
                .stream().anyMatch(v -> v.toLowerCase(Locale.ROOT).contains(value));
    }

    private boolean matchesFilters(request r, LocalDate from, LocalDate to, Integer processorId, String keyword,
                                   Map<Integer, String> majorNames, Map<Integer, String> minorNames, Map<Integer, String> departmentNames) {
        return (from == null || !r.getRequest_date().isBefore(from))
                && (to == null || !r.getRequest_date().isAfter(to))
                && (processorId == null || processorId.equals(r.getProcessor()))
                && matches(r, keyword, majorNames, minorNames, departmentNames);
    }

    private boolean isInProgress(request r) { return r.getProcessing_date() == null; }

    private int resolveMajor(String value) {
        if (value.matches("\\d+")) return majors.findById(Integer.parseInt(value)).orElseThrow().getId();
        return majors.findByName(value).orElseGet(() -> { major_category item = new major_category(); item.setName(value); return majors.save(item); }).getId();
    }
    private int resolveMinor(String value) {
        if (value.matches("\\d+")) return minors.findById(Integer.parseInt(value)).orElseThrow().getId();
        return minors.findByName(value).orElseGet(() -> { minor_category item = new minor_category(); item.setName(value); return minors.save(item); }).getId();
    }
    private int resolveDepartment(String value) {
        if (value.matches("\\d+")) return departments.findById(Integer.parseInt(value)).orElseThrow().getId();
        return departments.findByName(value).orElseGet(() -> { department item = new department(); item.setName(value); return departments.save(item); }).getId();
    }

    public record WorklogPageData(List<request> items, List<major_category> majors, List<minor_category> minors,
                                  List<department> departments, List<sarangit.semin5.worklog.entity.processor> processors,
                                  Map<Integer, String> majorNames, Map<Integer, String> minorNames, Map<Integer, String> departmentNames,
                                  Map<Integer, String> processorNames) { }
}
