package org.example.service;

import org.example.model.Ticket;
import org.example.model.TicketStatus;
import org.example.model.TicketType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TicketService {
    private final Map<Long, Ticket> tickets = new ConcurrentHashMap<>();
    

    public Ticket createTicket(String title, String description, TicketType type, String assignee) {
        Ticket ticket = new Ticket(title, description, type, assignee);
        tickets.put(ticket.getId(), ticket);
        return ticket;
    }

    public Optional<Ticket> getTicket(Long ticketId) {
        return Optional.ofNullable(tickets.get(ticketId));
    }
    

    public List<Ticket> getAllTickets() {
        return new ArrayList<>(tickets.values());
    }

    public List<Ticket> getTicketsByType(TicketType type) {
        return tickets.values().stream()
                .filter(ticket -> ticket.getType() == type)
                .collect(Collectors.toList());
    }



    public boolean updateTicketStatus(Long ticketId, TicketStatus newStatus) {
        Optional<Ticket> ticketOpt = getTicket(ticketId);
        if (ticketOpt.isEmpty()) {
            return false;
        }
        
        Ticket ticket = ticketOpt.get();
        
        if (!isValidStatusTransition(ticket.getType(), ticket.getStatus(), newStatus)) {
            throw new IllegalArgumentException(
                String.format("Invalid status transition from %s to %s for ticket type %s", 
                            ticket.getStatus(), newStatus, ticket.getType()));
        }
        
        if (newStatus == ticket.getFinalStatus() && !ticket.areAllSubTasksCompleted()) {
            throw new IllegalStateException(
                "Cannot close ticket " + ticketId + " - mark all sub-task completed to change status");
        }
        
        ticket.setStatus(newStatus);
        return true;
    }

    public boolean updateTicketAssignee(Long ticketId, String newAssignee) {
        Optional<Ticket> ticketOpt = getTicket(ticketId);
        if (ticketOpt.isEmpty()) {
            return false;
        }
        
        ticketOpt.get().setAssignee(newAssignee);
        return true;
    }
    public boolean addComment(Long ticketId, String comment) {
        Optional<Ticket> ticketOpt = getTicket(ticketId);
        if (ticketOpt.isEmpty()) {
            return false;
        }
        
        ticketOpt.get().addComment(comment);
        return true;
    }

    public boolean deleteTicket(Long ticketId) {
        return tickets.remove(ticketId) != null;
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


    private boolean isValidStatusTransition(TicketType type, TicketStatus currentStatus, TicketStatus newStatus) {
        if (type == TicketType.STORY) {
            return isValidStoryTransition(currentStatus, newStatus);
        } else if (type == TicketType.EPIC) {
            return isValidEpicTransition(currentStatus, newStatus);
        } else if (type == TicketType.ON_CALL) {
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



}
