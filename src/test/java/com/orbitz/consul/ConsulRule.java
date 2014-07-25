package com.orbitz.consul;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class ConsulRule implements TestRule {

    @Override
    public Statement apply(Statement statement, Description description) {
        ConsulRunning annotation = description.getAnnotation(ConsulRunning.class);

        if(annotation == null) {
            return statement;
        }

        try {
            Consul.newClient().agentClient().getAgent();

            return statement;
        } catch (Exception ex) {
            return new Statement() {

                @Override
                public void evaluate() throws Throwable {

                }
            };
        }
    }
}
