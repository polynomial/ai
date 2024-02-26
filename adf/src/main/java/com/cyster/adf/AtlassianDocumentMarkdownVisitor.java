package com.cyster.adf;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeVisitor;
import com.vladsch.flexmark.util.ast.VisitHandler;
import com.vladsch.flexmark.util.ast.Visitor;

class AtlassianDocumentMarkdownVisitor {
    private AtlassianDocumentBuilder builder = new AtlassianDocumentBuilder();
    private NodeVisitor visitor = new NodeVisitor();
    
    JsonNode generate(String markdown) {
        this.builder = new AtlassianDocumentBuilder();
        this.visitor = new NodeVisitor();

        visitor.addHandler(new VisitHandler<Document>(Document.class, new DocumentVisitor()));        
        visitor.addHandler(new VisitHandler<Paragraph>(Paragraph.class, new ParagraphVisitor()));        
        visitor.addHandler(new VisitHandler<Text>(Text.class, new TextVisitor()));        
        
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        
        System.out.println(dumpToString(document, 0));
        System.out.println("\n");
        
        visitor.visit(document);
        
        return builder.getDocument();
    }

    private String dumpToString(Node node, int depth) {
        String value = "";
        do {            
            value = value + " ".repeat(depth) + "<" + node.getNodeName() + ">\n";
            
            if (node.getNodeName().equalsIgnoreCase("text")) {
                value = value + " ".repeat(depth + 1) + node.getChars().unescape() + "\n";
            }
            
            if (node.hasChildren()) {
                value = value + dumpToString(node.getFirstChild(), depth + 1);
            }
            value = value + " ".repeat(depth) + "</" + node.getNodeName() + ">\n";
            
            node = node.getNext();
        } while(node != null);
          
        return value;
    }

    public class DocumentVisitor implements Visitor<Document> {
        @Override
        public void visit(Document node) {
            AtlassianDocumentMarkdownVisitor.this.builder.addDocument();
            AtlassianDocumentMarkdownVisitor.this.visitor.visitChildren(node);
        }
    }

    public class ParagraphVisitor implements Visitor<Paragraph> {        
        @Override
        public void visit(Paragraph node) {
            AtlassianDocumentMarkdownVisitor.this.builder.startParagraph();
            AtlassianDocumentMarkdownVisitor.this.visitor.visitChildren(node);
            AtlassianDocumentMarkdownVisitor.this.builder.endParagraph();
        }
    }
    
    public class TextVisitor implements Visitor<Text> {
        @Override
        public void visit(Text node) {
            AtlassianDocumentMarkdownVisitor.this.builder.addText(node.getChars().unescape());
            AtlassianDocumentMarkdownVisitor.this.visitor.visitChildren(node);
        }
    }
 
    
}
