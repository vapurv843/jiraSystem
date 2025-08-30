package org.example.service;

import org.example.model.Sprint;
import org.example.model.Ticket;
import org.example.model.TicketType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SprintServiceTest {
    
    private SprintService sprintService;
    private TicketService ticketService;
    
    @BeforeEach
    void setUp() {
        ticketService = new TicketService();
        sprintService = new SprintService(ticketService);
    }
    
    @Test
    void testCreateSprint() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusWeeks(2);
        
        Sprint sprint = sprintService.createSprint("Sprint 1", "First sprint", start, end);
        
        assertNotNull(sprint);
        assertNotNull(sprint.getId());
        assertEquals("Sprint 1", sprint.getName());
        assertEquals("First sprint", sprint.getDescription());
        assertEquals(start, sprint.getStartDate());
        assertEquals(end, sprint.getEndDate());
        assertFalse(sprint.isActive());
    }
    
    @Test
    void testCreateSprintWithInvalidDates() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.minusDays(1); // End before start
        
        assertThrows(IllegalArgumentException.class, () -> {
            sprintService.createSprint("Invalid Sprint", "Description", start, end);
        });
    }
    
    @Test
    void testStartSprint() {
        Sprint sprint = sprintService.createSprint("Sprint 1", "Desc", 
                LocalDateTime.now(), LocalDateTime.now().plusWeeks(2));
        
        assertTrue(sprintService.startSprint(sprint.getId()));
        assertTrue(sprint.isActive());
        
        Optional<Sprint> activeSprint = sprintService.getActiveSprint();
        assertTrue(activeSprint.isPresent());
        assertEquals(sprint.getId(), activeSprint.get().getId());
    }
    
    @Test
    void testCannotStartMultipleSprints() {
        Sprint sprint1 = sprintService.createSprint("Sprint 1", "Desc", 
                LocalDateTime.now(), LocalDateTime.now().plusWeeks(2));
        Sprint sprint2 = sprintService.createSprint("Sprint 2", "Desc", 
                LocalDateTime.now(), LocalDateTime.now().plusWeeks(2));
        
        assertTrue(sprintService.startSprint(sprint1.getId()));
        
        assertThrows(IllegalStateException.class, () -> {
            sprintService.startSprint(sprint2.getId());
        });
    }
    
    @Test
    void testEndActiveSprint() {
        Sprint sprint = sprintService.createSprint("Sprint 1", "Desc", 
                LocalDateTime.now(), LocalDateTime.now().plusWeeks(2));
        
        sprintService.startSprint(sprint.getId());
        assertTrue(sprint.isActive());
        
        assertTrue(sprintService.endActiveSprint());
        assertFalse(sprint.isActive());
        assertFalse(sprintService.getActiveSprint().isPresent());
    }
    
    @Test
    void testAddStoryToSprint() {
        Sprint sprint = sprintService.createSprint("Sprint 1", "Desc", 
                LocalDateTime.now(), LocalDateTime.now().plusWeeks(2));
        Ticket story = ticketService.createTicket("Story 1", "Desc", TicketType.STORY, "user");
        
        assertTrue(sprintService.addStoryToSprint(sprint.getId(), story.getId()));
        assertTrue(sprint.getStoryIds().contains(story.getId()));
        
        List<Ticket> sprintStories = sprintService.getStoriesInSprint(sprint.getId());
        assertEquals(1, sprintStories.size());
        assertEquals(story.getId(), sprintStories.get(0).getId());
    }
    
    @Test
    void testCannotAddNonStoryToSprint() {
        Sprint sprint = sprintService.createSprint("Sprint 1", "Desc", 
                LocalDateTime.now(), LocalDateTime.now().plusWeeks(2));
        Ticket epic = ticketService.createTicket("Epic 1", "Desc", TicketType.EPIC, "user");
        
        assertThrows(IllegalArgumentException.class, () -> {
            sprintService.addStoryToSprint(sprint.getId(), epic.getId());
        });
    }
    
    @Test
    void testCannotAddStoryToMultipleSprints() {
        Sprint sprint1 = sprintService.createSprint("Sprint 1", "Desc", 
                LocalDateTime.now(), LocalDateTime.now().plusWeeks(2));
        Sprint sprint2 = sprintService.createSprint("Sprint 2", "Desc", 
                LocalDateTime.now(), LocalDateTime.now().plusWeeks(2));
        Ticket story = ticketService.createTicket("Story 1", "Desc", TicketType.STORY, "user");
        
        assertTrue(sprintService.addStoryToSprint(sprint1.getId(), story.getId()));
        
        assertThrows(IllegalStateException.class, () -> {
            sprintService.addStoryToSprint(sprint2.getId(), story.getId());
        });
    }
    
    @Test
    void testRemoveStoryFromSprint() {
        Sprint sprint = sprintService.createSprint("Sprint 1", "Desc", 
                LocalDateTime.now(), LocalDateTime.now().plusWeeks(2));
        Ticket story = ticketService.createTicket("Story 1", "Desc", TicketType.STORY, "user");
        
        sprintService.addStoryToSprint(sprint.getId(), story.getId());
        assertTrue(sprint.getStoryIds().contains(story.getId()));
        
        assertTrue(sprintService.removeStoryFromSprint(sprint.getId(), story.getId()));
        assertFalse(sprint.getStoryIds().contains(story.getId()));
    }
    
    @Test
    void testAddToCurrentSprint() {
        Sprint sprint = sprintService.createSprint("Sprint 1", "Desc", 
                LocalDateTime.now(), LocalDateTime.now().plusWeeks(2));
        Ticket story = ticketService.createTicket("Story 1", "Desc", TicketType.STORY, "user");
        
        sprintService.startSprint(sprint.getId());
        
        assertTrue(sprintService.addStoryToCurrentSprint(story.getId()));
        assertTrue(sprint.getStoryIds().contains(story.getId()));
    }
    
    @Test
    void testCannotAddToCurrentSprintWhenNoneActive() {
        Ticket story = ticketService.createTicket("Story 1", "Desc", TicketType.STORY, "user");
        
        assertThrows(IllegalStateException.class, () -> {
            sprintService.addStoryToCurrentSprint(story.getId());
        });
    }
    
    @Test
    void testGetSprintStatistics() {
        Sprint sprint = sprintService.createSprint("Sprint 1", "Desc", 
                LocalDateTime.now(), LocalDateTime.now().plusWeeks(2));
        
        Ticket story1 = ticketService.createTicket("Story 1", "Desc", TicketType.STORY, "user");
        Ticket story2 = ticketService.createTicket("Story 2", "Desc", TicketType.STORY, "user");
        
        sprintService.addStoryToSprint(sprint.getId(), story1.getId());
        sprintService.addStoryToSprint(sprint.getId(), story2.getId());
        
        var stats = sprintService.getSprintStatistics(sprint.getId());
        
        assertEquals(sprint.getId(), stats.get("sprintId"));
        assertEquals(2, stats.get("totalStories"));
        assertEquals(2L, stats.get("openStories"));
        assertEquals(0L, stats.get("inProgressStories"));
        assertEquals(0L, stats.get("completedStories"));
    }
    
    @Test
    void testDeleteSprint() {
        Sprint sprint = sprintService.createSprint("Sprint 1", "Desc", 
                LocalDateTime.now(), LocalDateTime.now().plusWeeks(2));
        Long sprintId = sprint.getId();
        
        assertTrue(sprintService.deleteSprint(sprintId));
        assertFalse(sprintService.getSprint(sprintId).isPresent());
    }
    
    @Test
    void testCannotDeleteActiveSprint() {
        Sprint sprint = sprintService.createSprint("Sprint 1", "Desc", 
                LocalDateTime.now(), LocalDateTime.now().plusWeeks(2));
        
        sprintService.startSprint(sprint.getId());
        
        assertThrows(IllegalStateException.class, () -> {
            sprintService.deleteSprint(sprint.getId());
        });
    }
}
