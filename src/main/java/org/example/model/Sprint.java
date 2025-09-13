package org.example.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Sprint {
    private static long counter = 1;

    @EqualsAndHashCode.Include
    private final Long id;

    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @ToString.Exclude
    private List<Long> storyIds = new ArrayList<>();

    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Sprint(String name, String description, LocalDateTime startDate, LocalDateTime endDate) {
        this.id = counter++;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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
        return String.format(
                "Sprint{id=%d, name='%s', active=%s, stories=%d}",
                id, name, active, storyIds.size()
        );
    }
}
