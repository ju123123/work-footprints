package com.workfootprint.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.workfootprint.entity.ReportEntity;
import com.workfootprint.entity.WorkRecordEntity;
import com.workfootprint.mapper.ReportMapper;
import com.workfootprint.mapper.WorkRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportService {
    private final ReportMapper reportMapper;
    private final WorkRecordMapper workRecordMapper;
    private final AiService aiService;

    public ReportService(ReportMapper reportMapper, WorkRecordMapper workRecordMapper, AiService aiService) {
        this.reportMapper = reportMapper;
        this.workRecordMapper = workRecordMapper;
        this.aiService = aiService;
    }

    public ReportEntity generate(long userId, String type, LocalDate startDate, LocalDate endDate) {
        List<WorkRecordEntity> records = workRecordMapper.selectList(new LambdaQueryWrapper<WorkRecordEntity>()
                .eq(WorkRecordEntity::getUserId, userId)
                .between(WorkRecordEntity::getRecordDate, startDate, endDate)
                .orderByAsc(WorkRecordEntity::getRecordDate)
                .orderByAsc(WorkRecordEntity::getId));

        List<String> lines = new ArrayList<>();
        for (WorkRecordEntity r : records) {
            String line = r.getRawContent();
            if (StringUtils.hasText(r.getTaskSummary())) {
                line = r.getTaskSummary();
            }
            lines.add(r.getRecordDate() + " " + line);
        }

        String content = aiService.generateReport(type, startDate.toString(), endDate.toString(), lines);
        if (!StringUtils.hasText(content)) {
            content = "# " + type + "\n\n" + String.join("\n", lines);
        }

        ReportEntity e = new ReportEntity();
        e.setUserId(userId);
        e.setType(type);
        e.setStartDate(startDate);
        e.setEndDate(endDate);
        e.setContent(content);
        reportMapper.insert(e);
        return e;
    }

    public List<ReportEntity> list(long userId) {
        return reportMapper.selectList(new LambdaQueryWrapper<ReportEntity>()
                .eq(ReportEntity::getUserId, userId)
                .orderByDesc(ReportEntity::getId));
    }

    public ReportEntity updateContent(long userId, long reportId, String content) {
        ReportEntity existing = reportMapper.selectOne(new LambdaQueryWrapper<ReportEntity>()
                .eq(ReportEntity::getId, reportId)
                .eq(ReportEntity::getUserId, userId));
        if (existing == null) {
            return null;
        }
        existing.setContent(content);
        reportMapper.updateById(existing);
        return reportMapper.selectById(reportId);
    }
}
