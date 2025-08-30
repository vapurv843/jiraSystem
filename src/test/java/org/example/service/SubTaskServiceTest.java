package org.example.service;

import org.example.model.SubTask;
import org.example.model.Ticket;
import org.example.model.TicketStatus;
import org.example.model.TicketType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SubTaskServiceTest {
    
    private SubTaskService subTaskService;
    private TicketService ticketService;
    
    @BeforeEach
    void setUp() {
        ticketService = new TicketService();
        subTaskService = new SubTaskService(ticketService);
    }
    
    @Test
    void testCreateSubTask() {
        Ticket ticket = ticketService.createTicket("Parent Ticket", "Desc", TicketType.STORY, "user");
        
        SubTask subTask = subTaskService.createSubTask(ticket.getId(), "Sub Task", "Sub desc", "assignee");
        
        assertNotNull(subTask);
        assertNotNull(subTask.getId());
        assertEquals(ticket.getId(), subTask.getParentTicketId());
        assertEquals("Sub Task", subTask.getTitle());
        assertEquals("Sub desc", subTask.getDescription());
        assertEquals("assignee", subTask.getAssignee());
        assertEquals(TicketStatus.OPEN, subTask.getStatus());
        
        // Verify sub-task is added to parent ticket
        assertTrue(ticket.getSubTasks().contains(subTask));
    }
    
    @Test
    void testCreateSubTaskForNonExistentTicket() {
        assertThrows(IllegalArgumentException.class, () -> {
            subTaskService.createSubTask(999L, "Sub Task", "Desc", "user");
        });
    }
    
    @Test
    void testGetSubTask() {
        Ticket ticket = ticketService.createTicket("Parent", "Desc", TicketType.STORY, "user");
        SubTask subTask = subTaskService.createSubTask(ticket.getId(), "Sub", "Desc", "user");
        
        Optional<SubTask> retrieved = subTaskService.getSubTask(subTask.getId());
        assertTrue(retrieved.isPresent());
        assertEquals(subTask.getId(), retrieved.get().getId());
        
        Optional<SubTask> notFound = subTaskService.getSubTask(999L);
        assertFalse(notFound.isPresent());
    }
    
    @Test
    void testGetSubTasksForTicket() {
        Ticket ticket = ticketService.createTicket("Parent", "Desc", TicketType.STORY, "user");
        
        SubTask subTask1 = subTaskService.createSubTask(ticket.getId(), "Sub 1", "Desc", "user1");
        SubTask subTask2 = subTaskService.createSubTask(ticket.getId(), "Sub 2", "Desc", "user2");
        
        List<SubTask> subTasks = subTaskService.getSubTasksForTicket(ticket.getId());
        
        assertEquals(2, subTasks.size());
        assertTrue(subTasks.contains(subTask1));
        assertTrue(subTasks.contains(subTask2));
    }
    
    @Test
    void testSubTaskStatusTransitionsForStory() {
        Ticket story = ticketService.createTicket("Story", "Desc", TicketType.STORY, "user");
        SubTask subTask = subTaskService.createSubTask(story.getId(), "Sub", "Desc", "user");
        
        // Test Story sub-task flow: Open -> In Progress -> Testing -> In Review -> Deployed
        assertTrue(subTaskService.updateSubTaskStatus(subTask.getId(), TicketStatus.IN_PROGRESS));
        assertEquals(TicketStatus.IN_PROGRESS, subTask.getStatus());
        
        assertTrue(subTaskService.updateSubTaskStatus(subTask.getId(), TicketStatus.TESTING));
        assertEquals(TicketStatus.TESTING, subTask.getStatus());
        
        assertTrue(subTaskService.updateSubTaskStatus(subTask.getId(), TicketStatus.IN_REVIEW));
        assertEquals(TicketStatus.IN_REVIEW, subTask.getStatus());
        
        assertTrue(subTaskService.updateSubTaskStatus(subTask.getId(), TicketStatus.DEPLOYED));
        assertEquals(TicketStatus.DEPLOYED, subTask.getStatus());
    }
    
    @Test
    void testSubTaskStatusTransitionsForEpic() {
        Ticket epic = ticketService.createTicket("Epic", "Desc", TicketType.EPIC, "user");
        SubTask subTask = subTaskService.createSubTask(epic.getId(), "Sub", "Desc", "user");
        
        // Test Epic sub-task flow: Open -> In Progress -> Completed
        assertTrue(subTaskService.updateSubTaskStatus(subTask.getId(), TicketStatus.IN_PROGRESS));
        assertEquals(TicketStatus.IN_PROGRESS, subTask.getStatus());
        
        assertTrue(subTaskService.updateSubTaskStatus(subTask.getId(), TicketStatus.COMPLETED));
        assertEquals(TicketStatus.COMPLETED, subTask.getStatus());
    }
    
    @Test
    void testSubTaskStatusTransitionsForOnCall() {
        Ticket onCall = ticketService.createTicket("OnCall", "Desc", TicketType.ON_CALL, "user");
        SubTask subTask = subTaskService.createSubTask(onCall.getId(), "Sub", "Desc", "user");
        
        // Test OnCall sub-task flow: Open -> In Progress -> Resolved
        assertTrue(subTaskService.updateSubTaskStatus(subTask.getId(), TicketStatus.IN_PROGRESS));
        assertEquals(TicketStatus.IN_PROGRESS, subTask.getStatus());
        
        assertTrue(subTaskService.updateSubTaskStatus(subTask.getId(), TicketStatus.RESOLVED));
        assertEquals(TicketStatus.RESOLVED, subTask.getStatus());
    }
    
    @Test
    void testInvalidSubTaskStatusTransition() {
        Ticket story = ticketService.createTicket("Story", "Desc", TicketType.STORY, "user");
        SubTask subTask = subTaskService.createSubTask(story.getId(), "Sub", "Desc", "user");
        
        // Invalid transition: OPEN -> TESTING (should go through IN_PROGRESS)
        assertThrows(IllegalArgumentException.class, () -> {
            subTaskService.updateSubTaskStatus(subTask.getId(), TicketStatus.TESTING);
        });
    }
    
    @Test
    void testUpdateSubTaskAssignee() {
        Ticket ticket = ticketService.createTicket("Parent", "Desc", TicketType.STORY, "user");
        SubTask subTask = subTaskService.createSubTask(ticket.getId(), "Sub", "Desc", "user1");
        
        assertTrue(subTaskService.updateSubTaskAssignee(subTask.getId(), "user2"));
        assertEquals("user2", subTask.getAssignee());
    }
    
    @Test
    void testDeleteSubTask() {
        Ticket ticket = ticketService.createTicket("Parent", "Desc", TicketType.STORY, "user");
        SubTask subTask = subTaskService.createSubTask(ticket.getId(), "Sub", "Desc", "user");
        Long subTaskId = subTask.getId();
        
        assertTrue(subTaskService.deleteSubTask(subTaskId));
        assertFalse(subTaskService.getSubTask(subTaskId).isPresent());
        
        // Verify sub-task is removed from parent ticket
        assertFalse(ticket.getSubTasks().contains(subTask));
    }
    
    @Test
    void testTicketCompletionWithSubTasks() {
        Ticket story = ticketService.createTicket("Story", "Desc", TicketType.STORY, "user");
        SubTask subTask1 = subTaskService.createSubTask(story.getId(), "Sub 1", "Desc", "user");
        SubTask subTask2 = subTaskService.createSubTask(story.getId(), "Sub 2", "Desc", "user");
        
        // Initially, not all sub-tasks are completed
        assertFalse(story.areAllSubTasksCompleted());
        
        // Complete first sub-task
        subTaskService.updateSubTaskStatus(subTask1.getId(), TicketStatus.IN_PROGRESS);
        subTaskService.updateSubTaskStatus(subTask1.getId(), TicketStatus.TESTING);
        subTaskService.updateSubTaskStatus(subTask1.getId(), TicketStatus.IN_REVIEW);
        subTaskService.updateSubTaskStatus(subTask1.getId(), TicketStatus.DEPLOYED);
        
        // Still not all completed
        assertFalse(story.areAllSubTasksCompleted());
        
        // Complete second sub-task
        subTaskService.updateSubTaskStatus(subTask2.getId(), TicketStatus.IN_PROGRESS);
        subTaskService.updateSubTaskStatus(subTask2.getId(), TicketStatus.TESTING);
        subTaskService.updateSubTaskStatus(subTask2.getId(), TicketStatus.IN_REVIEW);
        subTaskService.updateSubTaskStatus(subTask2.getId(), TicketStatus.DEPLOYED);
        
        // Now all sub-tasks are completed
        assertTrue(story.areAllSubTasksCompleted());
    }
    
    @Test
    void testCannotCloseTicketWithIncompleteSubTasks() {
        Ticket story = ticketService.createTicket("Story", "Desc", TicketType.STORY, "user");
        SubTask subTask = subTaskService.createSubTask(story.getId(), "Sub", "Desc", "user");
        
        // Move story to IN_PROGRESS
        ticketService.updateTicketStatus(story.getId(), TicketStatus.IN_PROGRESS);
        ticketService.updateTicketStatus(story.getId(), TicketStatus.TESTING);
        ticketService.updateTicketStatus(story.getId(), TicketStatus.IN_REVIEW);
        
        // Try to deploy story with incomplete sub-task
        assertThrows(IllegalStateException.class, () -> {
            ticketService.updateTicketStatus(story.getId(), TicketStatus.DEPLOYED);
        });
        
        // Complete sub-task and try again
        subTaskService.updateSubTaskStatus(subTask.getId(), TicketStatus.IN_PROGRESS);
        subTaskService.updateSubTaskStatus(subTask.getId(), TicketStatus.TESTING);
        subTaskService.updateSubTaskStatus(subTask.getId(), TicketStatus.IN_REVIEW);
        subTaskService.updateSubTaskStatus(subTask.getId(), TicketStatus.DEPLOYED);
        
        // Now it should work
        assertTrue(ticketService.updateTicketStatus(story.getId(), TicketStatus.DEPLOYED));
        assertEquals(TicketStatus.DEPLOYED, story.getStatus());
    }
}
