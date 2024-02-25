package com.cyster.md_adf;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;

public class MarkdownMapperTest {

    @Test
    public void commonMark() {

        Parser parser = Parser.builder().build();
        Node document = parser.parse("This is *Sparta*");
        HtmlRenderer renderer = HtmlRenderer.builder().build();

        assertTrue(renderer.render(document).equals("<p>This is <em>Sparta</em></p>\n"), "Check a simple HTML mapping");

    }

}
