package com.extole.app.jira.ticket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.cyster.sherpa.service.conversation.ConversationException;
import com.cyster.sherpa.service.conversation.Message;
import com.extole.sage.scenarios.support.ExtoleSupportTicketScenario;

@Service
@EnableAsync
public class TicketCommenter {
    private static final Logger logger = LogManager.getLogger(TicketCommenter.class);    
    private static final Logger ticketLogger = LogManager.getLogger("tickets");

    private ExtoleSupportTicketScenario supportTicketScenario;

    public TicketCommenter(ExtoleSupportTicketScenario supportTicketScenario) {
        this.supportTicketScenario = supportTicketScenario; 
    }

    @Async("ticketCommentTaskExecutor")
    public void process(String ticketNumber) {
        logger.info("Ticket - processing " + ticketNumber + " asynchronously on thread " + Thread.currentThread().getName());
        
        Message response;
        try {
            response = supportTicketScenario.createConversation(null, null)
                .addMessage("Please review the new ticket " + ticketNumber)
                .respond();
        } catch (ConversationException exception) {
            logger.error("Problem processing ticket: " + ticketNumber, exception);
            return;
        }

        logger.info("Ticket - processed " + ticketNumber + " : " + response);

        ticketLogger.info(ticketNumber + " " + response.toString());
    }
    
    @Bean(name = "ticketCommentTaskExecutor")
    public ThreadPoolTaskExecutor getTaskExecutor() {
        var taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(100);
        taskExecutor.setMaxPoolSize(110);
        taskExecutor.setQueueCapacity(120);
        taskExecutor.setThreadNamePrefix("TicketCommenter-");
        taskExecutor.initialize();
        
        return taskExecutor;
    }
}
