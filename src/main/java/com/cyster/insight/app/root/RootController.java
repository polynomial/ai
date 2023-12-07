package com.cyster.insight.app.root;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.ai.core.llm.LlmClient;
import org.springframework.ai.openai.llm.OpenAiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cyster.insight.impl.openai.OpenAiFactoryImpl;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.CompletionResult;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionRequest.ChatCompletionRequestFunctionCall;
import com.theokanning.openai.completion.chat.ChatFunction;
import com.theokanning.openai.completion.chat.ChatFunctionCall;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.FunctionExecutor;
import com.theokanning.openai.service.OpenAiService;

import io.reactivex.functions.Consumer;

@RestController
public class RootController {

	private final LlmClient llmClient;
	private final OpenAiService openAiService;

	@Autowired
	public RootController(OpenAiClient openAiClient, OpenAiFactoryImpl openAiFactory) {
		this.llmClient = openAiClient;
		this.openAiService = openAiFactory.getService();
	}

	@GetMapping("/")
	public String index() {
		return "Hello world\n";
	}

	@GetMapping("/ai-check")
	public Map<String, String> generate(
			@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
		return Map.of("generation", llmClient.generate(message));
	}

	@GetMapping("/ai1")
	public Map<String, String> generate1(
			@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
		CompletionRequest completionRequest = CompletionRequest.builder().prompt(message).model("ada").echo(true)
				.build();

		CompletionResult result = this.openAiService.createCompletion(completionRequest);

		Map<String, String> response = new HashMap<String, String>();
		List<CompletionChoice> choices = result.getChoices();
		for (int i = 0; i < choices.size(); i++) {
			CompletionChoice choice = choices.get(i);
			response.put("Choice[" + i + "]", choice.getText());
		}
		return response;
	}

	@GetMapping("/ai2")
	public Map<String, String> generate2(
			@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
		var messages = new ArrayList<ChatMessage>();
		messages.add(new ChatMessage("system", "Your a funny guy"));
		messages.add(new ChatMessage("user", message));

		var chatCompletionRequest = ChatCompletionRequest.builder().model("gpt-3.5-turbo").messages(messages).build();

		var response = new HashMap<String, String>();

		var result = this.openAiService.createChatCompletion(chatCompletionRequest);

		var choices = result.getChoices();
		for (var i = 0; i < choices.size(); i++) {
			ChatCompletionChoice choice = choices.get(i);
			response.put("Choice[" + i + "]", choice.toString());
		}
		return response;
	}

	@GetMapping("/ai3")
	public Map<String, String> generate3(
			@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
		var messages = new ArrayList<ChatMessage>();
		messages.add(new ChatMessage("system", "Your a funny guy"));
		messages.add(new ChatMessage("user", message));

		var chatCompletionRequest = ChatCompletionRequest.builder().model("gpt-3.5-turbo").messages(messages).build();

		var response = new HashMap<String, String>();

		this.openAiService.streamChatCompletion(chatCompletionRequest).doOnError(Throwable::printStackTrace)
				.blockingForEach(new Consumer<ChatCompletionChunk>() {
					@Override
					public void accept(ChatCompletionChunk chunk) throws Exception {
						response.put(chunk.getId(), chunk.toString());
					}
				});

		return response;
	}

	public static class Weather {
		@JsonPropertyDescription("City and state, for example: León, Guanajuato")
		public String location;

		@JsonPropertyDescription("The temperature unit, can be 'celsius' or 'fahrenheit'")
		@JsonProperty(required = true)
		public WeatherUnit unit;
	}

	public enum WeatherUnit {
		CELSIUS, FAHRENHEIT;
	}

	public static class WeatherResponse {
		public String location;
		public WeatherUnit unit;
		public int temperature;
		public String description;

		public WeatherResponse(String location, WeatherUnit unit, int temperature, String description) {
			this.location = location;
			this.unit = unit;
			this.temperature = temperature;
			this.description = description;
		}
	}

	@GetMapping("/ai4")
	public Map<String, String> generate4(
			@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {

		ChatFunction weatherFunction = ChatFunction.builder().name("get_weather")
				.description("Get the current weather of a location").executor(Weather.class,
						w -> new WeatherResponse(w.location, w.unit, new Random().nextInt(50), "sunny"))
				.build();

		List<ChatFunction> functionList = new ArrayList<ChatFunction>();
		functionList.add(weatherFunction);
		FunctionExecutor functionExecutor = new FunctionExecutor(functionList);

		List<ChatMessage> messages = new ArrayList<>();
		ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), "Tell me the weather in Barcelona.");
		messages.add(userMessage);
		ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder().model("gpt-3.5-turbo-0613")
				.messages(messages).functions(functionExecutor.getFunctions())
				.functionCall(new ChatCompletionRequestFunctionCall("auto")).maxTokens(256).build();

		Map<String, String> response = new HashMap<String, String>();

		ChatMessage responseMessage = openAiService.createChatCompletion(chatCompletionRequest).getChoices().get(0)
				.getMessage();
		response.put("response1", responseMessage.toString());

		ChatFunctionCall functionCall = responseMessage.getFunctionCall();
		if (functionCall != null) {
			ChatMessage functionResponseMessage = functionExecutor
					.executeAndConvertToMessageHandlingExceptions(functionCall);
			messages.add(functionResponseMessage);
			response.put("response2", functionResponseMessage.toString());
		}

		return response;
	}
}
