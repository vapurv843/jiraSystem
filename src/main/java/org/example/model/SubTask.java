package org.example.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
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
}
