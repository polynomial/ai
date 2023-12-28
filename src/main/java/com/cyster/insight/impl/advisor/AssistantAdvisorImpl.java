package com.cyster.insight.impl.advisor;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Optional;

import com.cyster.insight.service.advisor.Advisor;
import com.cyster.insight.service.advisor.AdvisorBuilder;
import com.cyster.insight.service.conversation.Conversation;
import com.theokanning.openai.ListSearchParameters;
import com.theokanning.openai.OpenAiResponse;
import com.theokanning.openai.assistants.Assistant;
import com.theokanning.openai.assistants.AssistantRequest;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.threads.ThreadRequest;

public class AssistantAdvisorImpl implements Advisor {
    
    public static String VERSION = "0.1";
    public static String METADATA_VERSION = "version";
    public static String METADATA_IDENTITY = "identityHash";
    
    private OpenAiService openAiService;
    private Assistant assistant;
    private Toolset toolset;
    
    public AssistantAdvisorImpl(OpenAiService openAiService, Assistant assistant, Toolset toolset) {
        this.openAiService = openAiService;
        this.assistant = assistant;
        this.toolset = toolset;
    }
    
    public String getId() {
        return this.assistant.getId();
    }
    
    public String getName() {
        return this.assistant.getName();
    }

    @Override
    public Conversation start() {
        var threadRequest = ThreadRequest.builder()
            .build();
        
        var thread = this.openAiService.createThread(threadRequest);
 
        return new AssistantAdvisorConversation(this.openAiService, this.assistant.getId(), thread, this.toolset);
    }
    
    
    public static class Builder implements AdvisorBuilder {

        private static final String MODEL = "gpt-4-1106-preview";
            
        private final OpenAiService openAiService;
        private final String name;
        private Optional<String> instructions = Optional.empty();
        private Toolset.Builder toolsetBuilder = new Toolset.Builder();
        
        Builder(OpenAiService openAiService, String name) {
            this.openAiService = openAiService;
            this.name = name;
        }
        
        @Override
        public Builder setInstructions(String instructions) {
            this.instructions = Optional.of(instructions);
            return this;
        }
        @Override
        public  <T> AdvisorBuilder withTool(AdvisorTool<T> tool) {
            this.toolsetBuilder.addTool(tool);
            return this;
        }
    
        //@Override
        //public ManagedAssistantBuilder withFile() {
            // TODO Auto-generated method stub
        //    return null;
        //}
    
        @Override
        public Advisor getOrCreate() {
            String hash = this.getHash();
            
            var assistant = this.findAssistant(hash);
            if (assistant.isEmpty()) {
                assistant = Optional.of(this.create(hash));                
            }
            
            return new AssistantAdvisorImpl(this.openAiService, assistant.get(), this.toolsetBuilder.create());
        }
        
        private Assistant create(String hash) {
                               
            var metadata = new HashMap<String, String>();
            metadata.put(METADATA_VERSION, VERSION);
            metadata.put(METADATA_IDENTITY, hash);
        
            Toolset toolset = this.toolsetBuilder.create();

            var requestBuilder = AssistantRequest.builder()
                .name(this.name)
                .model(MODEL)
                .metadata(metadata)
                .tools(toolset.getAssistantTools());
                
           if (this.instructions.isPresent()) {
               requestBuilder.instructions(this.instructions.get());
           }
            
            var assistant =  this.openAiService.createAssistant(requestBuilder.build());
            
            return assistant;
        }
    
        private Optional<Assistant> findAssistant(String hash) {
            OpenAiResponse<Assistant> response = null;
            do {
                var searchBuilder = ListSearchParameters.builder().limit(99);
                if (response != null) {
                    searchBuilder.after(response.getLastId());
                }
                response =  this.openAiService.listAssistants(searchBuilder.build());
                
                for (var assistant : response.getData()) {
                    if (assistant.getName() != null && assistant.getName().equals(this.name)) {
                        if (assistant.getMetadata().containsKey(METADATA_IDENTITY)) {
                            if (assistant.getMetadata().get(METADATA_IDENTITY).equals(hash)) {
                                return Optional.of(assistant);                                
                            }
                        }
                    }
                }
            } while (response.isHasMore());
            
            return Optional.empty();
        } 
        
        private String getHash() {
            String text = VERSION + this.name + this.instructions;
            for(var tool: this.toolsetBuilder.create().getAdvisorTools()) {
                text = text + tool.getName() + tool.getDescription(); 
            }
            
            MessageDigest digest;
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            byte[] hashBytes = digest.digest(text.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte value : hashBytes) {
                String hex = Integer.toHexString(0xff & value);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();   
        }
        
    }
}
