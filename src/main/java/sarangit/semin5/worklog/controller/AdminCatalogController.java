package sarangit.semin5.worklog.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sarangit.semin5.worklog.service.AdminCatalogService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/catalogs")
@RequiredArgsConstructor
public class AdminCatalogController {
    private final AdminCatalogService catalogService;
    @GetMapping("/{type}") public List<AdminCatalogService.CatalogItem> list(@PathVariable String type) { return catalogService.list(type); }
    @PostMapping("/{type}") @ResponseStatus(HttpStatus.CREATED) public void create(@PathVariable String type, @RequestBody NameRequest body) { catalogService.create(type, body.name()); }
    @PutMapping("/{type}/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void update(@PathVariable String type, @PathVariable int id, @RequestBody NameRequest body) { catalogService.update(type, id, body.name()); }
    @DeleteMapping("/{type}/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable String type, @PathVariable int id) { catalogService.delete(type, id); }
    @PatchMapping("/processors/{id}/active") @ResponseStatus(HttpStatus.NO_CONTENT) public void setProcessorActive(@PathVariable int id, @RequestBody ActiveRequest body) { catalogService.setProcessorActive(id, body.active()); }
    public record NameRequest(String name) { }
    public record ActiveRequest(boolean active) { }
}
