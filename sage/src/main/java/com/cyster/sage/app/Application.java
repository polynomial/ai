package com.cyster.sage.app;

import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import com.extole.sage.ExtoleSageScan;

import com.cyster.sage.CysterSageScan;
import com.cyster.store.CysterStoreScan;

@SpringBootApplication
@Import(value={CysterSageScan.class, CysterStoreScan.class, ExtoleSageScan.class})
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	
	public CommandLineRunner commandLineRunner(ApplicationContext context) {
		return args -> {

			System.out.println("Spring Boot Beans:");

			String[] beanNames = context.getBeanDefinitionNames();
			Arrays.sort(beanNames);
			for (String beanName : beanNames) {
				System.out.println(beanName);
			}

			Environment environment = context.getEnvironment();
			if (!environment.containsProperty("OPENAI_API_KEY")) {
				System.out.println("Warning: Environment variable OPENAI_API_KEY not defined!");
			}
			

			String protocol = "http";
			String domain = "localhost";
			String port = "8080"; // environment.getProperty("server.port");
			String contextPath = "/";  // environment.getProperty("server.servlet.context-path");
			System.out.println("Listening on: " + protocol + "://" + domain + ":" + port + contextPath);
		};
	}

}
