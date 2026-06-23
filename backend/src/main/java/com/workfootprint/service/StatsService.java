package com.workfootprint.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.workfootprint.dto.stats.ProjectCountDto;
import com.workfootprint.dto.stats.RecordStatsDto;
import com.workfootprint.entity.ProjectEntity;
import com.workfootprint.entity.WorkRecordEntity;
import com.workfootprint.mapper.ProjectMapper;
import com.workfootprint.mapper.WorkRecordMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class StatsService {
    private final WorkRecordMapper workRecordMapper;
    private final ProjectMapper projectMapper;

    public StatsService(WorkRecordMapper workRecordMapper, ProjectMapper projectMapper) {
        this.workRecordMapper = workRecordMapper;
        this.projectMapper = projectMapper;
    }

    public RecordStatsDto recordStats(long userId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<WorkRecordEntity> q = new LambdaQueryWrapper<WorkRecordEntity>()
                .eq(WorkRecordEntity::getUserId, userId);
        if (startDate != null && endDate != null) {
            q.between(WorkRecordEntity::getRecordDate, startDate, endDate);
        } else if (startDate != null) {
            q.ge(WorkRecordEntity::getRecordDate, startDate);
        } else if (endDate != null) {
            q.le(WorkRecordEntity::getRecordDate, endDate);
        }
        List<WorkRecordEntity> records = workRecordMapper.selectList(q);

        RecordStatsDto dto = new RecordStatsDto();
        dto.setTotal(records.size());

        long draft = 0;
        long pending = 0;
        long confirmed = 0;
        Map<Long, Long> byProject = new HashMap<>();
        for (WorkRecordEntity r : records) {
            if (WorkRecordService.STATUS_DRAFT.equals(r.getStatus())) draft++;
            if (WorkRecordService.STATUS_PENDING.equals(r.getStatus())) pending++;
            if (WorkRecordService.STATUS_CONFIRMED.equals(r.getStatus())) confirmed++;
            if (r.getProjectId() != null) {
                byProject.put(r.getProjectId(), byProject.getOrDefault(r.getProjectId(), 0L) + 1);
            }
        }
        dto.setDraft(draft);
        dto.setPending(pending);
        dto.setConfirmed(confirmed);

        List<ProjectCountDto> list = new ArrayList<>();
        if (!byProject.isEmpty()) {
            List<Long> ids = new ArrayList<>(byProject.keySet());
            Map<Long, String> names = new HashMap<>();
            List<ProjectEntity> projects = projectMapper.selectList(new LambdaQueryWrapper<ProjectEntity>()
                    .eq(ProjectEntity::getUserId, userId)
                    .in(ProjectEntity::getId, ids));
            for (ProjectEntity p : projects) {
                names.put(p.getId(), p.getName());
            }
            for (Long projectId : ids) {
                ProjectCountDto p = new ProjectCountDto();
                p.setProjectId(projectId);
                p.setProjectName(names.getOrDefault(projectId, "未知项目"));
                p.setCount(byProject.get(projectId));
                list.add(p);
            }
            list.sort(Comparator.comparingLong(ProjectCountDto::getCount).reversed());
        }
        dto.setByProject(list);
        return dto;
    }
}

