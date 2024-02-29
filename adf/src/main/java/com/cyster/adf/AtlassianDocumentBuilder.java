package com.cyster.adf;

import java.util.Iterator;
import java.util.Stack;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AtlassianDocumentBuilder {   
    private enum Mark {
        code,
        em,
        link,
        strike,
        strong,
        subsup,
        textColor,
        underline
    };
    
    private ObjectNode document;
    private Stack<ArrayNode> contentStack = new Stack<>();
    private ArrayNode marks = JsonNodeFactory.instance.arrayNode();
    
    public AtlassianDocumentBuilder() {
        document = JsonNodeFactory.instance.objectNode();
        document.put("version", 1);
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

    public AtlassianDocumentBuilder startCode() {
        addMark(Mark.code);
        return this;
    }
    
    public AtlassianDocumentBuilder endCode() {
        removeMark(Mark.code);
        return this;
    }
    
    public AtlassianDocumentBuilder startEmphasis() {
        addMark(Mark.em);
        return this;
    }
    
    public AtlassianDocumentBuilder endEmphasis() {
        removeMark(Mark.em);
        return this;
    }

    public AtlassianDocumentBuilder startLink(String href) {
        ObjectNode attributes = JsonNodeFactory.instance.objectNode();
        attributes.put("href", href);        
        addMark(Mark.link, attributes);
        return this;
    }
    
    public AtlassianDocumentBuilder endLink() {
        removeMark(Mark.link);
        return this;
    }
     
    public AtlassianDocumentBuilder startStrike() {
        addMark(Mark.strike);
        return this;
    }
    
    public AtlassianDocumentBuilder endStrike() {
        removeMark(Mark.strike);
        return this;
    }
    
    public AtlassianDocumentBuilder startStrong() {
        addMark(Mark.strong);
        return this;
    }
    
    public AtlassianDocumentBuilder endStrong() {
        removeMark(Mark.strong);
        return this;
    }

    public AtlassianDocumentBuilder startSuperscript() {
        ObjectNode attributes = JsonNodeFactory.instance.objectNode();
        attributes.put("type", "sup");  
        addMark(Mark.subsup, attributes);
        return this;
    }
    
    public AtlassianDocumentBuilder endSubsuperscript() {
        removeMark(Mark.subsup);
        return this;
    }

    public AtlassianDocumentBuilder startSubscript() {
        ObjectNode attributes = JsonNodeFactory.instance.objectNode();
        attributes.put("type", "sub");  
        addMark(Mark.subsup, attributes);
        return this;
    }
    
    public AtlassianDocumentBuilder endSubscript() {
        removeMark(Mark.subsup);
        return this;
    }
  
    
    public AtlassianDocumentBuilder startTextColor(String color) {
        ObjectNode attributes = JsonNodeFactory.instance.objectNode();
        attributes.put("color", color);  
        addMark(Mark.textColor);
        return this;
    }
    
    public AtlassianDocumentBuilder endTextColor() {
        removeMark(Mark.textColor);
        return this;
    }

    public AtlassianDocumentBuilder startUnderline() {
        addMark(Mark.underline);
        return this;
    }
    
    public AtlassianDocumentBuilder endUnderline() {
        removeMark(Mark.underline);
        return this;
    }
   
    private ObjectNode addMark(Mark mark, ObjectNode attributes) {
        ObjectNode node = addMark(mark);
        node.set("attrs", attributes);
       
        return node;
    }
    
    private ObjectNode addMark(Mark mark) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("type", mark.toString());
        marks.add(node);
        
        return node;
    }
    
    private void removeMark(Mark mark) {
        for (Iterator<JsonNode> iterator = marks.iterator(); iterator.hasNext(); ) {
            JsonNode currentNode = iterator.next();
            if (currentNode.has("type") && currentNode.get("type").textValue().equals(mark.toString())) {
                iterator.remove();
            }
        }
    }
    
    public AtlassianDocumentBuilder addText(String text) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("type", "text");
        node.put("text", text);
        
        if (!marks.isEmpty()) {
            node.set("marks", marks.deepCopy());
        }

        this.contentStack.peek().add(node);
        
        return this;
    }
    
    public JsonNode getDocument() {
        return this.document;
    }
}
