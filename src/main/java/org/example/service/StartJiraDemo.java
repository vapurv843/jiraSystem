package org.example.service;

import org.example.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class StartJiraDemo {

    private final TicketService ticketService;
    private final SubTaskService subTaskService;
    private final SprintService sprintService;

    public StartJiraDemo() {
        this.ticketService = new TicketService();
        this.subTaskService = new SubTaskService(ticketService);
        this.sprintService = new SprintService(ticketService);
    }

    public void startJira() {
        System.out.println("=== Ticket System start ===\n");

        try {
            showTicketCreation();
            showStatusTransitions();
            showSubTaskManagement();
            showSprintManagement();
            showValidationRules();
            showSystemState();
        } catch (Exception e) {
            System.err.println("Something went wrong: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n=== Demo Finished ===");
    }

    private void showTicketCreation() {
        System.out.println("1. Creating tickets...\n");

        Ticket story1 = ticketService.createTicket(
                "Login Feature",
                "Implement login functionality for users",
                TicketType.STORY,
                "Apurv"
        );

        Ticket story2 = ticketService.createTicket(
                "Customer Login Feature",
                "Implement login functionality for customers",
                TicketType.STORY,
                "Verma"
        );

        Ticket epic1 = ticketService.createTicket(
                "User Authentication",
                "Complete user authentication flow",
                TicketType.EPIC,
                "ApurvBbr"
        );

        Ticket onCall1 = ticketService.createTicket(
                "Fix Production Bug",
                "Resolve urgent production issue",
                TicketType.ON_CALL,
                "Apurv Verma"
        );

        System.out.println("Created tickets:");
        System.out.println("* Story 1: " + story1.getDescription());
        System.out.println("* Story 2: " + story2.getDescription());
        System.out.println("* Epic: " + epic1.getDescription());
        System.out.println("* On-Call: " + onCall1.getDescription());
        System.out.println();
    }

    private void showStatusTransitions() {
        System.out.println("2. Demonstrating status transitions...\n");

        List<Ticket> stories = ticketService.getTicketsByType(TicketType.STORY);
        if (!stories.isEmpty()) {
            Ticket story = stories.get(0);
            System.out.println("Updating Story: " + story.getTitle());

            ticketService.updateTicketStatus(story.getId(), TicketStatus.IN_PROGRESS);
            System.out.println(" Status: " + story.getStatus());

            ticketService.updateTicketStatus(story.getId(), TicketStatus.TESTING);
            System.out.println(" Status: " + story.getStatus());

            ticketService.updateTicketStatus(story.getId(), TicketStatus.IN_REVIEW);
            System.out.println(" Status: " + story.getStatus());
        }

        List<Ticket> epics = ticketService.getTicketsByType(TicketType.EPIC);
        if (!epics.isEmpty()) {
            Ticket epic = epics.get(0);
            System.out.println("\nUpdating Epic: " + epic.getTitle());

            ticketService.updateTicketStatus(epic.getId(), TicketStatus.IN_PROGRESS);
            System.out.println("→ Status: " + epic.getStatus());
        }

        List<Ticket> onCalls = ticketService.getTicketsByType(TicketType.ON_CALL);
        if (!onCalls.isEmpty()) {
            Ticket onCall = onCalls.get(0);
            System.out.println("\nUpdating On-Call Ticket: " + onCall.getTitle());

            ticketService.updateTicketStatus(onCall.getId(), TicketStatus.IN_PROGRESS);
            System.out.println(" Status: " + onCall.getStatus());

            ticketService.updateTicketStatus(onCall.getId(), TicketStatus.RESOLVED);
            System.out.println(" Status: " + onCall.getStatus());
        }

        System.out.println();
    }

    private void showSubTaskManagement() {
        System.out.println("3. Managing sub-tasks...\n");

        List<Ticket> stories = ticketService.getTicketsByType(TicketType.STORY);
        if (!stories.isEmpty()) {
            Ticket story = stories.get(0);
            System.out.println("Adding sub-tasks to story: " + story.getTitle());

            SubTask subTask1 = subTaskService.createSubTask(
                    story.getId(),
                    "Design UI Page",
                    "Create UI design for login",
                    "sm1"
            );

            SubTask subTask2 = subTaskService.createSubTask(
                    story.getId(),
                    "Implement Login Backend",
                    "Integrate Firebase authentication",
                    "sm2"
            );

            System.out.println(" Created Sub-task: " + subTask1.getTitle());
            System.out.println(" Created Sub-task: " + subTask2.getTitle());

            subTaskService.updateSubTaskStatus(subTask1.getId(), TicketStatus.IN_PROGRESS);
            subTaskService.updateSubTaskStatus(subTask1.getId(), TicketStatus.TESTING);
            subTaskService.updateSubTaskStatus(subTask1.getId(), TicketStatus.IN_REVIEW);
            subTaskService.updateSubTaskStatus(subTask1.getId(), TicketStatus.DEPLOYED);
            System.out.println("Completed Sub-task: " + subTask1.getTitle());

            subTaskService.updateSubTaskStatus(subTask2.getId(), TicketStatus.IN_PROGRESS);
            System.out.println("In Progress Sub-task: " + subTask2.getTitle());

            System.out.println("All sub-tasks completed? " + story.areAllSubTasksCompleted());
        }

        System.out.println();
    }

    private void showSprintManagement() {
        System.out.println("4. Managing sprints...\n");

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(2);

        Sprint sprint = sprintService.createSprint(
                "2025-September-13",
                "Development Sprint 1",
                startDate,
                endDate
        );

        System.out.println("Created sprint: " + sprint.getName());

        sprintService.startSprint(sprint.getId());
        System.out.println("Sprint started: " + sprint.getId());

        List<Ticket> stories = ticketService.getTicketsByType(TicketType.STORY);
        for (Ticket story : stories) {
            sprintService.addStoryToSprint(sprint.getId(), story.getId());
            System.out.println("Added Story: " + story.getTitle());
        }

        System.out.println("Sprint Statistics: " + sprintService.getSprintStatistics(sprint.getId()));
        System.out.println();
    }

    private void showValidationRules() {
        System.out.println("5. Checking validation rules...\n");

        try {
            List<Ticket> epics = ticketService.getTicketsByType(TicketType.EPIC);
            if (!epics.isEmpty()) {
                Optional<Sprint> activeSprint = sprintService.getActiveSprint();
                activeSprint.ifPresent(sprint ->
                        sprintService.addStoryToSprint(sprint.getId(), epics.get(0).getId())
                );
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Rule triggered: " + e.getMessage());
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
            System.out.println("Rule triggered: " + e.getMessage());
        }

        try {
            List<Ticket> epics = ticketService.getTicketsByType(TicketType.EPIC);
            if (!epics.isEmpty()) {
                ticketService.updateTicketStatus(epics.get(0).getId(), TicketStatus.TESTING);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Rule triggered: " + e.getMessage());
        }

        System.out.println();
    }

    private void showSystemState() {
        System.out.println("6. Current system state...\n");

        System.out.println("All Tickets:");
        ticketService.getAllTickets().forEach(ticket -> {
            System.out.println("- " + ticket);
            List<SubTask> subTasks = subTaskService.getSubTasksForTicket(ticket.getId());
            subTasks.forEach(subTask -> System.out.println("  * " + subTask));
        });

        System.out.println("\nActive Sprint:");
        Optional<Sprint> activeSprint = sprintService.getActiveSprint();
        if (activeSprint.isPresent()) {
            Sprint sprint = activeSprint.get();
            System.out.println("- " + sprint);
            List<Ticket> sprintStories = sprintService.getStoriesInCurrentSprint();
            sprintStories.forEach(story -> System.out.println("  * " + story));
        } else {
            System.out.println("No active sprint found.");
        }

        System.out.println();
    }
}
