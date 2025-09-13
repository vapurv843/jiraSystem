package org.example.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Ticket {
    private static long counter = 1;

    @EqualsAndHashCode.Include
    private final Long id;

    private String title;
    private String description;
    private TicketType type;
    private TicketStatus status;
    private String assignee;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ToString.Exclude
    private List<String> comments = new ArrayList<>();

    @ToString.Exclude
    private List<SubTask> subTasks = new ArrayList<>();

    public Ticket(String title, String description, TicketType type, String assignee) {
        this.id = counter++;
        this.title = title;
        this.description = description;
        this.type = type;
        this.assignee = assignee;
        this.status = TicketStatus.OPEN;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }


    public void addComment(String comment) {
        comments.add(comment);
        updateTimestamp();
    }

    public void addSubTask(SubTask subTask) {
        subTasks.add(subTask);
        updateTimestamp();
    }

    public void removeSubTask(SubTask subTask) {
        subTasks.remove(subTask);
        updateTimestamp();
    }

    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean areAllSubTasksCompleted() {
        if (subTasks.isEmpty()) return true;

        return subTasks.stream().allMatch(subTask -> {
            TicketStatus subTaskStatus = subTask.getStatus();
            return switch (type) {
                case STORY -> subTaskStatus == TicketStatus.DEPLOYED;
                case EPIC -> subTaskStatus == TicketStatus.COMPLETED;
                case ON_CALL -> subTaskStatus == TicketStatus.RESOLVED;
                default -> false;
            };
        });
    }

    public TicketStatus getFinalStatus() {
        return switch (type) {
            case STORY -> TicketStatus.DEPLOYED;
            case EPIC -> TicketStatus.COMPLETED;
            case ON_CALL -> TicketStatus.RESOLVED;
            default -> throw new IllegalStateException("Unexpected TicketType: " + type);
        };
    }
}
