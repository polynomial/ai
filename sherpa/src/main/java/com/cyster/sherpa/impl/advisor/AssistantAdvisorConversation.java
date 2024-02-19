package com.cyster.sherpa.impl.advisor;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cyster.sherpa.service.conversation.Conversation;
import com.cyster.sherpa.service.conversation.ConversationException;
import com.cyster.sherpa.service.conversation.Message;
import com.cyster.sherpa.service.conversation.Message.Type;
import com.theokanning.openai.OpenAiResponse;
import com.theokanning.openai.messages.MessageRequest;
import com.theokanning.openai.runs.Run;
import com.theokanning.openai.runs.RunCreateRequest;
import com.theokanning.openai.runs.SubmitToolOutputRequestItem;
import com.theokanning.openai.runs.SubmitToolOutputsRequest;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.threads.Thread;
import com.theokanning.openai.threads.ThreadRequest;

public class AssistantAdvisorConversation<C> implements Conversation {
    private static final long RUN_BACKOFF_MIN = 1000L;
    private static final long RUN_BACKOFF_MAX = 1000 * 60 * 1L;
    private static final long RUN_POLL_ATTEMPTS_MAX = 100;
    private static final long RUN_RETRIES_MAX = 5;
    private static final int MAX_PARAMETER_LENGTH = 50;
    private static final String ELIPSES = "...";
    private static final int CONVERSATION_RETIES_MAX = 3;

    private static final Logger logger = LogManager.getLogger(AssistantAdvisorConversation.class);

    private OpenAiService openAiService;
    private String assistantId;
    private Toolset<C> toolset;
    private List<Message> messages;
    private Optional<Thread> thread = Optional.empty();
    private Optional<String> overrideInstructions = Optional.empty();
    private C context;

    AssistantAdvisorConversation(OpenAiService openAiService, String assistantId, Toolset<C> toolset,
        Optional<String> overrideInstructions, C context) {
        this.openAiService = openAiService;
        this.assistantId = assistantId;
        this.toolset = toolset;
        this.messages = new ArrayList<Message>();
        this.overrideInstructions = overrideInstructions;
        this.context = context;
    }

    @Override
    public Conversation addMessage(String message) {
        var typedMessage = new Message(message);

        this.messages.add(typedMessage);

        return this;
    }

    @Override
    public Message respond() throws ConversationException {
        int retries = 0;

        Message message = null;
        do {
            try {
                message = doRun();
            } catch (RetryableAdvisorConversationException exception) {
                retries = retries + 1;
                if (retries > CONVERSATION_RETIES_MAX) {
                    throw new ConversationException("Advisor experienced problems responding to conversation, tried "
                        + retries + " times", exception);
                }
                logger.warn("Advisor thread run failed, retrying");
            } catch (AdvisorConversationException exception) {
                throw new ConversationException("Advisor experienced problems responding to conversation", exception);
            }
        } while (message == null);

        return message;
    }

    @Override
    public List<Message> getMessages() {
        return this.messages;
    }

