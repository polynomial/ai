package com.extole.sage.advisors.brand;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cyster.sherpa.service.advisor.Advisor;
import com.cyster.sherpa.service.advisor.AdvisorService;

@Component
public class ExtoleBrandAdvisor implements Advisor {
    public final String NAME = "extole-brand";

    private AdvisorService advisorService;
    private Optional<Advisor> advisor = Optional.empty();
    private Optional<String> brandFetchApiKey;

    public ExtoleBrandAdvisor(AdvisorService advisorService,
        @Value("${brandFetchApiKey:#{environment.BRANDFETCH_API_KEY}}") String brandFetchApiKey) {
        this.advisorService = advisorService;
        this.brandFetchApiKey = Optional.of(brandFetchApiKey);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public ConversationBuilder createConversation() {
        if (this.advisor.isEmpty()) {
            String instructions = """ 
You focus on find details on Company brands.
""";

            this.advisor = Optional.of(this.advisorService.getOrCreateAdvisor(NAME)
                .setInstructions(instructions)
                .withTool(new BrandSearchTool(this.brandFetchApiKey))
                .withTool(new BrandFetchTool(this.brandFetchApiKey))
                .getOrCreate());
        }
        return this.advisor.get().createConversation();
    }

}
