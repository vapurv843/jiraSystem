package org.example;

import org.example.service.JiraSystem;

public class Main {
    public static void main(String[] args) {
        JiraSystem jiraSystem = new JiraSystem();
        jiraSystem.startJira();
    }
}