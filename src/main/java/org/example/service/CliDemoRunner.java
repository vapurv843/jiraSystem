package org.example.service;

import java.util.Scanner;

public class CliDemoRunner implements DemoRunner {
    private final TicketService ticketService;
    private final SubTaskService subTaskService;
    private final SprintService sprintService;
    private final Scanner scanner;

    public CliDemoRunner(TicketService ticketService, SubTaskService subTaskService, SprintService sprintService, Scanner scanner) {
        this.ticketService = ticketService;
        this.subTaskService = subTaskService;
        this.sprintService = sprintService;
        this.scanner = scanner;
    }

    @Override
    public void run() {
        runCliDemoclass cli = new runCliDemoclass(ticketService, subTaskService, sprintService, scanner);
        cli.runCliDemo();
    }
}