    private Message doRun() throws AdvisorConversationException {
        var thread = getOrCreateThread();

        var runRequestBuilder = RunCreateRequest.builder()
            .assistantId(this.assistantId);

        if (overrideInstructions.isPresent()) {
            runRequestBuilder.instructions(overrideInstructions.get());
        }

        Run run;
        try {
            run = this.openAiService.createRun(thread.getId(), runRequestBuilder.build());
        } catch (Throwable exception) {
            throw new AdvisorConversationException("Error while starting an OpenAi.run", exception);
        }

        int retryCount = 0;
        long delay = RUN_BACKOFF_MIN;
        long attempts = 0;
        String lastStatus = "";

        do {
            logger.info("Run.status: " + run.getStatus() + " (delay " + delay + "ms)");

            try {
                if (lastStatus.equals(run.getStatus())) {
                    java.lang.Thread.sleep(delay);
                    delay *= 2;
                    if (delay > RUN_BACKOFF_MAX) {
                        delay = RUN_BACKOFF_MAX;
                    }
                } else {
                    delay /= 2;
                    if (delay < RUN_BACKOFF_MIN) {
                        delay = RUN_BACKOFF_MIN;
                    }
                }
                lastStatus = run.getStatus();
            } catch (InterruptedException exception) {
                throw new RuntimeException("Thread interrupted with waitfinr for OpenAI run response", exception);
            }

            if (attempts > RUN_POLL_ATTEMPTS_MAX) {
                throw new AdvisorConversationException("Exceeded maximum openai thread run retry attempts ("
                    + RUN_POLL_ATTEMPTS_MAX
                    + ") while waiting for a response for an openai run");
            }

            try {
                run = this.openAiService.retrieveRun(run.getThreadId(), run.getId());
            } catch (Throwable exception) {
                if (exception instanceof SocketTimeoutException) {
                    if (retryCount++ > RUN_RETRIES_MAX) {
                        throw new AdvisorConversationException("Socket Timeout while checking OpenAi.run.status",
                            exception);
                    }
                } else {
                    throw new AdvisorConversationException("Error while checking OpenAi.run.status", exception);
                }
            }

            if (run.getStatus().equals("expired")) {
                throw new RetryableAdvisorConversationException("Run.expired");
            }

            if (run.getStatus().equals("failed")) {
                throw new AdvisorConversationException("Run.failed");
            }

            if (run.getRequiredAction() != null) {
                logger.info("Run.actions: " + run.getRequiredAction().getSubmitToolOutputs()
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
                    throw new AdvisorConversationException("Action Required but no details");
                }

                var outputItems = new ArrayList<SubmitToolOutputRequestItem>();

                for (var toolCall : run.getRequiredAction().getSubmitToolOutputs().getToolCalls()) {
                    if (!toolCall.getType().equals("function")) {
                        throw new AdvisorConversationException("Unexpected tool call - not a function");
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

        logger.info("Run.status: " + run.getStatus());

        OpenAiResponse<com.theokanning.openai.messages.Message> responseMessages = this.openAiService.listMessages(
            thread.getId());

        if (responseMessages.getData().size() == 0) {
            messages.add(new Message(Message.Type.INFO, "No responses"));
            throw new AdvisorConversationException("No Reponses");
        }
        var responseMessage = responseMessages.getData().get(0);
        if (!responseMessage.getRole().equals("assistant")) {
            messages.add(new Message(Message.Type.INFO, "Assistant did not response"));
            throw new AdvisorConversationException("Assistant did not respond");
        }

        var content = responseMessage.getContent();
        if (content.size() == 0) {
            messages.add(new Message(Message.Type.INFO, "No content"));
            throw new AdvisorConversationException("No Content");
        }

        if (content.size() > 1) {
            messages.add(new Message(Message.Type.INFO, "Lots of content (ignored)"));
            throw new AdvisorConversationException("Lots of Content");
        }

        if (!content.get(0).getType().equals("text")) {
            messages.add(new Message(Message.Type.INFO, "Content not of type text (ignored)"));
            throw new AdvisorConversationException("Content not of type text");
        }

        messages.add(new Message(Message.Type.INFO, content.toString()));

        var message = new Message(Message.Type.AI, content.get(0).getText().getValue());
        this.messages.add(message);

        return message;
    }

    private Thread getOrCreateThread() {
        if (thread.isEmpty()) {
            var threadRequest = ThreadRequest.builder().build();

            this.thread = Optional.of(this.openAiService.createThread(threadRequest));

            for (var message : this.messages) {
                if (message.getType() == Type.USER) {
                    MessageRequest messageRequest = MessageRequest.builder()
                        .role("user")
                        .content(message.getContent())
                        .build();
                    this.openAiService.createMessage(this.thread.get().getId(), messageRequest);
                }
            }
        }

        return this.thread.get();
    }

}
