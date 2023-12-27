package com.cyster.insight.impl.advisor;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.insight.service.advisor.Advisor;
import com.cyster.insight.service.advisor.AdvisorBuilder;
import com.cyster.insight.service.advisor.AdvisorService;
import com.cyster.insight.service.openai.OpenAiFactory;
import com.theokanning.openai.ListSearchParameters;
import com.theokanning.openai.OpenAiResponse;
import com.theokanning.openai.assistants.Assistant;
import com.theokanning.openai.assistants.AssistantFunction;
import com.theokanning.openai.assistants.AssistantRequest;
import com.theokanning.openai.assistants.AssistantToolsEnum;
import com.theokanning.openai.assistants.Tool;
import com.theokanning.openai.service.OpenAiService;


// https://platform.openai.com/docs/assistants/overview
// https://platform.openai.com/docs/assistants/tools/code-interpreter
// https://cobusgreyling.medium.com/what-are-openai-assistant-function-tools-exactly-06ef8e39b7bd

// See
// https://platform.openai.com/assistants

@Component
public class AdvisorServiceImpl implements AdvisorService {

    private OpenAiService openAiService;
    
    public AdvisorServiceImpl(OpenAiFactory openAifactory) {
        this.openAiService = openAifactory.getService();
    }
    
    public AdvisorBuilder getOrCreateAdvisor(String name) {
        // TODO support the other advisor implementations
        return new AssistantAdvisorImpl.Builder(this.openAiService, name);    
    }
     
    
 
}
