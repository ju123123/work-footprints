package com.workfootprint.controller;

import com.workfootprint.dto.stats.RecordStatsDto;
import com.workfootprint.security.SecurityUtils;
import com.workfootprint.security.UserPrincipal;
import com.workfootprint.service.StatsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/stats")
public class StatsController {
    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/records")
    public RecordStatsDto recordStats(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        UserPrincipal user = SecurityUtils.currentUser();
        return statsService.recordStats(user.userId(), startDate, endDate);
    }
}

