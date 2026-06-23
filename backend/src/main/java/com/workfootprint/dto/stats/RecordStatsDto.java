package com.workfootprint.dto.stats;

import java.util.List;

public class RecordStatsDto {
    private long total;
    private long draft;
    private long pending;
    private long confirmed;
    private List<ProjectCountDto> byProject;

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getDraft() {
        return draft;
    }

    public void setDraft(long draft) {
        this.draft = draft;
    }

    public long getPending() {
        return pending;
    }

    public void setPending(long pending) {
        this.pending = pending;
    }

    public long getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(long confirmed) {
        this.confirmed = confirmed;
    }

    public List<ProjectCountDto> getByProject() {
        return byProject;
    }

    public void setByProject(List<ProjectCountDto> byProject) {
        this.byProject = byProject;
    }
}

