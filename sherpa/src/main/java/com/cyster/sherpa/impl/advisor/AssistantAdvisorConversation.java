package com.cyster.sherpa.impl.advisor;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.cyster.sherpa.service.conversation.Conversation;
import com.cyster.sherpa.service.conversation.ConversationException;
import com.cyster.sherpa.service.conversation.Message;
import com.theokanning.openai.OpenAiResponse;
import com.theokanning.openai.messages.MessageRequest;
import com.theokanning.openai.runs.Run;
import com.theokanning.openai.runs.RunCreateRequest;
import com.theokanning.openai.runs.SubmitToolOutputRequestItem;
import com.theokanning.openai.runs.SubmitToolOutputsRequest;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.threads.Thread;

public class AssistantAdvisorConversation<C> implements Conversation {
    private static long MAX_BACKOFF = 1000 * 60 * 2;
    private static long MAX_ATTEMPTS = 100;
    private static long MAX_RETRY_COUNT = 5;
    private static final int MAX_PARAMETER_LENGTH = 50;
    private static final String ELIPSES = "...";

    private OpenAiService openAiService;
    private String assistantId;
    private Thread thread;
    private Toolset<C> toolset;
    private List<Message> messages;
    private String userMessage;
    private Optional<String> overrideInstructions = Optional.empty();
    private C context;

    AssistantAdvisorConversation(OpenAiService openAiService, String assistantId, Thread thread, Toolset<C> toolset,
        Optional<String> overrideInstructions, C context) {
        this.openAiService = openAiService;
        this.assistantId = assistantId;
        this.thread = thread;
        this.toolset = toolset;
        this.messages = new ArrayList<Message>();
        this.userMessage = "";
        this.overrideInstructions = overrideInstructions;
        this.context = context;
    }

    @Override
    public Conversation addMessage(String message) {
        this.messages.add(new Message(message));
        this.userMessage = message;
        return this;
    }

    @Override
    public Message respond() throws ConversationException {
        MessageRequest messageRequest = MessageRequest.builder()
            .role("user")
            .content(this.userMessage)
            .build();

        this.openAiService.createMessage(this.thread.getId(), messageRequest);

        var runRequestBuilder = RunCreateRequest.builder()
            .assistantId(this.assistantId);

        if (overrideInstructions.isPresent()) {
            runRequestBuilder.instructions(overrideInstructions.get());
        }

        Run run;
        try {
            run = this.openAiService.createRun(this.thread.getId(), runRequestBuilder.build());
        } catch (Throwable exception) {
            throw new ConversationException("Error while starting an OpenAi.run", exception);
        }

        int retryCount = 0;
        long delay = 500L;
        long attempts = 0;

        do {
            System.out.println("Run.status: " + run.getStatus());

            try {
                java.lang.Thread.sleep(delay);
                delay *= 2;
                if (delay > MAX_BACKOFF) {
                    delay = MAX_BACKOFF;
                }
            } catch (InterruptedException exception) {
                throw new RuntimeException("Thread interrupted with waitfinr for OpenAI run response", exception);
            }

            if (attempts > MAX_ATTEMPTS) {
                throw new RuntimeException("Exceeded maximum retry attempts (" + MAX_ATTEMPTS
                    + ") while waiting for a response for an openai run");
            }

            try {
                run = this.openAiService.retrieveRun(run.getThreadId(), run.getId());
            } catch (Throwable exception) {
                if (exception instanceof SocketTimeoutException) {
                    if (retryCount++ > MAX_RETRY_COUNT) {
                        throw new ConversationException("Socket Timeout while checking OpenAi.run.status", exception);
                    }
                } else {
                    throw new ConversationException("Error while checking OpenAi.run.status", exception);
                }
            }

            if (run.getStatus().equals("expired")) {
                throw new ConversationException("Run.expired");
            }

            if (run.getRequiredAction() != null) {

                System.out.println("Run.actions: " + run.getRequiredAction().getSubmitToolOutputs()
                    .getToolCalls()
                    .stream().map(toolCall -> toolCall.getFunction().getName()
                        + "("
                        + (toolCall.getFunction().getArguments().length() < MAX_PARAMETER_LENGTH ? toolCall
                            .getFunction().getArguments()
                            : toolCall.getFunction().getArguments().substring(0, MAX_PARAMETER_LENGTH - ELIPSES
                                .length()) + ELIPSES)
                        + ")").collect(Collectors.joining(", ")));

                if (run.getRequiredAction().getSubmitToolOutputs() == null
                    || run.getRequiredAction().getSubmitToolOutputs() == null
                    || run.getRequiredAction().getSubmitToolOutputs().getToolCalls() == null) {
                    throw new ConversationException("Action Required but no details");
                }

                var outputItems = new ArrayList<SubmitToolOutputRequestItem>();

                for (var toolCall : run.getRequiredAction().getSubmitToolOutputs().getToolCalls()) {
                    if (!toolCall.getType().equals("function")) {
                        throw new ConversationException("Unexpected tool call - not a function");
                    }

                    var callId = toolCall.getId();

                    var output = this.toolset.execute(toolCall.getFunction().getName(), toolCall.getFunction()
                        .getArguments(), this.context);

                    var outputItem = SubmitToolOutputRequestItem.builder()
                        .toolCallId(callId)
                        .output(output)
                        .build();

                    outputItems.add(outputItem);
                    messages.add(new Message(Message.Type.INFO, "Toolcall: " + toolCall.toString() + " Response: "
                        + outputItem.toString()));
                }
                SubmitToolOutputsRequest outputs = SubmitToolOutputsRequest.builder()
                    .toolOutputs(outputItems)
                    .build();
                this.openAiService.submitToolOutputs(run.getThreadId(), run.getId(), outputs);
            }
        } while (!run.getStatus().equals("completed"));

        System.out.println("Run.status: " + run.getStatus());

        OpenAiResponse<com.theokanning.openai.messages.Message> responseMessages = this.openAiService.listMessages(
            this.thread.getId());

        if (responseMessages.getData().size() == 0) {
            messages.add(new Message(Message.Type.INFO, "No responses"));
            throw new ConversationException("No Reponses");
        }
        var responseMessage = responseMessages.getData().get(0);
        if (!responseMessage.getRole().equals("assistant")) {
            messages.add(new Message(Message.Type.INFO, "Assistant did not response"));
            throw new ConversationException("Assistant did not respond");
        }

        var content = responseMessage.getContent();
        if (content.size() == 0) {
            messages.add(new Message(Message.Type.INFO, "No content"));
            throw new ConversationException("No Content");
        }

        if (content.size() > 1) {
            messages.add(new Message(Message.Type.INFO, "Lots of content (ignored)"));
            throw new ConversationException("Lots of Content");
        }

        if (!content.get(0).getType().equals("text")) {
            messages.add(new Message(Message.Type.INFO, "Content not of type text (ignored)"));
            throw new ConversationException("Content not of type text");
        }

        messages.add(new Message(Message.Type.INFO, content.toString()));

        var message = new Message(Message.Type.AI, content.get(0).getText().getValue());
        this.messages.add(message);
        return message;
    }

    @Override
    public List<Message> getMessages() {
        return this.messages;
    }

}
