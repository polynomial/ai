package com.cyster.adf;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladsch.flexmark.ast.AutoLink;
import com.vladsch.flexmark.ast.BlockQuote;
import com.vladsch.flexmark.ast.BulletList;
import com.vladsch.flexmark.ast.BulletListItem;
import com.vladsch.flexmark.ast.Code;
import com.vladsch.flexmark.ast.CodeBlock;
import com.vladsch.flexmark.ast.Emphasis;
import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.ast.OrderedList;
import com.vladsch.flexmark.ast.OrderedListItem;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.ast.StrongEmphasis;
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
        visitor.addHandler(new VisitHandler<Code>(Code.class, new CodeVisitor()));        
        visitor.addHandler(new VisitHandler<Emphasis>(Emphasis.class, new EmphasisVisitor()));
        visitor.addHandler(new VisitHandler<StrongEmphasis>(StrongEmphasis.class, new StrongEmphasisVisitor()));
        visitor.addHandler(new VisitHandler<AutoLink>(AutoLink.class, new AutoLinkVisitor()));
        visitor.addHandler(new VisitHandler<BlockQuote>(BlockQuote.class, new BlockQuoteVisitor()));        
        visitor.addHandler(new VisitHandler<FencedCodeBlock>(FencedCodeBlock.class, new FencedCodeBlockVisitor()));        
        visitor.addHandler(new VisitHandler<CodeBlock>(CodeBlock.class, new CodeBlockVisitor()));        
        visitor.addHandler(new VisitHandler<BulletList>(BulletList.class, new BulletListVisitor()));        
        visitor.addHandler(new VisitHandler<BulletListItem>(BulletListItem.class, new BulletListItemVisitor()));        
        visitor.addHandler(new VisitHandler<OrderedList>(OrderedList.class, new OrderedListVisitor()));        
        visitor.addHandler(new VisitHandler<OrderedListItem>(OrderedListItem.class, new OrderedListItemVisitor()));        

        visitor.addHandler(new VisitHandler<Link>(Link.class, new LinkVisitor()));        
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
            if (node.getNodeName().equalsIgnoreCase("AutoLink")) {
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

    public class CodeVisitor implements Visitor<Code> {
        @Override
        public void visit(Code node) {
            AtlassianDocumentMarkdownVisitor.this.builder.startCode();
            AtlassianDocumentMarkdownVisitor.this.visitor.visitChildren(node);
            AtlassianDocumentMarkdownVisitor.this.builder.endCode();
        }
    }
    
    public class EmphasisVisitor implements Visitor<Emphasis> {
        @Override
        public void visit(Emphasis node) {
            AtlassianDocumentMarkdownVisitor.this.builder.startEmphasis();
            AtlassianDocumentMarkdownVisitor.this.visitor.visitChildren(node);
            AtlassianDocumentMarkdownVisitor.this.builder.endEmphasis();
        }
    }

    public class StrongEmphasisVisitor implements Visitor<StrongEmphasis> {
        @Override
        public void visit(StrongEmphasis node) {
            AtlassianDocumentMarkdownVisitor.this.builder.startStrong();
            AtlassianDocumentMarkdownVisitor.this.visitor.visitChildren(node);
            AtlassianDocumentMarkdownVisitor.this.builder.endStrong();
        }
    }

    public class AutoLinkVisitor implements Visitor<AutoLink> {
        @Override
        public void visit(AutoLink node) {
            AtlassianDocumentMarkdownVisitor.this.builder.startLink(node.getUrl().unescape());
            AtlassianDocumentMarkdownVisitor.this.builder.addText(node.getUrl().unescape());
            AtlassianDocumentMarkdownVisitor.this.visitor.visitChildren(node);
            AtlassianDocumentMarkdownVisitor.this.builder.endLink();
        }
    }

    public class LinkVisitor implements Visitor<Link> {
        @Override
        public void visit(Link node) {
            AtlassianDocumentMarkdownVisitor.this.builder.startLink(node.getUrl().unescape());
            AtlassianDocumentMarkdownVisitor.this.visitor.visitChildren(node);
            AtlassianDocumentMarkdownVisitor.this.builder.endLink();
        }
    }

    public class BlockQuoteVisitor implements Visitor<BlockQuote> {
        @Override
        public void visit(BlockQuote node) {
            AtlassianDocumentMarkdownVisitor.this.builder.startBlockQuote();
            AtlassianDocumentMarkdownVisitor.this.visitor.visitChildren(node);
            AtlassianDocumentMarkdownVisitor.this.builder.endBlockQuote();
        }
    }

    public class FencedCodeBlockVisitor implements Visitor<FencedCodeBlock> {
        @Override
        public void visit(FencedCodeBlock node) {
            String language = node.getInfo().unescape();
            if (language == null || language.isEmpty()) {
                AtlassianDocumentMarkdownVisitor.this.builder.startCodeBlock();                
            } else {
                AtlassianDocumentMarkdownVisitor.this.builder.startCodeBlock(node.getInfo().unescape());                
            }
            AtlassianDocumentMarkdownVisitor.this.visitor.visitChildren(node);
            AtlassianDocumentMarkdownVisitor.this.builder.endCodeBlock();
        }
    }
    
    public class CodeBlockVisitor implements Visitor<CodeBlock> {
        @Override
        public void visit(CodeBlock node) {
            AtlassianDocumentMarkdownVisitor.this.builder.startCodeBlock();
            AtlassianDocumentMarkdownVisitor.this.visitor.visitChildren(node);
            AtlassianDocumentMarkdownVisitor.this.builder.endCodeBlock();
        }
    }

    public class BulletListVisitor implements Visitor<BulletList> {
        @Override
        public void visit(BulletList node) {
            AtlassianDocumentMarkdownVisitor.this.builder.startBulletList();
            AtlassianDocumentMarkdownVisitor.this.visitor.visitChildren(node);
            AtlassianDocumentMarkdownVisitor.this.builder.endBulletList();

        }
    }

    public class BulletListItemVisitor implements Visitor<BulletListItem> {
        @Override
        public void visit(BulletListItem node) {
            AtlassianDocumentMarkdownVisitor.this.builder.startListItem();
            AtlassianDocumentMarkdownVisitor.this.visitor.visitChildren(node);
            AtlassianDocumentMarkdownVisitor.this.builder.endListItem();
        }
    }
    
    public class OrderedListVisitor implements Visitor<OrderedList> {
        @Override
        public void visit(OrderedList node) {
            AtlassianDocumentMarkdownVisitor.this.builder.startOrderedList(); 
            AtlassianDocumentMarkdownVisitor.this.visitor.visitChildren(node);
            AtlassianDocumentMarkdownVisitor.this.builder.endOrderedList();
        }
    }

    public class OrderedListItemVisitor implements Visitor<OrderedListItem> {
        @Override
        public void visit(OrderedListItem node) {
            AtlassianDocumentMarkdownVisitor.this.builder.startListItem();
            AtlassianDocumentMarkdownVisitor.this.visitor.visitChildren(node);
            AtlassianDocumentMarkdownVisitor.this.builder.endListItem();
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
