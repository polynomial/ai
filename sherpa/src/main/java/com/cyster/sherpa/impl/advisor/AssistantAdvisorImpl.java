package com.cyster.sherpa.impl.advisor;

import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cyster.sherpa.service.advisor.Advisor;
import com.cyster.sherpa.service.advisor.AdvisorBuilder;
import com.cyster.sherpa.service.conversation.Conversation;
import com.theokanning.openai.ListSearchParameters;
import com.theokanning.openai.OpenAiHttpException;
import com.theokanning.openai.OpenAiResponse;
import com.theokanning.openai.assistants.Assistant;
import com.theokanning.openai.assistants.AssistantRequest;
import com.theokanning.openai.file.File;
import com.theokanning.openai.service.OpenAiService;

public class AssistantAdvisorImpl<C> implements Advisor<C> {
    private static final Logger logger = LogManager.getLogger(AssistantAdvisorImpl.class);

    public static String VERSION = "0.1";
    public static String METADATA_VERSION = "version";
    public static String METADATA_IDENTITY = "identityHash";

    private OpenAiService openAiService;
    private Assistant assistant;
    private Toolset<C> toolset;

    public AssistantAdvisorImpl(OpenAiService openAiService, Assistant assistant, Toolset<C> toolset) {
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
    public ConversationBuilder<C> createConversation() {
        return new ConversationBuilder<C>(this);
    }

    public static class ConversationBuilder<C2> implements Advisor.ConversationBuilder<C2> {
        private Optional<String> overrideInstructions = Optional.empty();
        private C2 context = null;
        private AssistantAdvisorImpl<C2> advisor;
        private List<String> messages = new ArrayList<String>();

        private ConversationBuilder(AssistantAdvisorImpl<C2> advisor) {
            this.advisor = advisor;
        }

        @Override
        public ConversationBuilder<C2> withContext(C2 context) {
            this.context = context;
            return this;
        }

        @Override
        public ConversationBuilder<C2> setOverrideInstructions(String instructions) {
            this.overrideInstructions = Optional.of(instructions);
            return this;
        }

        @Override
        public ConversationBuilder<C2> addMessage(String message) {
            this.messages.add(message);
            return this;
        }

        @Override
        public Conversation start() {
            var conversation = new AssistantAdvisorConversation<C2>(this.advisor.openAiService,
                this.advisor.assistant.getId(),
                this.advisor.toolset,
                overrideInstructions,
                context);

            for (var message : this.messages) {
                conversation.addMessage(message);
            }

            return conversation;
        }
    }

    public static class Builder<C2> implements AdvisorBuilder<C2> {
        private static final String MODEL = "gpt-4-1106-preview";

        private final OpenAiService openAiService;
        private final String name;
        private Optional<String> instructions = Optional.empty();
        private Toolset.Builder<C2> toolsetBuilder = new Toolset.Builder<C2>();
        private List<Path> filePaths = new ArrayList<Path>();

        Builder(OpenAiService openAiService, String name) {
            this.openAiService = openAiService;
            this.name = name;
        }

        @Override
        public Builder<C2> setInstructions(String instructions) {
            this.instructions = Optional.of(instructions);
            return this;
        }

        @Override
        public <T> AdvisorBuilder<C2> withTool(Tool<T, C2> tool) {
            this.toolsetBuilder.addTool(tool);
            return this;
        }

        @Override
        public AdvisorBuilder<C2> withFile(Path path) {
            this.filePaths.add(path);
            return this;
        }

        @Override
        public Advisor<C2> getOrCreate() {
            String hash = this.getHash();

            var assistant = this.findAssistant(hash);
            if (assistant.isEmpty()) {
                assistant = Optional.of(this.create(hash));
            }

            return new AssistantAdvisorImpl<C2>(this.openAiService, assistant.get(), this.toolsetBuilder.create());
        }

        private Assistant create(String hash) {
            List<String> fileIds = new ArrayList<String>();
            for (var filePath : this.filePaths) {
                File file = this.openAiService.uploadFile("assistants", filePath.toString());
                fileIds.add(file.getId());
            }

            var metadata = new HashMap<String, String>();
            metadata.put(METADATA_VERSION, VERSION);
            metadata.put(METADATA_IDENTITY, hash);

            var toolset = new AdvisorToolset<C2>(this.toolsetBuilder.create());
            if (fileIds.size() > 0) {
                toolset.enableRetrival();
            }

            var requestBuilder = AssistantRequest.builder()
                .name(this.name)
                .model(MODEL)
                .metadata(metadata)
                .tools(toolset.getAssistantTools());

            if (this.instructions.isPresent()) {
                requestBuilder.instructions(this.instructions.get());
            }

            if (fileIds.size() > 0) {
                requestBuilder.fileIds(fileIds);
            }

            Assistant assistant;
            try {
                assistant = this.openAiService.createAssistant(requestBuilder.build());
            } catch (OpenAiHttpException exception) {
                // TODO throw declared exception
                logger.error("Failed to create OpenAI assistant: " + requestBuilder.toString().replace("\n", "\\n"),
                    exception);
                throw exception;
            }

            return assistant;
        }

        private Optional<Assistant> findAssistant(String hash) {
            OpenAiResponse<Assistant> response = null;
            do {
                var searchBuilder = ListSearchParameters.builder().limit(99);
                if (response != null) {
                    searchBuilder.after(response.getLastId());
                }
                response = this.openAiService.listAssistants(searchBuilder.build());

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
            for (var tool : this.toolsetBuilder.create().getTools()) {
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
