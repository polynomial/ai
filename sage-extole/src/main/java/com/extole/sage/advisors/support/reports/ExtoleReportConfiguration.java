package com.extole.sage.advisors.support.reports;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.extole.sage.advisors.support.ExtoleWebClientFactory;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class ExtoleReportConfiguration {
    private static final Logger logger = LogManager.getLogger(ExtoleReportConfiguration.class); 

    private ExtoleWebClientFactory extoleWebClientFactory;
    
    public ExtoleReportConfiguration(ExtoleWebClientFactory extoleWebClientFactory, ApplicationContext context) throws IOException, ExtoleReportConfigurtationException {
        this.extoleWebClientFactory = extoleWebClientFactory;
        registerReports(context);
    }

    private void registerReports(ApplicationContext context) throws IOException, ExtoleReportConfigurtationException {
        if (context instanceof ConfigurableApplicationContext) {
            ConfigurableApplicationContext configurableContext = (ConfigurableApplicationContext) context;
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            
            Resource[] resources = resolver.getResources("classpath:/extole/reports/*.yml");
            
            for (Resource resource : resources) {
                logger.info("Loading Extole report tool: " + resource.getURI().toString());
                
                YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
                yaml.setResources(resource);
                
                String name = yaml.getObject().getProperty("name");
                if (name == null) {
                    throw new ExtoleReportConfigurtationException(resource, "name is not defined");
                }

                String description = yaml.getObject().getProperty("description");
                if (description == null) {
                    throw new ExtoleReportConfigurtationException(resource, "description is not defined");
                }
                
                String reportName = yaml.getObject().getProperty("reportName");
                if (reportName == null) {
                    throw new ExtoleReportConfigurtationException(resource, "reportName is not defined");
                }
                
                int rowLimit = 10;
                String rowLimitString = yaml.getObject().getProperty("rowLimit");
                if (rowLimitString != null) {
                    rowLimit = Integer.valueOf(rowLimitString);
                }
                
                Map<String, Object> parameters = new HashMap<>();
                
                // TODO handle nested attributes beyond the first level
                Properties properties = yaml.getObject();
                String parametersPrefix = "parameters.";
                for (String propertyName : properties.stringPropertyNames()) {
                    if (propertyName.startsWith(parametersPrefix)) {
                        String key = propertyName.substring(parametersPrefix.length());
                        String value = properties.getProperty(propertyName);
                        
                        parameters.put(key, value);
                    }
                }

                var report = new ExtoleConfigurableTimeRangeReportTool.Builder()
                    .withName(name)
                    .withDescription(description)
                    .withReportName(reportName)
                    .withRowLimit(rowLimit)
                    .withParameters(parameters)
                    .withExtoleWebClientFactory(extoleWebClientFactory)
                    .build();
                       
                logger.info("Loaded Extole report tool: " + report.getName());
                
                configurableContext.getBeanFactory().registerSingleton(report.getName(), report);
            }
        }
    }
    
}

