package com.extole.sage.advisors;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import com.cyster.ai.vector.simple.SimpleVectorStoreService;
import com.cyster.sherpa.impl.advisor.Tool;
import com.cyster.sherpa.service.advisor.Advisor;
import com.cyster.sherpa.service.advisor.AdvisorService;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@Component
public class ExtoleJavascriptPrehandlerActionAdvisor implements Advisor {
    public final String NAME = "extole-prehandler-action";

    private AdvisorService advisorService;
    private Optional<Advisor> advisor = Optional.empty();
    private SimpleVectorStoreService simpleVectorStoreService;

    public ExtoleJavascriptPrehandlerActionAdvisor(AdvisorService advisorService,
        SimpleVectorStoreService simpleVectorStoreService) {
        this.advisorService = advisorService;
        this.simpleVectorStoreService = simpleVectorStoreService;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public ConversationBuilder createConversation() {
        if (this.advisor.isEmpty()) {
            VectorStore store = this.simpleVectorStoreService.getStore("extole-code");

            String resourcePath = "/extole/scenario/prehandler_action_context.js";
            URL resourceUrl = ExtoleJavascriptPrehandlerActionAdvisor.class.getResource(resourcePath);
            if (resourceUrl == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }
            Path javascriptActionContextPath;
            try {
                javascriptActionContextPath = Paths.get(resourceUrl.toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException("Unable to convert resourceUrl to URI");
            }

            String instructions = """ 
            The JavaScript code described here executes with a variable 'context' of type PrehandlerActionContext.
            It should create a processedRawEvent using the ProcessedRawEventBuilder available from the context.

            To understand how to use the 'context' you need explore the api for classes like:
            - PrehandlerActionContext
            - PrehandlerContext
            - GlobalContext
            - LoggerContext
            - ClientContext
            - GlobalServices
            - ProcessedRawEventBuilder
""";
            instructions = """
A prehandler can modify a raw event before its processed by Extole. 
The request is modified using the ProcessedRawEventBuilder available as getEventBuilder from the context variable. 

A prehandler code snippet prehandler_javascript_code is executed in the following context.

var context = PrehandlerActionContext(javaPrehandlerContext);
(function(context) {
  // ... prehandler_javascript_code here ... 
})(context)

To understand how to use the 'context' you need explore the api for classes like:
 - PrehandlerActionContext
 - PrehandlerContext
 - GlobalContext
 - LoggerContext
 - ClientContext
 - GlobalServices
 - ProcessedRawEventBuilder
""";

            this.advisor = Optional.of(this.advisorService.getOrCreateAdvisor(NAME)
                .setInstructions(instructions)
                .withFile(javascriptActionContextPath)
                .withTool(new CodeRepositoryTool(store))
                .getOrCreate());
        }
        return this.advisor.get().createConversation();
    }

    public static class CodeRepositoryTool implements Tool<CodeRequest> {
        private VectorStore store;

        CodeRepositoryTool(VectorStore store) {
            this.store = store;
        }

        @Override
        public String getName() {
            return "get_code";
        }

        @Override
        public String getDescription() {
            return "Retrieves interfaces and classes";
        }

        @Override
        public Class<CodeRequest> getParameterClass() {
            return CodeRequest.class;
        }

        @Override
        public Object execute(CodeRequest codeRequest) {
            List<Document> documents = store.similaritySearch(codeRequest.query);
            return new CodeResponse(documents.stream()
                .map(Document::getContent)
                .collect(Collectors.toList()));
        }
    }

    public static class CodeRequest {
        @JsonPropertyDescription("name of class or attribute to find")
        @JsonProperty(required = true)
        public String query;
    }

    public static class CodeResponse {
        public List<String> content;

        public CodeResponse(List<String> content) {
            this.content = content;
        }

        public List<String> getContent() {
            return this.content;
        }
    }

}
