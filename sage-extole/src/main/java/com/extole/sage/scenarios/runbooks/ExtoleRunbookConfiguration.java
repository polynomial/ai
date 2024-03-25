package com.extole.sage.scenarios.runbooks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.extole.sage.advisors.support.ExtoleSupportAdvisor;

import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class ExtoleRunbookConfiguration {
    private static final Logger logger = LogManager.getLogger(ExtoleRunbookConfiguration.class); 

    private ExtoleSupportAdvisor advisor;
    
    public ExtoleRunbookConfiguration(ExtoleSupportAdvisor advisor, ApplicationContext context) throws IOException, ExtoleRunbookConfigurationException {
        this.advisor = advisor;
        registerRunbooks(context);
    }

    private void registerRunbooks(ApplicationContext context) throws IOException, ExtoleRunbookConfigurationException {
        if (context instanceof ConfigurableApplicationContext) {
            ConfigurableApplicationContext configurableContext = (ConfigurableApplicationContext) context;
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            
            Resource[] resources = resolver.getResources("classpath:/extole/runbooks/*.yml");
            
            for (Resource resource : resources) {
                logger.info("Loading Extole runbook: " + resource.getURI().toString());
                
                YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
                yaml.setResources(resource);
                
                String name = yaml.getObject().getProperty("name");
                if (name == null) {
                    throw new ExtoleRunbookConfigurationException(resource, "name is not defined");
                }
                
                String description = yaml.getObject().getProperty("description");
                if (description == null) {
                    throw new ExtoleRunbookConfigurationException(resource, "description is not defined");
                }
                
                String keywords = yaml.getObject().getProperty("keywords");
                if (keywords == null) {
                    throw new ExtoleRunbookConfigurationException(resource, "keywords is not defined");
                }
                
                String instructions = yaml.getObject().getProperty("instructions");
                if (instructions == null) {
                    throw new ExtoleRunbookConfigurationException(resource, "instructions is not defined");
                }
                
                var runbook = new ExtoleConfigurableRunbookScenario.Builder()
                    .withName("extole_runbook_" + name)
                    .withDescription(description)
                    .withKeywords(keywords)
                    .withInstructions(instructions)
                    .withAdvisor(advisor)
                    .build();
                       
                logger.info("Loaded Extole runbook: " + runbook.getName());
                
                configurableContext.getBeanFactory().registerSingleton(runbook.getName(), runbook);
            }
        }
    }
    
}

