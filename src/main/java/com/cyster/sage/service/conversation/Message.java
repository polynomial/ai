package com.cyster.sage.service.conversation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Message {

    public enum Type {
        SYSTEM("System"), AI("Ai"), USER("User"), ERROR("Error"), INFO("Info"), FUNCTION_CALL("Function Call"),
        FUNCTION_RESULT("Function Result");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }

    private Type type;
    private String content;

    public Message(Type type, String content) {
        this.type = type;
        this.content = content;
    }

    public Message(String content) {
        this.type = Type.USER;
        this.content = content;
    }

    public Type getType() {
        return this.type;
    }

    public String getContent() {
        return this.content;
    }

    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
