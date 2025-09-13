package org.example.service;

import org.example.model.demoType;
import java.util.Scanner;

public class DemoRunnerFactory {
    private final TicketService ticketService;
    private final SubTaskService subTaskService;
    private final SprintService sprintService;
    private final Scanner scanner;

    public DemoRunnerFactory(TicketService ticketService, SubTaskService subTaskService,
                             SprintService sprintService, Scanner scanner) {
        this.ticketService = ticketService;
        this.subTaskService = subTaskService;
        this.sprintService = sprintService;
        this.scanner = scanner;
    }

    public DemoRunner getRunner(demoType type) {
        return switch (type) {
            case CLI -> new CliDemoRunner(ticketService, subTaskService, sprintService, scanner);
            case HARDCODED -> new HardcodedDemoRunner();
        };
    }
}
