package com.workfootprint.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workfootprint.ai.ParsedWorkRecord;
import com.workfootprint.dto.record.UpdateWorkRecordRequest;
import com.workfootprint.entity.WorkRecordEntity;
import com.workfootprint.mapper.WorkRecordMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class WorkRecordService {
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_CONFIRMED = "CONFIRMED";

    private final WorkRecordMapper workRecordMapper;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

    public WorkRecordService(WorkRecordMapper workRecordMapper, AiService aiService, ObjectMapper objectMapper) {
        this.workRecordMapper = workRecordMapper;
        this.aiService = aiService;
        this.objectMapper = objectMapper;
    }

    public WorkRecordEntity create(long userId, String source, String rawContent, LocalDate recordDate) {
        WorkRecordEntity e = new WorkRecordEntity();
        e.setUserId(userId);
        e.setSource(source);
        e.setRawContent(rawContent);
        e.setRecordDate(recordDate != null ? recordDate : LocalDate.now());
        e.setStatus(STATUS_PENDING);
        ParsedWorkRecord parsed = aiService.parseWorkRecord(rawContent);
        if (parsed != null) {
            e.setProjectName(parsed.getProjectName());
            e.setModuleName(parsed.getModuleName());
            e.setTaskSummary(parsed.getTaskSummary());
            e.setResultSummary(parsed.getResultSummary());
            e.setIssueSummary(parsed.getIssueSummary());
            e.setNextPlan(parsed.getNextPlan());
            if (parsed.getTags() != null) {
                try {
                    e.setTagsJson(objectMapper.writeValueAsString(parsed.getTags()));
                } catch (Exception ignored) {
                }
            }
        }
        workRecordMapper.insert(e);
        return e;
    }

    public List<WorkRecordEntity> listByDate(long userId, LocalDate date) {
        LocalDate d = date != null ? date : LocalDate.now();
        return workRecordMapper.selectList(new LambdaQueryWrapper<WorkRecordEntity>()
                .eq(WorkRecordEntity::getUserId, userId)
                .eq(WorkRecordEntity::getRecordDate, d)
                .orderByDesc(WorkRecordEntity::getId));
    }

    public List<WorkRecordEntity> listPending(long userId) {
        return workRecordMapper.selectList(new LambdaQueryWrapper<WorkRecordEntity>()
                .eq(WorkRecordEntity::getUserId, userId)
                .eq(WorkRecordEntity::getStatus, STATUS_PENDING)
                .orderByDesc(WorkRecordEntity::getId));
    }

    public WorkRecordEntity findById(long userId, long id) {
        return workRecordMapper.selectOne(new LambdaQueryWrapper<WorkRecordEntity>()
                .eq(WorkRecordEntity::getUserId, userId)
                .eq(WorkRecordEntity::getId, id));
    }

    public WorkRecordEntity update(long userId, long id, UpdateWorkRecordRequest req) {
        WorkRecordEntity e = findById(userId, id);
        if (e == null) {
            return null;
        }
        if (req.getRecordDate() != null) e.setRecordDate(req.getRecordDate());
        if (req.getProjectName() != null) e.setProjectName(req.getProjectName());
        if (req.getModuleName() != null) e.setModuleName(req.getModuleName());
        if (req.getTaskSummary() != null) e.setTaskSummary(req.getTaskSummary());
        if (req.getResultSummary() != null) e.setResultSummary(req.getResultSummary());
        if (req.getIssueSummary() != null) e.setIssueSummary(req.getIssueSummary());
        if (req.getNextPlan() != null) e.setNextPlan(req.getNextPlan());
        if (req.getTagsJson() != null) e.setTagsJson(req.getTagsJson());
        if (req.getStatus() != null) e.setStatus(req.getStatus());
        workRecordMapper.updateById(e);
        return findById(userId, id);
    }

    public boolean delete(long userId, long id) {
        WorkRecordEntity e = findById(userId, id);
        if (e == null) {
            return false;
        }
        return workRecordMapper.deleteById(id) > 0;
    }
}
