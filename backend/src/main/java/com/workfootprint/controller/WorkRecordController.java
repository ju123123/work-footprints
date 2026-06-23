package com.workfootprint.controller;

import com.workfootprint.dto.record.CreateWorkRecordRequest;
import com.workfootprint.dto.record.UpdateWorkRecordRequest;
import com.workfootprint.dto.record.WorkRecordDto;
import com.workfootprint.entity.WorkRecordEntity;
import com.workfootprint.security.SecurityUtils;
import com.workfootprint.security.UserPrincipal;
import com.workfootprint.service.WorkRecordService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/records")
public class WorkRecordController {
    private final WorkRecordService workRecordService;

    public WorkRecordController(WorkRecordService workRecordService) {
        this.workRecordService = workRecordService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkRecordDto create(@Valid @RequestBody CreateWorkRecordRequest request) {
        UserPrincipal user = SecurityUtils.currentUser();
        WorkRecordEntity e = workRecordService.create(
                user.userId(),
                "WEB",
                request.getRawContent(),
                request.getRecordDate(),
                request.getStatus(),
                request.getProjectId(),
                request.getModuleId(),
                request.getTaskId()
        );
        return toDto(e);
    }

    @GetMapping
    public List<WorkRecordDto> listByDate(@RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        UserPrincipal user = SecurityUtils.currentUser();
        return workRecordService.listByDate(user.userId(), date).stream().map(this::toDto).toList();
    }

    @GetMapping("/pending")
    public List<WorkRecordDto> pending() {
        UserPrincipal user = SecurityUtils.currentUser();
        return workRecordService.listPending(user.userId()).stream().map(this::toDto).toList();
    }

    @GetMapping("/search")
    public List<WorkRecordDto> search(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "projectId", required = false) Long projectId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "tag", required = false) String tag
    ) {
        UserPrincipal user = SecurityUtils.currentUser();
        return workRecordService.search(user.userId(), startDate, endDate, projectId, status, keyword, tag).stream().map(this::toDto).toList();
    }

    @PostMapping("/{id}/publish")
    public WorkRecordDto publish(@PathVariable("id") long id) {
        UserPrincipal user = SecurityUtils.currentUser();
        WorkRecordEntity e = workRecordService.publish(user.userId(), id);
        if (e == null) {
            throw new NotFoundException();
        }
        return toDto(e);
    }

    @PatchMapping("/{id}")
    public WorkRecordDto update(@PathVariable("id") long id, @RequestBody UpdateWorkRecordRequest request) {
        UserPrincipal user = SecurityUtils.currentUser();
        WorkRecordEntity e = workRecordService.update(user.userId(), id, request);
        if (e == null) {
            throw new NotFoundException();
        }
        return toDto(e);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") long id) {
        UserPrincipal user = SecurityUtils.currentUser();
        boolean ok = workRecordService.delete(user.userId(), id);
        if (!ok) {
            throw new NotFoundException();
        }
    }

    private WorkRecordDto toDto(WorkRecordEntity e) {
        WorkRecordDto d = new WorkRecordDto();
        d.setId(e.getId());
        d.setSource(e.getSource());
        d.setRawContent(e.getRawContent());
        d.setRecordDate(e.getRecordDate());
        d.setProjectId(e.getProjectId());
        d.setModuleId(e.getModuleId());
        d.setTaskId(e.getTaskId());
        d.setProjectName(e.getProjectName());
        d.setModuleName(e.getModuleName());
        d.setTaskSummary(e.getTaskSummary());
        d.setResultSummary(e.getResultSummary());
        d.setIssueSummary(e.getIssueSummary());
        d.setNextPlan(e.getNextPlan());
        d.setTagsJson(e.getTagsJson());
        d.setStatus(e.getStatus());
        d.setCreatedAt(e.getCreatedAt());
        d.setUpdatedAt(e.getUpdatedAt());
        return d;
    }
}
