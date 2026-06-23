package com.workfootprint.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.workfootprint.dto.project.CreateProjectModuleRequest;
import com.workfootprint.dto.project.CreateProjectRequest;
import com.workfootprint.dto.project.CreateProjectTaskRequest;
import com.workfootprint.dto.project.UpdateProjectModuleRequest;
import com.workfootprint.dto.project.UpdateProjectRequest;
import com.workfootprint.dto.project.UpdateProjectTaskRequest;
import com.workfootprint.entity.ProjectEntity;
import com.workfootprint.entity.ProjectModuleEntity;
import com.workfootprint.entity.ProjectTaskEntity;
import com.workfootprint.mapper.ProjectMapper;
import com.workfootprint.mapper.ProjectModuleMapper;
import com.workfootprint.mapper.ProjectTaskMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ProjectService {
    private final ProjectMapper projectMapper;
    private final ProjectModuleMapper moduleMapper;
    private final ProjectTaskMapper taskMapper;

    public ProjectService(ProjectMapper projectMapper, ProjectModuleMapper moduleMapper, ProjectTaskMapper taskMapper) {
        this.projectMapper = projectMapper;
        this.moduleMapper = moduleMapper;
        this.taskMapper = taskMapper;
    }

    public List<ProjectEntity> listProjects(long userId) {
        return projectMapper.selectList(new LambdaQueryWrapper<ProjectEntity>()
                .eq(ProjectEntity::getUserId, userId)
                .orderByDesc(ProjectEntity::getId));
    }

    public ProjectEntity createProject(long userId, CreateProjectRequest request) {
        ProjectEntity e = new ProjectEntity();
        e.setUserId(userId);
        e.setName(request.getName());
        e.setDescription(request.getDescription());
        e.setStatus("ACTIVE");
        projectMapper.insert(e);
        return projectMapper.selectById(e.getId());
    }

    public ProjectEntity updateProject(long userId, long projectId, UpdateProjectRequest request) {
        ProjectEntity e = getProject(userId, projectId);
        if (e == null) {
            return null;
        }
        if (StringUtils.hasText(request.getName())) e.setName(request.getName());
        if (request.getDescription() != null) e.setDescription(request.getDescription());
        if (StringUtils.hasText(request.getStatus())) e.setStatus(request.getStatus());
        projectMapper.updateById(e);
        return getProject(userId, projectId);
    }

    public boolean deleteProject(long userId, long projectId) {
        ProjectEntity e = getProject(userId, projectId);
        if (e == null) {
            return false;
        }
        moduleMapper.delete(new LambdaQueryWrapper<ProjectModuleEntity>().eq(ProjectModuleEntity::getProjectId, projectId));
        taskMapper.delete(new LambdaQueryWrapper<ProjectTaskEntity>().eq(ProjectTaskEntity::getProjectId, projectId));
        return projectMapper.deleteById(projectId) > 0;
    }

    public List<ProjectModuleEntity> listModules(long userId, long projectId) {
        ProjectEntity p = getProject(userId, projectId);
        if (p == null) {
            return List.of();
        }
        return moduleMapper.selectList(new LambdaQueryWrapper<ProjectModuleEntity>()
                .eq(ProjectModuleEntity::getProjectId, projectId)
                .orderByAsc(ProjectModuleEntity::getId));
    }

    public ProjectModuleEntity createModule(long userId, long projectId, CreateProjectModuleRequest request) {
        ProjectEntity p = getProject(userId, projectId);
        if (p == null) {
            return null;
        }
        ProjectModuleEntity e = new ProjectModuleEntity();
        e.setProjectId(projectId);
        e.setName(request.getName());
        moduleMapper.insert(e);
        return moduleMapper.selectById(e.getId());
    }

    public ProjectModuleEntity updateModule(long userId, long projectId, long moduleId, UpdateProjectModuleRequest request) {
        ProjectEntity p = getProject(userId, projectId);
        if (p == null) {
            return null;
        }
        ProjectModuleEntity e = moduleMapper.selectOne(new LambdaQueryWrapper<ProjectModuleEntity>()
                .eq(ProjectModuleEntity::getId, moduleId)
                .eq(ProjectModuleEntity::getProjectId, projectId));
        if (e == null) {
            return null;
        }
        if (StringUtils.hasText(request.getName())) e.setName(request.getName());
        moduleMapper.updateById(e);
        return moduleMapper.selectById(moduleId);
    }

    public boolean deleteModule(long userId, long projectId, long moduleId) {
        ProjectEntity p = getProject(userId, projectId);
        if (p == null) {
            return false;
        }
        ProjectModuleEntity e = moduleMapper.selectOne(new LambdaQueryWrapper<ProjectModuleEntity>()
                .eq(ProjectModuleEntity::getId, moduleId)
                .eq(ProjectModuleEntity::getProjectId, projectId));
        if (e == null) {
            return false;
        }
        taskMapper.delete(new LambdaQueryWrapper<ProjectTaskEntity>().eq(ProjectTaskEntity::getModuleId, moduleId));
        return moduleMapper.deleteById(moduleId) > 0;
    }

    public List<ProjectTaskEntity> listTasks(long userId, long projectId, Long moduleId) {
        ProjectEntity p = getProject(userId, projectId);
        if (p == null) {
            return List.of();
        }
        LambdaQueryWrapper<ProjectTaskEntity> q = new LambdaQueryWrapper<ProjectTaskEntity>()
                .eq(ProjectTaskEntity::getProjectId, projectId)
                .orderByDesc(ProjectTaskEntity::getId);
        if (moduleId != null) {
            q.eq(ProjectTaskEntity::getModuleId, moduleId);
        }
        return taskMapper.selectList(q);
    }

    public ProjectTaskEntity createTask(long userId, long projectId, CreateProjectTaskRequest request) {
        ProjectEntity p = getProject(userId, projectId);
        if (p == null) {
            return null;
        }
        if (request.getModuleId() != null) {
            ProjectModuleEntity m = moduleMapper.selectOne(new LambdaQueryWrapper<ProjectModuleEntity>()
                    .eq(ProjectModuleEntity::getId, request.getModuleId())
                    .eq(ProjectModuleEntity::getProjectId, projectId));
            if (m == null) {
                return null;
            }
        }
        ProjectTaskEntity e = new ProjectTaskEntity();
        e.setProjectId(projectId);
        e.setModuleId(request.getModuleId());
        e.setName(request.getName());
        e.setStatus(request.getStatus());
        e.setProgressSummary(request.getProgressSummary());
        e.setRiskSummary(request.getRiskSummary());
        taskMapper.insert(e);
        return taskMapper.selectById(e.getId());
    }

    public ProjectTaskEntity updateTask(long userId, long projectId, long taskId, UpdateProjectTaskRequest request) {
        ProjectEntity p = getProject(userId, projectId);
        if (p == null) {
            return null;
        }
        ProjectTaskEntity e = taskMapper.selectOne(new LambdaQueryWrapper<ProjectTaskEntity>()
                .eq(ProjectTaskEntity::getId, taskId)
                .eq(ProjectTaskEntity::getProjectId, projectId));
        if (e == null) {
            return null;
        }
        if (StringUtils.hasText(request.getName())) e.setName(request.getName());
        if (request.getModuleId() != null) {
            if (request.getModuleId() == 0) {
                e.setModuleId(null);
            } else {
                ProjectModuleEntity m = moduleMapper.selectOne(new LambdaQueryWrapper<ProjectModuleEntity>()
                        .eq(ProjectModuleEntity::getId, request.getModuleId())
                        .eq(ProjectModuleEntity::getProjectId, projectId));
                if (m == null) {
                    return null;
                }
                e.setModuleId(request.getModuleId());
            }
        }
        if (StringUtils.hasText(request.getStatus())) e.setStatus(request.getStatus());
        if (request.getProgressSummary() != null) e.setProgressSummary(request.getProgressSummary());
        if (request.getRiskSummary() != null) e.setRiskSummary(request.getRiskSummary());
        taskMapper.updateById(e);
        return taskMapper.selectById(taskId);
    }

    public boolean deleteTask(long userId, long projectId, long taskId) {
        ProjectEntity p = getProject(userId, projectId);
        if (p == null) {
            return false;
        }
        ProjectTaskEntity e = taskMapper.selectOne(new LambdaQueryWrapper<ProjectTaskEntity>()
                .eq(ProjectTaskEntity::getId, taskId)
                .eq(ProjectTaskEntity::getProjectId, projectId));
        if (e == null) {
            return false;
        }
        return taskMapper.deleteById(taskId) > 0;
    }

    private ProjectEntity getProject(long userId, long projectId) {
        return projectMapper.selectOne(new LambdaQueryWrapper<ProjectEntity>()
                .eq(ProjectEntity::getId, projectId)
                .eq(ProjectEntity::getUserId, userId));
    }
}

