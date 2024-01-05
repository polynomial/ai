package com.extole.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import com.cyster.sage.CysterSageScan;
import com.extole.sage.ExtoleSageScan;

//@SpringBootApplication()
//@Import(value={CysterSageScan.class,ExtoleSageScan.class})
public class CodeStore implements CommandLineRunner {

    @Autowired
    private ApplicationContext applicationContext;

    //@Autowired
    //private SimpleVectorStoreService store;
    
    public static void main(String[] args) {
        SpringApplication.run(CodeStore.class, args);          
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("CodeStore.run!");
        System.out.println(applicationContext.getDisplayName());
        System.out.println(applicationContext.getId());

        //SimpleVectorStoreService stores = applicationContext.getBean(SimpleVectorStoreService.class);
        //System.out.println("Stores: " + stores.getStores().toString());
    }

}