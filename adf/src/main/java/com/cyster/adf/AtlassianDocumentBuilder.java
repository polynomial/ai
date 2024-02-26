package com.cyster.adf;

import java.util.Stack;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AtlassianDocumentBuilder {
    private ObjectNode document;
    Stack<ArrayNode> contentStack = new Stack<>();

    public AtlassianDocumentBuilder() {
        document = JsonNodeFactory.instance.objectNode();
        document.put("version", 3);
        document.put("type", "doc");
        
        var content = JsonNodeFactory.instance.arrayNode();
        contentStack.push(content);
        document.set("content", content);
    }
 
    public AtlassianDocumentBuilder addDocument() {    
        return this;
    }

    public AtlassianDocumentBuilder startParagraph() {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("type", "paragraph");
        
        ArrayNode content = JsonNodeFactory.instance.arrayNode();
        this.contentStack.peek().add(node);

        contentStack.push(content);
        node.set("content", content);

        return this;
    }
    
    public AtlassianDocumentBuilder endParagraph() {
        contentStack.pop();
        return this;
    }
        
    public AtlassianDocumentBuilder addText(String text) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("type", "text");
        node.put("text", text);

        this.contentStack.peek().add(node);
        
        return this;
    }
    
    public JsonNode getDocument() {
        return this.document;
    }
}
