package org.example.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Ticket {
    private static long counter = 1;

    private final Long id;
    private String title;
    private String description;
    private TicketType type;
    private TicketStatus status;
    private String assignee;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> comments;
    private List<SubTask> subTasks;

    
    public Ticket(String title, String description, TicketType type, String assignee) {
        this.id = counter++;
        this.title = title;
        this.description = description;
        this.type = type;
        this.assignee = assignee;
        this.status = TicketStatus.OPEN;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.comments = new ArrayList<>();
        this.subTasks = new ArrayList<>();

    }
    
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TicketType getType() { return type; }
    public TicketStatus getStatus() { return status; }
    public String getAssignee() { return assignee; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<String> getComments() { return new ArrayList<>(comments); }
    public List<SubTask> getSubTasks() { return new ArrayList<>(subTasks); }

    
    public void setTitle(String title) {
        this.title = title;
        updateTimestamp();
    }
    
    public void setDescription(String description) {
        this.description = description;
        updateTimestamp();
    }
    
    public void setStatus(TicketStatus status) {
        this.status = status;
        updateTimestamp();
    }
    
    public void setAssignee(String assignee) {
        this.assignee = assignee;
        updateTimestamp();
    }
    
    public void addComment(String comment) {
        this.comments.add(comment);
        updateTimestamp();
    }
    
    public void addSubTask(SubTask subTask) {
        this.subTasks.add(subTask);
        updateTimestamp();
    }
    
    public void removeSubTask(SubTask subTask) {
        this.subTasks.remove(subTask);
        updateTimestamp();
    }

    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();

    }


    public boolean areAllSubTasksCompleted() {
        if (subTasks.isEmpty()) {
            return true;
        }

        return subTasks.stream().allMatch(subTask -> {
            TicketStatus subTaskStatus = subTask.getStatus();
            if (type == TicketType.STORY) {
                return subTaskStatus == TicketStatus.DEPLOYED;
            } else if (type == TicketType.EPIC) {
                return subTaskStatus == TicketStatus.COMPLETED;
            } else if (type == TicketType.ON_CALL) {
                return subTaskStatus == TicketStatus.RESOLVED;
            } else {
                return false;
            }
        });
    }



    public TicketStatus getFinalStatus() {
        if (type == TicketType.STORY) {
            return TicketStatus.DEPLOYED;
        } else if (type == TicketType.EPIC) {
            return TicketStatus.COMPLETED;
        } else if (type == TicketType.ON_CALL) {
            return TicketStatus.RESOLVED;
        } else {
            throw new IllegalStateException("Unexpected TicketType: " + type);
        }
    }

    
    @Override
    public String toString() {
        return String.format("Ticket{id=%d, title='%s', type=%s, status=%s, assignee='%s'}", 
                           id, title, type, status, assignee);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Ticket ticket = (Ticket) obj;
        return id.equals(ticket.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
