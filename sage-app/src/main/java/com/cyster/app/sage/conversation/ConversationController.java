package com.cyster.app.sage.conversation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.cyster.sage.service.scenariosession.ScenarioSession;
import com.cyster.sage.service.scenariosession.ScenarioSessionStore;
import com.cyster.sherpa.service.conversation.ConversationException;
import com.cyster.sherpa.service.conversation.Message;
import com.cyster.sherpa.service.scenario.Scenario;
import com.cyster.sherpa.service.scenario.ScenarioException;
import com.cyster.sherpa.service.scenario.ScenarioService;

@RestController
public class ConversationController {
    private ScenarioSessionStore scenarioSessionStore;
    private ScenarioService scenarioStore;

    private static final Logger logger = LogManager.getLogger(ConversationController.class);

    public ConversationController(ScenarioSessionStore scenarioSessionStore, ScenarioService scenarioStore) {
        this.scenarioSessionStore = scenarioSessionStore;
        this.scenarioStore = scenarioStore;
    }

    @GetMapping("/conversations")
    public List<ConversationResponse> index() {
        return scenarioSessionStore.createQueryBuilder().list().stream()
            .map(value -> new ConversationResponse.Builder().setId(value.getId())
                .setMessages(value.getConversation().getMessages()).build())
            .collect(Collectors.toList());
    }

    @PostMapping("/conversations")
    public ConversationResponse create_conversation(
        @RequestHeader("Authorization") String authorizationHeader,
        @RequestBody ConversationRequest request)
        throws ScenarioNameNotSpecifiedRestException, ScenarioNameNotFoundRestException {
        var token = extractAccessToken(authorizationHeader);

        if (request == null || request.getScenarioName() == null || request.getScenarioName().isBlank()) {
            throw new ScenarioNameNotSpecifiedRestException();
        }

        Scenario<?,?> scenario;
        try {
            scenario = this.scenarioStore.getScenario(request.getScenarioName());
        } catch (ScenarioException exception) {
            throw new ScenarioNameNotFoundRestException(request.getScenarioName());
        }

        /* TODO 
        Map<String, String> context;
        if (request.getContext() == null) {
            context = Collections.emptyMap();
        } else {
            context = request.getContext();
        }
        token.ifPresent(accessToken -> context.put("accessToken", accessToken));
        */
        
        var conversation = scenario.createConversation(null, null);

        var handle = scenarioSessionStore.addSession(scenario, conversation);

        return new ConversationResponse.Builder()
            .setId(handle.getId())
            .setScenario(scenario.getName())
            .setMessages(conversation.getMessages())
            .build();
    }

    @PostMapping("/conversations/messages")
    public ConvenienceConversationResponse start_conversation(
        @RequestHeader MultiValueMap<String, String> headers,
        @RequestBody PromptedConversationRequest request)
        throws ScenarioNameNotSpecifiedRestException, ScenarioNameNotFoundRestException, ConversationRestException {
        
        Optional<String> token = Optional.empty();
        if (headers.containsKey("Authorization")) {
             token = extractAccessToken(headers.getFirst("Authorization"));
        }
        
        if (request == null || request.getScenario().isBlank()) {
            throw new ScenarioNameNotSpecifiedRestException();
        }

        Scenario<?,?> scenario;
        try {
            scenario = this.scenarioStore.getScenario(request.getScenario());
        } catch (ScenarioException exception) {
            throw new ScenarioNameNotFoundRestException(request.getScenario());
        }

        // TODO process Parameters and Context
        // token.ifPresent(accessToken -> context.put("accessToken", accessToken));
    
         
        var conversation = scenario.createConversation(null, null);

        if (request.getPrompt() != null && !request.getPrompt().isBlank()) {
            conversation.addMessage(request.getPrompt());
        }

        var handle = scenarioSessionStore.addSession(scenario, conversation);

        Message answer;
        try {
            answer = conversation.respond();
        } catch (ConversationException exception) {
            throw new ConversationRestException(handle.getId(), exception);
        }

        var response = new ConversationResponse.Builder().setId(handle.getId())
            .setScenario(scenario.getName()).setMessages(handle.getConversation().getMessages()).build();

        var conveneinceReponse = new ConvenienceConversationResponse(response, answer.getContent());
        return conveneinceReponse;
    }

    @GetMapping("/conversations/{id}/messages")
    public List<MessageResponse> get_conversation_messages(
        @PathVariable("id") String id)
        throws ScenarioSessionNotFoundRestException, ScenarioSessionNotSpecifiedRestException {

        if (id == null || id.isBlank()) {
            throw new ScenarioSessionNotSpecifiedRestException();
        }
        Optional<ScenarioSession> session = this.scenarioSessionStore.getSession(id);
        if (session.isEmpty()) {
            throw new ScenarioSessionNotFoundRestException(id);
        }

        var messages = new ArrayList<MessageResponse>();
        for (var message : session.get().getConversation().getMessages()) {
            messages.add(new MessageResponse(message.getType().toString(), message.getContent()));
        }

        return messages;
    }

    @PostMapping("/conversations/{id}/messages")
    public MessageResponse continue_conversation(
        @PathVariable("id") String id,
        @RequestBody MessagePromptRequest request)
        throws ScenarioSessionNotFoundRestException, ScenarioSessionNotSpecifiedRestException,
        ConversationRestException {

        if (id == null || id.isBlank()) {
            throw new ScenarioSessionNotSpecifiedRestException();
        }
        Optional<ScenarioSession> session = this.scenarioSessionStore.getSession(id);
        if (session.isEmpty()) {
            throw new ScenarioSessionNotFoundRestException(id);
        }

        logger.info("Converstation.continue conversationId: " + session.get().getId());

        Message response;
        try {
            response = session.get().getConversation().addMessage(request.getPrompt()).respond();
        } catch (ConversationException exception) {
            throw new ConversationRestException(session.get().getId(), exception);
        }

        return new MessageResponse(response.getType().toString(), response.getContent());
    }

    private Optional<String> extractAccessToken(String authorizationHeader) {
        if (authorizationHeader != null) {
            var accessToken = authorizationHeader.replace("Bearer ", "");
            if (accessToken.length() > 0) {
                return Optional.of(accessToken);
            }
        }
        return Optional.empty();
    }

}
