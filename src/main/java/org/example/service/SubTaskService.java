package org.example.service;

import org.example.model.SubTask;
import org.example.model.Ticket;
import org.example.model.TicketStatus;
import org.example.model.TicketType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class SubTaskService {
    private final Map<Long, SubTask> subTasks = new ConcurrentHashMap<>();
    private final TicketService ticketService;
    
    public SubTaskService(TicketService ticketService) {
        this.ticketService = ticketService;
    }
    

    public SubTask createSubTask(Long parentTicketId, String title, String description, String assignee) {
        Optional<Ticket> parentTicket = ticketService.getTicket(parentTicketId);
        if (parentTicket.isEmpty()) {
            throw new IllegalArgumentException("Parent ticket not found: " + parentTicketId);
        }
        
        SubTask subTask = new SubTask(parentTicketId, title, description, assignee);
        subTasks.put(subTask.getId(), subTask);
        
        parentTicket.get().addSubTask(subTask);
        
        return subTask;
    }
    

    public Optional<SubTask> getSubTask(Long subTaskId) {
        return Optional.ofNullable(subTasks.get(subTaskId));
    }
    

    public List<SubTask> getSubTasksForTicket(Long ticketId) {
        return subTasks.values().stream()
                .filter(subTask -> subTask.getParentTicketId().equals(ticketId))
                .collect(Collectors.toList());
    }
    

    public List<SubTask> getAllSubTasks() {
        return new ArrayList<>(subTasks.values());
    }


    public List<SubTask> getSubTasksByAssignee(String assignee) {
        return subTasks.values().stream()
                .filter(subTask -> assignee.equals(subTask.getAssignee()))
                .collect(Collectors.toList());
    }

    public boolean updateSubTaskStatus(Long subTaskId, TicketStatus newStatus) {
        Optional<SubTask> subTaskOpt = getSubTask(subTaskId);
        if (subTaskOpt.isEmpty()) {
            return false;
        }
        
        SubTask subTask = subTaskOpt.get();
        Optional<Ticket> parentTicket = ticketService.getTicket(subTask.getParentTicketId());
        
        if (parentTicket.isEmpty()) {
            throw new IllegalStateException("Parent ticket not found for sub-task: " + subTaskId);
        }
        
        TicketType parentType = parentTicket.get().getType();
        
        if (!isValidStatusTransition(parentType, subTask.getStatus(), newStatus)) {
            throw new IllegalArgumentException(
                String.format("Invalid status transition from %s to %s for sub-task of type %s", 
                            subTask.getStatus(), newStatus, parentType));
        }
        
        subTask.setStatus(newStatus);
        return true;
    }
    

    public boolean updateSubTaskAssignee(Long subTaskId, String newAssignee) {
        Optional<SubTask> subTaskOpt = getSubTask(subTaskId);
        if (subTaskOpt.isEmpty()) {
            return false;
        }
        
        subTaskOpt.get().setAssignee(newAssignee);
        return true;
    }
    

    public boolean deleteSubTask(Long subTaskId) {
        Optional<SubTask> subTaskOpt = getSubTask(subTaskId);
        if (subTaskOpt.isEmpty()) {
            return false;
        }
        
        SubTask subTask = subTaskOpt.get();
        
        Optional<Ticket> parentTicket = ticketService.getTicket(subTask.getParentTicketId());
        if (parentTicket.isPresent()) {
            parentTicket.get().removeSubTask(subTask);
        }
        
        return subTasks.remove(subTaskId) != null;
    }

    private boolean isValidStatusTransition(TicketType parentType, TicketStatus currentStatus, TicketStatus newStatus) {
        if (parentType == TicketType.STORY) {
            return isValidStoryTransition(currentStatus, newStatus);
        } else if (parentType == TicketType.EPIC) {
            return isValidEpicTransition(currentStatus, newStatus);
        } else if (parentType == TicketType.ON_CALL) {
            return isValidOnCallTransition(currentStatus, newStatus);
        } else {
            return false;
        }
    }


    private boolean isValidStoryTransition(TicketStatus current, TicketStatus next) {
        if (current == TicketStatus.OPEN) {
            return next == TicketStatus.IN_PROGRESS;
        } else if (current == TicketStatus.IN_PROGRESS) {
            return next == TicketStatus.TESTING || next == TicketStatus.OPEN;
        } else if (current == TicketStatus.TESTING) {
            return next == TicketStatus.IN_REVIEW || next == TicketStatus.IN_PROGRESS;
        } else if (current == TicketStatus.IN_REVIEW) {
            return next == TicketStatus.DEPLOYED || next == TicketStatus.TESTING;
        } else if (current == TicketStatus.DEPLOYED) {
            return false;
        } else {
            return false;
        }
    }


    private boolean isValidEpicTransition(TicketStatus current, TicketStatus next) {
        if (current == TicketStatus.OPEN) {
            return next == TicketStatus.IN_PROGRESS;
        } else if (current == TicketStatus.IN_PROGRESS) {
            return next == TicketStatus.COMPLETED || next == TicketStatus.OPEN;
        } else if (current == TicketStatus.COMPLETED) {
            return false;
        } else {
            return false;
        }
    }


    private boolean isValidOnCallTransition(TicketStatus current, TicketStatus next) {
        if (current == TicketStatus.OPEN) {
            return next == TicketStatus.IN_PROGRESS;
        } else if (current == TicketStatus.IN_PROGRESS) {
            return next == TicketStatus.RESOLVED || next == TicketStatus.OPEN;
        } else if (current == TicketStatus.RESOLVED) {
            return false;
        } else {
            return false;
        }
    }



    public List<TicketStatus> getValidNextStatuses(Long subTaskId) {
        Optional<SubTask> subTaskOpt = getSubTask(subTaskId);
        if (subTaskOpt.isEmpty()) {
            return Collections.emptyList();
        }
        
        SubTask subTask = subTaskOpt.get();
        Optional<Ticket> parentTicket = ticketService.getTicket(subTask.getParentTicketId());
        
        if (parentTicket.isEmpty()) {
            return Collections.emptyList();
        }
        
        TicketType parentType = parentTicket.get().getType();
        List<TicketStatus> validStatuses = new ArrayList<>();
        
        for (TicketStatus status : TicketStatus.values()) {
            if (isValidStatusTransition(parentType, subTask.getStatus(), status)) {
                validStatuses.add(status);
            }
        }
        
        return validStatuses;
    }
}
