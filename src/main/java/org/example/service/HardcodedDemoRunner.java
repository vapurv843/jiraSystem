package org.example.service;

public class HardcodedDemoRunner implements DemoRunner {
    @Override
    public void run() {
        StartJiraDemo demo = new StartJiraDemo();
        demo.startJira();
    }
}
