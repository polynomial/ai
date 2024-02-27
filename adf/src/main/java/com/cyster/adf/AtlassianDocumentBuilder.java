package com.cyster.adf;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AtlassianDocumentBuilder {
    enum Mark {
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
    private Set<Mark> marks = new HashSet<>();
    
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
        marks.add(Mark.code);
        return this;
    }
    
    public AtlassianDocumentBuilder endCode() {
        marks.remove(Mark.code);
        return this;
    }
    
    public AtlassianDocumentBuilder startEmphasis() {
        marks.add(Mark.em);
        return this;
    }
    
    public AtlassianDocumentBuilder endEmphasis() {
        marks.remove(Mark.em);
        return this;
    }

    public AtlassianDocumentBuilder startLink() {
        marks.add(Mark.link);
        return this;
    }
    
    public AtlassianDocumentBuilder endLink() {
        marks.remove(Mark.link);
        return this;
    }
     
    public AtlassianDocumentBuilder startStrike() {
        marks.add(Mark.strike);
        return this;
    }
    
    public AtlassianDocumentBuilder endStrike() {
        marks.remove(Mark.strike);
        return this;
    }
    
    public AtlassianDocumentBuilder startStrong() {
        marks.add(Mark.strong);
        return this;
    }
    
    public AtlassianDocumentBuilder endStrong() {
        marks.remove(Mark.strong);
        return this;
    }

    public AtlassianDocumentBuilder startSubsup() {
        marks.add(Mark.subsup);
        return this;
    }
    
    public AtlassianDocumentBuilder endSubsup() {
        marks.remove(Mark.subsup);
        return this;
    }
  
    public AtlassianDocumentBuilder startTextColor() {
        marks.add(Mark.textColor);
        return this;
    }
    
    public AtlassianDocumentBuilder endTextColor() {
        marks.remove(Mark.textColor);
        return this;
    }

    public AtlassianDocumentBuilder startUnderline() {
        marks.add(Mark.textColor);
        return this;
    }
    
    public AtlassianDocumentBuilder endUnderline() {
        marks.remove(Mark.textColor);
        return this;
    }
    
    public AtlassianDocumentBuilder addText(String text) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("type", "text");
        node.put("text", text);
        
        if (!marks.isEmpty()) {
            var marksNode = JsonNodeFactory.instance.arrayNode();
            for(var mark: marks) {
                var markNode = JsonNodeFactory.instance.objectNode();
                markNode.put("type", mark.name().toLowerCase());
                marksNode.add(markNode);
            }
            node.set("marks", marksNode);
        }

        this.contentStack.peek().add(node);
        
        return this;
    }
    
    public JsonNode getDocument() {
        return this.document;
    }
}
