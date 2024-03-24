package com.extole.sage.scenarios.runbooks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.extole.sage.advisors.support.ExtoleSupportAdvisor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class ExtoleRunbookConfiguration {
    private static final Logger logger = LogManager.getLogger(ExtoleRunbookConfiguration.class); 
    
    private ExtoleSupportAdvisor advisor;
    
    ExtoleRunbookConfiguration(ExtoleSupportAdvisor advisor) {
        this.advisor = advisor;
    }
    
    @Bean
    public List<RunbookScenario> runbooks() throws IOException, ExtoleRunbookConfigurationException {
        logger.info("Loading Extole runbooks");

        List<RunbookScenario> runbooks = new ArrayList<>();
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
            
            var runbook = new ExtoleConfigurableRunbookScenario.Builder();
            runbook.withName(name);
            runbook.withDescription(description);
            runbook.withKeywords(keywords);
            runbook.withInstructions(instructions);
            runbook.withAdvisor(advisor);
            
            runbooks.add(runbook.build());
        }
        
        return runbooks;
    }
    

}
