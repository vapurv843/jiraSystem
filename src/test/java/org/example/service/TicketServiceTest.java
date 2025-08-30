package org.example.service;

import org.example.model.Ticket;
import org.example.model.TicketStatus;
import org.example.model.TicketType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TicketServiceTest {
    
    private TicketService ticketService;
    
    @BeforeEach
    void setUp() {
        ticketService = new TicketService();
    }
    
    @Test
    void testCreateTicket() {
        Ticket ticket = ticketService.createTicket("Test Story", "Description", TicketType.STORY, "john.doe");
        
        assertNotNull(ticket);
        assertNotNull(ticket.getId());
        assertEquals("Test Story", ticket.getTitle());
        assertEquals("Description", ticket.getDescription());
        assertEquals(TicketType.STORY, ticket.getType());
        assertEquals("john.doe", ticket.getAssignee());
        assertEquals(TicketStatus.OPEN, ticket.getStatus());
    }
    
    @Test
    void testGetTicket() {
        Ticket ticket = ticketService.createTicket("Test", "Desc", TicketType.STORY, "user");
        
        Optional<Ticket> retrieved = ticketService.getTicket(ticket.getId());
        assertTrue(retrieved.isPresent());
        assertEquals(ticket.getId(), retrieved.get().getId());
        
        Optional<Ticket> notFound = ticketService.getTicket(999L);
        assertFalse(notFound.isPresent());
    }
    
    @Test
    void testGetTicketsByType() {
        ticketService.createTicket("Story 1", "Desc", TicketType.STORY, "user1");
        ticketService.createTicket("Story 2", "Desc", TicketType.STORY, "user2");
        ticketService.createTicket("Epic 1", "Desc", TicketType.EPIC, "user3");
        
        List<Ticket> stories = ticketService.getTicketsByType(TicketType.STORY);
        List<Ticket> epics = ticketService.getTicketsByType(TicketType.EPIC);
        
        assertEquals(2, stories.size());
        assertEquals(1, epics.size());
        assertTrue(stories.stream().allMatch(t -> t.getType() == TicketType.STORY));
        assertTrue(epics.stream().allMatch(t -> t.getType() == TicketType.EPIC));
    }
    
    @Test
    void testStoryStatusTransitions() {
        Ticket story = ticketService.createTicket("Story", "Desc", TicketType.STORY, "user");
        
        // Valid transitions
        assertTrue(ticketService.updateTicketStatus(story.getId(), TicketStatus.IN_PROGRESS));
        assertEquals(TicketStatus.IN_PROGRESS, story.getStatus());
        
        assertTrue(ticketService.updateTicketStatus(story.getId(), TicketStatus.TESTING));
        assertEquals(TicketStatus.TESTING, story.getStatus());
        
        assertTrue(ticketService.updateTicketStatus(story.getId(), TicketStatus.IN_REVIEW));
        assertEquals(TicketStatus.IN_REVIEW, story.getStatus());
        
        assertTrue(ticketService.updateTicketStatus(story.getId(), TicketStatus.DEPLOYED));
        assertEquals(TicketStatus.DEPLOYED, story.getStatus());
    }
    
    @Test
    void testInvalidStatusTransition() {
        Ticket story = ticketService.createTicket("Story", "Desc", TicketType.STORY, "user");
        
        // Invalid transition: OPEN -> TESTING (should go through IN_PROGRESS)
        assertThrows(IllegalArgumentException.class, () -> {
            ticketService.updateTicketStatus(story.getId(), TicketStatus.TESTING);
        });
    }
    
    @Test
    void testEpicStatusTransitions() {
        Ticket epic = ticketService.createTicket("Epic", "Desc", TicketType.EPIC, "user");
        
        assertTrue(ticketService.updateTicketStatus(epic.getId(), TicketStatus.IN_PROGRESS));
        assertEquals(TicketStatus.IN_PROGRESS, epic.getStatus());
        
        assertTrue(ticketService.updateTicketStatus(epic.getId(), TicketStatus.COMPLETED));
        assertEquals(TicketStatus.COMPLETED, epic.getStatus());
    }
    
    @Test
    void testOnCallStatusTransitions() {
        Ticket onCall = ticketService.createTicket("OnCall", "Desc", TicketType.ON_CALL, "user");
        
        assertTrue(ticketService.updateTicketStatus(onCall.getId(), TicketStatus.IN_PROGRESS));
        assertEquals(TicketStatus.IN_PROGRESS, onCall.getStatus());
        
        assertTrue(ticketService.updateTicketStatus(onCall.getId(), TicketStatus.RESOLVED));
        assertEquals(TicketStatus.RESOLVED, onCall.getStatus());
    }
    
    @Test
    void testAddComment() {
        Ticket ticket = ticketService.createTicket("Test", "Desc", TicketType.STORY, "user");
        
        assertTrue(ticketService.addComment(ticket.getId(), "First comment"));
        assertTrue(ticketService.addComment(ticket.getId(), "Second comment"));
        
        assertEquals(2, ticket.getComments().size());
        assertTrue(ticket.getComments().contains("First comment"));
        assertTrue(ticket.getComments().contains("Second comment"));
    }
    
    @Test
    void testUpdateAssignee() {
        Ticket ticket = ticketService.createTicket("Test", "Desc", TicketType.STORY, "user1");
        
        assertTrue(ticketService.updateTicketAssignee(ticket.getId(), "user2"));
        assertEquals("user2", ticket.getAssignee());
    }
    
    @Test
    void testDeleteTicket() {
        Ticket ticket = ticketService.createTicket("Test", "Desc", TicketType.STORY, "user");
        Long ticketId = ticket.getId();
        
        assertTrue(ticketService.deleteTicket(ticketId));
        assertFalse(ticketService.getTicket(ticketId).isPresent());
        
        // Try to delete non-existent ticket
        assertFalse(ticketService.deleteTicket(999L));
    }
}
