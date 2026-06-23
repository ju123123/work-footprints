package com.workfootprint.controller;

import com.workfootprint.dto.project.*;
import com.workfootprint.entity.ProjectEntity;
import com.workfootprint.entity.ProjectModuleEntity;
import com.workfootprint.entity.ProjectTaskEntity;
import com.workfootprint.security.SecurityUtils;
import com.workfootprint.security.UserPrincipal;
import com.workfootprint.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<ProjectDto> list() {
        UserPrincipal user = SecurityUtils.currentUser();
        return projectService.listProjects(user.userId()).stream().map(this::toDto).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectDto create(@Valid @RequestBody CreateProjectRequest request) {
        UserPrincipal user = SecurityUtils.currentUser();
        ProjectEntity e = projectService.createProject(user.userId(), request);
        return toDto(e);
    }

    @PatchMapping("/{projectId}")
    public ProjectDto update(@PathVariable("projectId") long projectId, @RequestBody UpdateProjectRequest request) {
        UserPrincipal user = SecurityUtils.currentUser();
        ProjectEntity e = projectService.updateProject(user.userId(), projectId, request);
        if (e == null) {
            throw new NotFoundException();
        }
        return toDto(e);
    }

    @DeleteMapping("/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("projectId") long projectId) {
        UserPrincipal user = SecurityUtils.currentUser();
        boolean ok = projectService.deleteProject(user.userId(), projectId);
        if (!ok) {
            throw new NotFoundException();
        }
    }

    @GetMapping("/{projectId}/modules")
    public List<ProjectModuleDto> listModules(@PathVariable("projectId") long projectId) {
        UserPrincipal user = SecurityUtils.currentUser();
        return projectService.listModules(user.userId(), projectId).stream().map(this::toDto).toList();
    }

    @PostMapping("/{projectId}/modules")
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectModuleDto createModule(@PathVariable("projectId") long projectId, @Valid @RequestBody CreateProjectModuleRequest request) {
        UserPrincipal user = SecurityUtils.currentUser();
        ProjectModuleEntity e = projectService.createModule(user.userId(), projectId, request);
        if (e == null) {
            throw new NotFoundException();
        }
        return toDto(e);
    }

    @PatchMapping("/{projectId}/modules/{moduleId}")
    public ProjectModuleDto updateModule(@PathVariable("projectId") long projectId, @PathVariable("moduleId") long moduleId, @RequestBody UpdateProjectModuleRequest request) {
        UserPrincipal user = SecurityUtils.currentUser();
        ProjectModuleEntity e = projectService.updateModule(user.userId(), projectId, moduleId, request);
        if (e == null) {
            throw new NotFoundException();
        }
        return toDto(e);
    }

    @DeleteMapping("/{projectId}/modules/{moduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteModule(@PathVariable("projectId") long projectId, @PathVariable("moduleId") long moduleId) {
        UserPrincipal user = SecurityUtils.currentUser();
        boolean ok = projectService.deleteModule(user.userId(), projectId, moduleId);
        if (!ok) {
            throw new NotFoundException();
        }
    }

    @GetMapping("/{projectId}/tasks")
    public List<ProjectTaskDto> listTasks(@PathVariable("projectId") long projectId, @RequestParam(value = "moduleId", required = false) Long moduleId) {
        UserPrincipal user = SecurityUtils.currentUser();
        return projectService.listTasks(user.userId(), projectId, moduleId).stream().map(this::toDto).toList();
    }

    @PostMapping("/{projectId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectTaskDto createTask(@PathVariable("projectId") long projectId, @Valid @RequestBody CreateProjectTaskRequest request) {
        UserPrincipal user = SecurityUtils.currentUser();
        ProjectTaskEntity e = projectService.createTask(user.userId(), projectId, request);
        if (e == null) {
            throw new NotFoundException();
        }
        return toDto(e);
    }

    @PatchMapping("/{projectId}/tasks/{taskId}")
    public ProjectTaskDto updateTask(@PathVariable("projectId") long projectId, @PathVariable("taskId") long taskId, @RequestBody UpdateProjectTaskRequest request) {
        UserPrincipal user = SecurityUtils.currentUser();
        ProjectTaskEntity e = projectService.updateTask(user.userId(), projectId, taskId, request);
        if (e == null) {
            throw new NotFoundException();
        }
        return toDto(e);
    }

    @DeleteMapping("/{projectId}/tasks/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable("projectId") long projectId, @PathVariable("taskId") long taskId) {
        UserPrincipal user = SecurityUtils.currentUser();
        boolean ok = projectService.deleteTask(user.userId(), projectId, taskId);
        if (!ok) {
            throw new NotFoundException();
        }
    }

    private ProjectDto toDto(ProjectEntity e) {
        ProjectDto d = new ProjectDto();
        d.setId(e.getId());
        d.setName(e.getName());
        d.setDescription(e.getDescription());
        d.setStatus(e.getStatus());
        d.setCreatedAt(e.getCreatedAt());
        d.setUpdatedAt(e.getUpdatedAt());
        return d;
    }

    private ProjectModuleDto toDto(ProjectModuleEntity e) {
        ProjectModuleDto d = new ProjectModuleDto();
        d.setId(e.getId());
        d.setProjectId(e.getProjectId());
        d.setName(e.getName());
        d.setCreatedAt(e.getCreatedAt());
        d.setUpdatedAt(e.getUpdatedAt());
        return d;
    }

    private ProjectTaskDto toDto(ProjectTaskEntity e) {
        ProjectTaskDto d = new ProjectTaskDto();
        d.setId(e.getId());
        d.setProjectId(e.getProjectId());
        d.setModuleId(e.getModuleId());
        d.setName(e.getName());
        d.setStatus(e.getStatus());
        d.setProgressSummary(e.getProgressSummary());
        d.setRiskSummary(e.getRiskSummary());
        d.setCreatedAt(e.getCreatedAt());
        d.setUpdatedAt(e.getUpdatedAt());
        return d;
    }
}

