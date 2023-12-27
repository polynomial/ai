package com.cyster.insight.app.conversation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.cyster.insight.service.conversation.ConversationException;
import com.cyster.insight.service.conversation.Message;
import com.cyster.insight.service.scenario.Scenario;
import com.cyster.insight.service.scenariosession.ScenarioSession;
import com.cyster.insight.service.scenariosession.ScenarioSessionStore;
import com.cyster.insight.service.scenariostore.ScenarioStore;
import com.cyster.insight.service.scenariostore.ScenarioStoreException;

@RestController
public class ConversationController {
    private ScenarioSessionStore scenarioSessionStore;
    private ScenarioStore scenarioStore;

    public ConversationController(ScenarioSessionStore scenarioSessionStore, ScenarioStore scenarioStore) {
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

        Scenario scenario;
        try {
            scenario = this.scenarioStore.getScenario(request.getScenarioName());
        } catch (ScenarioStoreException exception) {
            throw new ScenarioNameNotFoundRestException(request.getScenarioName());
        }

        Map<String, String> context;
        if (request.getContext() == null) {
            context = Collections.emptyMap();
        } else {
            context = request.getContext();
        }

        var builder = scenario.createConversation().setContext(context);
        token.ifPresent(accessToken -> builder.setAccessToken(accessToken));
        var conversation = builder.start();

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

        Scenario scenario;
        try {
            scenario = this.scenarioStore.getScenario(request.getScenario());
        } catch (ScenarioStoreException exception) {
            throw new ScenarioNameNotFoundRestException(request.getScenario());
        }

        Map<String, String> context;
        if (request.getContext() == null) {
            context = Collections.emptyMap();
        } else {
            context = request.getContext();
        }

        var builder = scenario.createConversation().setContext(context);
        token.ifPresent(accessToken -> builder.setAccessToken(accessToken));
        var conversation = builder.start();

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

        System.out.println("Converstation.continue conversationId: " + session.get().getId());

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
