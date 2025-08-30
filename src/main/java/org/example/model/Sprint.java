package org.example.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class Sprint {
    private static long counter = 1;
    
    private final Long id;
    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<Long> storyIds;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public Sprint(String name, String description, LocalDateTime startDate, LocalDateTime endDate) {
        this.id = counter++;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.storyIds = new ArrayList<>();
        this.active = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public LocalDateTime getStartDate() { return startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public List<Long> getStoryIds() { return new ArrayList<>(storyIds); }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    

    
    public void setActive(boolean active) {
        this.active = active;
        updateTimestamp();
    }
    
    public void addStory(Long storyId) {
        if (!storyIds.contains(storyId)) {
            storyIds.add(storyId);
            updateTimestamp();
        }
    }
    
    public void removeStory(Long storyId) {
        storyIds.remove(storyId);
        updateTimestamp();
    }
    
    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return String.format("Sprint{id=%d, name='%s', active=%s, stories=%d}",
                           id, name, active, storyIds.size());
    }


}
