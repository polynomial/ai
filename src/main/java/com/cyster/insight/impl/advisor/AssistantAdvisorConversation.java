package com.cyster.insight.impl.advisor;

import java.util.ArrayList;
import java.util.List;

import com.cyster.insight.service.conversation.Conversation;
import com.cyster.insight.service.conversation.ConversationException;
import com.cyster.insight.service.conversation.Message;
import com.theokanning.openai.OpenAiResponse;
import com.theokanning.openai.messages.MessageRequest;
import com.theokanning.openai.runs.RunCreateRequest;
import com.theokanning.openai.runs.SubmitToolOutputRequestItem;
import com.theokanning.openai.runs.SubmitToolOutputsRequest;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.threads.Thread;


public class AssistantAdvisorConversation implements Conversation {
    private OpenAiService openAiService;
    private String assistantId;
    private Thread thread;
    private Toolset toolset;
    private List<Message> messages;
    private String userMessage;
    
    AssistantAdvisorConversation(OpenAiService openAiService, String assistantId, Thread thread, Toolset toolset) {
        this.openAiService = openAiService;
        this.assistantId = assistantId;
        this.thread = thread;
        this.toolset = toolset;
        this.messages = new ArrayList<Message>();
        this.userMessage = "";
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
        
        var runRequest =  RunCreateRequest.builder()
            .assistantId(this.assistantId)
            // .instructions()
            .build();

        var run = this.openAiService.createRun(this.thread.getId(), runRequest);
        do {            
            System.out.println("Run.status: " + run.getStatus());
            
            try {
                java.lang.Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
   
            run = this.openAiService.retrieveRun(run.getThreadId(), run.getId());

            if (run.getStatus().equals("expired")) {
                throw new ConversationException("Run expired");
            }
            
            if (run.getRequiredAction() != null) {
                System.out.println("Run.getRequiredAction(): " +  run.getRequiredAction());

                if (run.getRequiredAction().getSubmitToolOutputs() == null 
                    || run.getRequiredAction().getSubmitToolOutputs() == null 
                    || run.getRequiredAction().getSubmitToolOutputs().getToolCalls() == null) {
                    throw new ConversationException("Action Required but no details");
                }
                
                for(var toolCall: run.getRequiredAction().getSubmitToolOutputs().getToolCalls()) {                    
                    if (!toolCall.getType().equals("function")) {
                        throw new ConversationException("Unexpected tool call - not a function");
                    }
                    
                    var callId = toolCall.getId();
                    var output = this.toolset.execute(toolCall.getFunction().getName(), toolCall.getFunction().getArguments());
                    
                    var outputItem = SubmitToolOutputRequestItem.builder()
                        .toolCallId(callId)
                        .output(output)
                        .build();
                   
                    var outputItems = new ArrayList<SubmitToolOutputRequestItem>();
                    outputItems.add(outputItem);
                    
                    SubmitToolOutputsRequest outputs = SubmitToolOutputsRequest.builder()
                        .toolOutputs(outputItems)
                        .build();
                    
                    this.openAiService.submitToolOutputs(run.getThreadId(), run.getId(), outputs);
                }
            }            
        } while (!run.getStatus().equals("completed"));
        
        System.out.println("Run.status: " + run.getStatus());

        OpenAiResponse<com.theokanning.openai.messages.Message> responseMessages = 
            this.openAiService.listMessages(this.thread.getId());
        
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
        
        var message = new Message(Message.Type.AI, content.get(0).getText().getValue());
        this.messages.add(message);
        return message;
    }

    @Override
    public List<Message> getMessages() {
        return this.messages;
    }

}
