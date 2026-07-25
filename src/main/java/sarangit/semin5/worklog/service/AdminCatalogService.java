package sarangit.semin5.worklog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sarangit.semin5.worklog.entity.department;
import sarangit.semin5.worklog.entity.major_category;
import sarangit.semin5.worklog.entity.minor_category;
import sarangit.semin5.worklog.entity.processor;
import sarangit.semin5.worklog.repository.DepartmentRepository;
import sarangit.semin5.worklog.repository.MajorCategoryRepository;
import sarangit.semin5.worklog.repository.MinorCategoryRepository;
import sarangit.semin5.worklog.repository.ProcessorRepository;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCatalogService {
    private final MajorCategoryRepository majors;
    private final MinorCategoryRepository minors;
    private final DepartmentRepository departments;
    private final ProcessorRepository processors;

    public List<CatalogItem> list(String type) {
        return switch (type) {
            case "majors" -> majors.findAllByOrderByIdAsc().stream().map(item -> new CatalogItem(item.getId(), item.getName(), null)).toList();
            case "minors" -> minors.findAllByOrderByIdAsc().stream().map(item -> new CatalogItem(item.getId(), item.getName(), null)).toList();
            case "departments" -> departments.findAllByOrderByNameAsc().stream().sorted(Comparator.comparing(item -> "기타".equals(item.getName()))).map(item -> new CatalogItem(item.getId(), item.getName(), null)).toList();
            case "processors" -> processors.findAll().stream().sorted(Comparator.comparing(processor::getName)).map(item -> new CatalogItem(item.getId(), item.getName(), item.isActive())).toList();
            default -> throw new IllegalArgumentException("Unknown catalog type");
        };
    }

    @Transactional
    public void create(String type, String name) {
        switch (type) {
            case "majors" -> { major_category item = new major_category(); item.setName(name); majors.save(item); }
            case "minors" -> { minor_category item = new minor_category(); item.setName(name); minors.save(item); }
            case "departments" -> { department item = new department(); item.setName(name); departments.save(item); }
            case "processors" -> { processor item = new processor(); item.setName(name); item.setActive(true); processors.save(item); }
            default -> throw new IllegalArgumentException("Unknown catalog type");
        }
    }

    @Transactional
    public void update(String type, int id, String name) {
        switch (type) {
            case "majors" -> majors.findById(id).orElseThrow().setName(name);
            case "minors" -> minors.findById(id).orElseThrow().setName(name);
            case "departments" -> departments.findById(id).orElseThrow().setName(name);
            case "processors" -> processors.findById(id).orElseThrow().setName(name);
            default -> throw new IllegalArgumentException("Unknown catalog type");
        }
    }

    @Transactional
    public void delete(String type, int id) {
        switch (type) {
            case "majors" -> majors.deleteById(id);
            case "minors" -> minors.deleteById(id);
            case "departments" -> departments.deleteById(id);
            case "processors" -> processors.deleteById(id);
            default -> throw new IllegalArgumentException("Unknown catalog type");
        }
    }
    @Transactional
    public void setProcessorActive(int id, boolean active) { processors.findById(id).orElseThrow().setActive(active); }
    public record CatalogItem(int id, String name, Boolean active) { }
}
