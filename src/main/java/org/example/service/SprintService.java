package org.example.service;

import org.example.model.Sprint;
import org.example.model.Ticket;
import org.example.model.TicketType;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class SprintService {
    private final Map<Long, Sprint> sprints = new ConcurrentHashMap<>();
    private final TicketService ticketService;
    private Long currentActiveSprintId = null;
    
    public SprintService(TicketService ticketService) {
        this.ticketService = ticketService;
    }
    

    public Sprint createSprint(String name, String description, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        Sprint sprint = new Sprint(name, description, startDate, endDate);
        sprints.put(sprint.getId(), sprint);
        return sprint;
    }
    

    public Optional<Sprint> getSprint(Long sprintId) {
        return Optional.ofNullable(sprints.get(sprintId));
    }
    

    public List<Sprint> getAllSprints() {
        return new ArrayList<>(sprints.values());
    }
    

    public Optional<Sprint> getActiveSprint() {
        if (currentActiveSprintId == null) {
            return Optional.empty();
        }
        return getSprint(currentActiveSprintId);
    }

    public boolean startSprint(Long sprintId) {
        Optional<Sprint> sprintOpt = getSprint(sprintId);
        if (sprintOpt.isEmpty()) {
            return false;
        }
        
        Sprint sprint = sprintOpt.get();
        
        if (currentActiveSprintId != null) {
            throw new IllegalStateException("Cannot start sprint - another sprint is already active: " + currentActiveSprintId);
        }
        
        sprint.setActive(true);
        currentActiveSprintId = sprintId;
        return true;
    }
    

    public boolean endActiveSprint() {
        if (currentActiveSprintId == null) {
            return false;
        }
        
        Optional<Sprint> sprintOpt = getSprint(currentActiveSprintId);
        if (sprintOpt.isPresent()) {
            sprintOpt.get().setActive(false);
        }
        
        currentActiveSprintId = null;
        return true;
    }
    

    public boolean addStoryToSprint(Long sprintId, Long storyId) {
        Optional<Sprint> sprintOpt = getSprint(sprintId);
        if (sprintOpt.isEmpty()) {
            return false;
        }
        
        Optional<Ticket> ticketOpt = ticketService.getTicket(storyId);
        if (ticketOpt.isEmpty()) {
            throw new IllegalArgumentException("Ticket not found: " + storyId);
        }
        
        Ticket ticket = ticketOpt.get();
        if (ticket.getType() != TicketType.STORY) {
            throw new IllegalArgumentException("Only Story tickets can be added to sprints. Ticket " + storyId + " is of type: " + ticket.getType());
        }
        
        for (Sprint sprint : sprints.values()) {
            if (!sprint.getId().equals(sprintId) && sprint.getStoryIds().contains(storyId)) {
                throw new IllegalStateException("Story " + storyId + " is already in sprint " + sprint.getId());
            }
        }
        
        sprintOpt.get().addStory(storyId);
        return true;
    }
    

    public boolean removeStoryFromSprint(Long sprintId, Long storyId) {
        Optional<Sprint> sprintOpt = getSprint(sprintId);
        if (sprintOpt.isEmpty()) {
            return false;
        }
        
        sprintOpt.get().removeStory(storyId);
        return true;
    }
    

    public boolean addStoryToCurrentSprint(Long storyId) {
        if (currentActiveSprintId == null) {
            throw new IllegalStateException("No active sprint to add story to");
        }
        
        return addStoryToSprint(currentActiveSprintId, storyId);
    }

    public boolean removeStoryFromCurrentSprint(Long storyId) {
        if (currentActiveSprintId == null) {
            return false;
        }
        
        return removeStoryFromSprint(currentActiveSprintId, storyId);
    }
    

    public List<Ticket> getStoriesInSprint(Long sprintId) {
        Optional<Sprint> sprintOpt = getSprint(sprintId);
        if (sprintOpt.isEmpty()) {
            return Collections.emptyList();
        }
        
        return sprintOpt.get().getStoryIds().stream()
                .map(ticketService::getTicket)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public List<Ticket> getStoriesInCurrentSprint() {
        if (currentActiveSprintId == null) {
            return Collections.emptyList();
        }
        
        return getStoriesInSprint(currentActiveSprintId);
    }

    public boolean deleteSprint(Long sprintId) {
        Optional<Sprint> sprintOpt = getSprint(sprintId);
        if (sprintOpt.isEmpty()) {
            return false;
        }
        
        Sprint sprint = sprintOpt.get();
        if (sprint.isActive()) {
            throw new IllegalStateException("Cannot delete active sprint: " + sprintId);
        }
        
        return sprints.remove(sprintId) != null;
    }

    public Map<String, Object> getSprintStatistics(Long sprintId) {
        Optional<Sprint> sprintOpt = getSprint(sprintId);
        if (sprintOpt.isEmpty()) {
            return Collections.emptyMap();
        }
        
        List<Ticket> stories = getStoriesInSprint(sprintId);
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("sprintId", sprintId);
        stats.put("totalStories", stories.size());
        stats.put("completedStories", stories.stream()
                .mapToLong(story -> story.getStatus().name().equals("DEPLOYED") ? 1 : 0)
                .sum());
        stats.put("inProgressStories", stories.stream()
                .mapToLong(story -> story.getStatus().name().equals("IN_PROGRESS") ? 1 : 0)
                .sum());
        stats.put("openStories", stories.stream()
                .mapToLong(story -> story.getStatus().name().equals("OPEN") ? 1 : 0)
                .sum());
        
        return stats;
    }
    

    public Optional<Sprint> findSprintContainingStory(Long storyId) {
        return sprints.values().stream()
                .filter(sprint -> sprint.getStoryIds().contains(storyId))
                .findFirst();
    }
}
