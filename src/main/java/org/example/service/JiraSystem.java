package org.example.service;

import org.example.model.demoType;
import java.util.*;

public class JiraSystem {
    private final DemoRunnerFactory factory;
    private final Scanner scanner;

    public JiraSystem() {
        TicketService ticketService = new TicketService();
        SubTaskService subTaskService = new SubTaskService(ticketService);
        SprintService sprintService = new SprintService(ticketService);
        this.scanner = new Scanner(System.in);
        this.factory = new DemoRunnerFactory(ticketService, subTaskService, sprintService, scanner);
    }

    public void runType() {
        demoType selectedDemoType = null;

        while (selectedDemoType == null) {
            System.out.println("Enter the way that demo should be presented (CLI or HARDCODED): ");
            String input = scanner.nextLine().toUpperCase();

            try {
                selectedDemoType = demoType.valueOf(input);
                DemoRunner runner = factory.getRunner(selectedDemoType);
                runner.run();
            } catch (IllegalArgumentException e) {
                System.out.println(" Invalid demo type: " + input +
                        ". Allowed values: " + Arrays.toString(demoType.values()));
            }
        }
    }
}
