package com.workfootprint.controller;

import com.workfootprint.dto.report.GenerateReportRequest;
import com.workfootprint.dto.report.ReportDto;
import com.workfootprint.entity.ReportEntity;
import com.workfootprint.security.SecurityUtils;
import com.workfootprint.security.UserPrincipal;
import com.workfootprint.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public ReportDto generate(@Valid @RequestBody GenerateReportRequest request) {
        UserPrincipal user = SecurityUtils.currentUser();
        ReportEntity e = reportService.generate(user.userId(), request.getType(), request.getStartDate(), request.getEndDate());
        return toDto(e);
    }

    @GetMapping
    public List<ReportDto> list() {
        UserPrincipal user = SecurityUtils.currentUser();
        return reportService.list(user.userId()).stream().map(this::toDto).toList();
    }

    private ReportDto toDto(ReportEntity e) {
        ReportDto d = new ReportDto();
        d.setId(e.getId());
        d.setType(e.getType());
        d.setStartDate(e.getStartDate());
        d.setEndDate(e.getEndDate());
        d.setContent(e.getContent());
        d.setCreatedAt(e.getCreatedAt());
        d.setUpdatedAt(e.getUpdatedAt());
        return d;
    }
}

