package org.example.service;

import org.example.model.*;
import java.time.LocalDateTime;
import java.util.*;

public class runCliDemoclass {
    private final TicketService ticketService;
    private final SubTaskService subTaskService;
    private final SprintService sprintService;
    private final Scanner scanner;
    public runCliDemoclass(TicketService ticketService, SubTaskService subTaskService, SprintService sprintService, Scanner scanner) {
        this.ticketService = ticketService;
        this.subTaskService = subTaskService;
        this.sprintService = sprintService;
        this.scanner = scanner;
    }
    public void runCliDemo() {


        System.out.println("=== Jira System ===");

        boolean running = true;
        while (running) {
            System.out.println("\nChoose an option:");
            System.out.println("1. Create Ticket");
            System.out.println("2. Update Ticket Status");
            System.out.println("3. Add SubTask");
            System.out.println("4. Manage Sprint");
            System.out.println("5. Show System State");
            System.out.println("0. Exit");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> createTicket();
                case 2 -> updateTicketStatus();
                case 3 -> addSubTask();
                case 4 -> manageSprint();
                case 5 -> showSystemState();
                case 0 -> running = false;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void createTicket() {
        System.out.print("Title: ");
        String title = scanner.nextLine();

        System.out.print("Description: ");
        String desc = scanner.nextLine();

        System.out.print("Assignee: ");
        String assignee = scanner.nextLine();

        System.out.print("Type (STORY, EPIC, ON_CALL): ");
        TicketType type = TicketType.valueOf(scanner.nextLine().toUpperCase());

        Ticket ticket = ticketService.createTicket(title, desc, type, assignee);
        System.out.println("Created: " + ticket);
    }

    private void updateTicketStatus() {
        System.out.print("Ticket ID: ");
        Long id = scanner.nextLong();
        scanner.nextLine();
        TicketStatus status = null;


        System.out.print("New Status (e.g., OPEN, IN_PROGRESS, CLOSED): ");
        String input = scanner.nextLine().toUpperCase();
        try {
            status = TicketStatus.valueOf(input);

            try {
                ticketService.updateTicketStatus(id, status);
                System.out.println(" Updated ticket " + id + " to " + status);
            } catch (Exception e) {
                System.out.println(" Error while updating ticket: " + e.getMessage());
            }

        } catch (IllegalArgumentException e) {
            System.out.println("Invalid status: " + input +
                    ". Allowed values: " + Arrays.toString(TicketStatus.values()));
        }
    }

    private void addSubTask() {
        System.out.print("Parent Ticket ID: ");
        Long parentId = scanner.nextLong();
        scanner.nextLine();

        System.out.print("Subtask Title: ");
        String title = scanner.nextLine();

        System.out.print("Description: ");
        String desc = scanner.nextLine();

        System.out.print("Assignee: ");
        String assignee = scanner.nextLine();

        SubTask subTask = subTaskService.createSubTask(parentId, title, desc, assignee);
        System.out.println("Created SubTask: " + subTask);
    }

    private void manageSprint() {
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(2);
        Sprint sprint = sprintService.createSprint("Sprint-" + startDate.toLocalDate(), "Demo Sprint", startDate, endDate);

        sprintService.startSprint(sprint.getId());
        System.out.println("Started sprint: " + sprint);

        ticketService.getTicketsByType(TicketType.STORY)
                .forEach(story -> sprintService.addStoryToSprint(sprint.getId(), story.getId()));
    }

    private void showSystemState() {
        System.out.println("\nAll Tickets:");
        ticketService.getAllTickets().forEach(System.out::println);

        System.out.println("\nActive Sprint:");
        sprintService.getActiveSprint().ifPresentOrElse(
                s -> {
                    System.out.println(s);
                    sprintService.getStoriesInCurrentSprint().forEach(story -> System.out.println(" * " + story));
                },
                () -> System.out.println("No active sprint.")
        );
    }
}
