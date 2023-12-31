package com.extole.insight.advisors;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import com.cyster.ai.vector.VectorStoreService;
import com.cyster.insight.service.advisor.Advisor;
import com.cyster.insight.service.advisor.AdvisorService;

@Component
public class ExtoleAdvisor implements Advisor {
    public final String NAME = "extole-advisor";

    private AdvisorService advisorService;
    private Optional<Advisor> advisor = Optional.empty();
    private VectorStore vectorStore;
    
    public ExtoleAdvisor(AdvisorService advisorService, VectorStoreService vectorStoreService) {
        this.advisorService = advisorService;
      
        this.vectorStore = vectorStoreService.getStore("TestCollection");

        List<Document> documents = List.of(
            new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
            new Document("The World is Big and Salvation Lurks Around the Corner"),
            new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));
      
        vectorStore.add(documents);

        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!! before");
        System.out.println(vectorStore.similaritySearch("rocks"));
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!! after");

    }
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public ConversationBuilder createConversation() {
        if (this.advisor.isEmpty()) {
            this.advisor = Optional.of(this.advisorService.getOrCreateAdvisor(NAME)
                .setInstructions("You are a helpful assistant.")
                .getOrCreate());
        }
        return this.advisor.get().createConversation();
    }
    
    

}
