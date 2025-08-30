package org.example.model;
import java.time.LocalDateTime;


public class SubTask {
    private static long counter = 1;

    private final Long id;
    private final Long parentTicketId;
    private String title;
    private String description;
    private TicketStatus status;
    private String assignee;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

    public SubTask(Long parentTicketId, String title, String description, String assignee) {
        this.id = counter++;
        this.parentTicketId = parentTicketId;
        this.title = title;
        this.description = description;
        this.assignee = assignee;
        this.status = TicketStatus.OPEN;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.version = 1L;
    }

    public Long getId() { return id; }
    public Long getParentTicketId() { return parentTicketId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TicketStatus getStatus() { return status; }
    public String getAssignee() { return assignee; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }


    public void setStatus(TicketStatus status) {
        this.status = status;
        updateTimestamp();
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
        updateTimestamp();
    }

    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
        this.version++;
    }

    @Override
    public String toString() {
        return String.format("SubTask{id=%d, parentTicketId=%d, title='%s', status=%s, assignee='%s'}",
                           id, parentTicketId, title, status, assignee);
    }


}
