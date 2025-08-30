package org.example.service;

import org.example.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;


public class JiraSystem {
    private final TicketService ticketService;
    private final SubTaskService subTaskService;
    private final SprintService sprintService;
    private final Scanner scanner;
    
    public JiraSystem() {
        this.ticketService = new TicketService();
        this.subTaskService = new SubTaskService(ticketService);
        this.sprintService = new SprintService(ticketService);
        this.scanner = new Scanner(System.in);
    }

    public void startJira() {
        System.out.println("JIRA System Demo");
        
        try {
            showTicketCreation();
            showStatusTransitions();
            showSubTaskManagement();
            showSprintManagement();
            showValidationRules();
            showSystemState();
            
        } catch (Exception e) {
            System.err.println("error occurrd: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== Demo Complete ===");
    }
    
    private void showTicketCreation() {
        System.out.println("1.Creating tickets...\n");
        
        Ticket story1 = ticketService.createTicket("implement  Login Feature", "implement  Login Feature", TicketType.STORY, "Apurv");
        Ticket story2 = ticketService.createTicket("implement  Login Feature cust", "implement  Login Feature cust", TicketType.STORY, "Verma");
        
        Ticket epic1 = ticketService.createTicket("User Authentication", "Complete User Authentication functionality", TicketType.EPIC, "ApurvBbr");
        
        Ticket oncall1 = ticketService.createTicket("Fix production Bug", "Fix production Bug", TicketType.ON_CALL, "Apurv verma");
        
        System.out.println("Created tickets are following:");
        System.out.println("Story 1 is   " + story1.getDescription());
        System.out.println("story2 is  " + story2.getDescription());
        System.out.println("epic1 is  " + epic1.getDescription());
        System.out.println("oncall ticket is  " + oncall1.getDescription());
        System.out.println();
    }
    
    private void showStatusTransitions() {
        System.out.println("2.Showing status transitions...\n");
        
        List<Ticket> stories = ticketService.getTicketsByType(TicketType.STORY);
        if (!stories.isEmpty()) {
            Ticket story = stories.get(0);
            System.out.println("Story for: " + story.getTitle());
            
            ticketService.updateTicketStatus(story.getId(), TicketStatus.IN_PROGRESS);
            System.out.println(" Moved to IN_PROGRESS: " + story.getStatus());
            
            ticketService.updateTicketStatus(story.getId(), TicketStatus.TESTING);
            System.out.println("- Moved to TESTING: " + story.getStatus());
            
            ticketService.updateTicketStatus(story.getId(), TicketStatus.IN_REVIEW);
            System.out.println("- Moved to IN_REVIEW: " + story.getStatus());
        }
        
        List<Ticket> epics = ticketService.getTicketsByType(TicketType.EPIC);
        if (!epics.isEmpty()) {
            Ticket epic = epics.get(0);
            System.out.println("\nEpic updates  for: " + epic.getTitle());
            
            ticketService.updateTicketStatus(epic.getId(), TicketStatus.IN_PROGRESS);
            System.out.println(" Moved to IN_PROGRESS: " + epic.getStatus());
        }
        
        List<Ticket> oncalls = ticketService.getTicketsByType(TicketType.ON_CALL);
        if (!oncalls.isEmpty()) {
            Ticket oncall = oncalls.get(0);
            System.out.println("\nOn-call updates  for: " + oncall.getTitle());
            
            ticketService.updateTicketStatus(oncall.getId(), TicketStatus.IN_PROGRESS);
            System.out.println("- Moved to IN_PROGRESS: " + oncall.getStatus());
            
            ticketService.updateTicketStatus(oncall.getId(), TicketStatus.RESOLVED);
            System.out.println("- Moved to RESOLVED: " + oncall.getStatus());
        }
        
        System.out.println();
    }
    
    private void showSubTaskManagement() {
        System.out.println("3.Showing sub-task management...\n");
        
        List<Ticket> stories = ticketService.getTicketsByType(TicketType.STORY);
        if (!stories.isEmpty()) {
            Ticket story = stories.get(0);
            System.out.println("Adding sub-tasks to story: " + story.getTitle());
            
            SubTask subTask1 = subTaskService.createSubTask(story.getId(), "Design UI page", "Design UI backend ", "sm1");
            SubTask subTask2 = subTaskService.createSubTask(story.getId(), "Implement login feature", "create login firebase", "sm2");
            
            System.out.println("Created sub-task: " + subTask1.getTitle());
            System.out.println("Created sub-task: " + subTask2.getTitle());
            
            subTaskService.updateSubTaskStatus(subTask1.getId(), TicketStatus.IN_PROGRESS);
            subTaskService.updateSubTaskStatus(subTask1.getId(), TicketStatus.TESTING);
            subTaskService.updateSubTaskStatus(subTask1.getId(), TicketStatus.IN_REVIEW);
            subTaskService.updateSubTaskStatus(subTask1.getId(), TicketStatus.DEPLOYED);
            System.out.println("Completed sub-task: " + subTask1.getTitle());
            
            subTaskService.updateSubTaskStatus(subTask2.getId(), TicketStatus.IN_PROGRESS);
            System.out.println(" Started sub-task: " + subTask2.getTitle());
            
            System.out.println("Story sub-tasks completion status: " + story.areAllSubTasksCompleted());
        }
        
        System.out.println();
    }
    
    private void showSprintManagement() {
        System.out.println("4. Showing sprint management...\n");
        
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(2);
        Sprint sprint = sprintService.createSprint("2025.august.1", "Dev sprint one ", startDate, endDate);
        System.out.println("Created sprint: " + sprint.getName());

        sprintService.startSprint(sprint.getId());
        System.out.println("Started sprint: " + sprint.getId());
        
        List<Ticket> stories = ticketService.getTicketsByType(TicketType.STORY);
        for (Ticket story : stories) {
            sprintService.addStoryToSprint(sprint.getId(), story.getId());
            System.out.println("Added story to sprint: " + story.getTitle());
        }
        
        System.out.println("Sprint dataPoints: " + sprintService.getSprintStatistics(sprint.getId()));
        
        System.out.println();
    }
    
    private void showValidationRules() {
        System.out.println("5. Showing validation rules...\n");
        
        try {
            List<Ticket> epics = ticketService.getTicketsByType(TicketType.EPIC);
            if (!epics.isEmpty()) {
                Optional<Sprint> activeSprint = sprintService.getActiveSprint();
                if (activeSprint.isPresent()) {
                    sprintService.addStoryToSprint(activeSprint.get().getId(), epics.get(0).getId());
                }
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Rule is : " + e.getMessage());
        }
        
        try {
            List<Ticket> stories = ticketService.getTicketsByType(TicketType.STORY);
            if (!stories.isEmpty()) {
                Ticket story = stories.get(0);
                if (!story.areAllSubTasksCompleted()) {
                    ticketService.updateTicketStatus(story.getId(), TicketStatus.DEPLOYED);
                }
            }
        } catch (IllegalStateException e) {
            System.out.println("Rule is : " + e.getMessage());
        }
        
        try {
            List<Ticket> epics = ticketService.getTicketsByType(TicketType.EPIC);
            if (!epics.isEmpty()) {
                ticketService.updateTicketStatus(epics.get(0).getId(), TicketStatus.TESTING);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Rule is : " + e.getMessage());
        }
        
        System.out.println();
    }
    
    private void showSystemState() {
        System.out.println("6. Current system state...\n");
        
        System.out.println("All Tickets:");
        ticketService.getAllTickets().forEach(ticket -> {
            System.out.println("- " + ticket);
            List<SubTask> subTasks = subTaskService.getSubTasksForTicket(ticket.getId());
            subTasks.forEach(subTask -> System.out.println("  └─ " + subTask));
        });
        
        System.out.println("\nActive Sprint:");
        Optional<Sprint> activeSprint = sprintService.getActiveSprint();
        if (activeSprint.isPresent()) {
            Sprint sprint = activeSprint.get();
            System.out.println("- " + sprint);
            List<Ticket> sprintStories = sprintService.getStoriesInCurrentSprint();
            sprintStories.forEach(story -> System.out.println("  └─ " + story));
        } else {
            System.out.println(" No active sprint");
        }
        
        System.out.println();
    }
}
