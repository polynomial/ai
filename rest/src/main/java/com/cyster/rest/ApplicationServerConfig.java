package com.cyster.rest;

import java.util.Arrays;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

public class ApplicationServerConfig {
    private ApplicationContext context;

    public ApplicationServerConfig(ApplicationContext context) {
        this.context = context;
    }

    public void dumpBeans() {
        System.out.println("Spring Boot Beans:");

        String[] beanNames = context.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            System.out.println(beanName);
        }
    }

    public void dumpEnvironment() {
        Environment environment = context.getEnvironment();

        System.out.println("Environment:");
        for (PropertySource<?> propertySource : ((AbstractEnvironment) environment).getPropertySources()) {
            if (propertySource instanceof EnumerablePropertySource) {
                EnumerablePropertySource<?> eps = (EnumerablePropertySource<?>) propertySource;
                for (String key : eps.getPropertyNames()) {
                    Object value = propertySource.getProperty(key);
                    System.out.println("environment: " + key + "=" + value.toString());
                }
            }
        }
    }

    public String getDescription() {
        Environment environment = this.context.getEnvironment();

        String applicationName = "app";
        if (environment.getProperty("spring.application.name") != null) {
            applicationName = environment.getProperty("spring.application.name");
        }
        String protocol = "http";
        if (environment.getProperty("server.protocol") != null) {
            protocol = environment.getProperty("server.protocol");
        }

        String domain = "localhost";
        if (environment.getProperty("server.domain") != null) {
            domain = environment.getProperty("server.domain");
        }

        String port = "8080";
        if (environment.getProperty("server.port") != null) {
            port = environment.getProperty("server.port");
        }
        String contextPath = "/";
        if (environment.getProperty("server.servlet.context-path") != null) {
            contextPath = environment.getProperty("server.servlet.context-path");
        }

        return "Application '" + applicationName + "' listening on " + protocol + "://" + domain + ":"
            + port + contextPath;

    }
}