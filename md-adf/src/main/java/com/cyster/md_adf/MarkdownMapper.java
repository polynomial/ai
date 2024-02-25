package com.cyster.md_adf;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

// https://github.com/vsch/flexmark-java/wiki/Usage
// https://developer.atlassian.com/cloud/jira/platform/apis/document/structure/
// https://unpkg.com/@atlaskit/adf-schema@35.5.1/dist/json-schema/v1/full.json

public class MarkdownMapper {
    private MutableDataSet options;

    MarkdownMapper() {
        MutableDataSet options = new MutableDataSet();
        // uncomment to set optional extensions
        // options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create(),
        // StrikethroughExtension.create()));

        // uncomment to convert soft-breaks to hard breaks
        // options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");

        // Instantiate a parser
        this.options = options;

    }

    JsonNode toAdobeDocumentFromat(String markdown) {
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        Parser parser = Parser.builder().build();

        // You can re-use parser and renderer instances
        Node document = parser.parse("This is *Sparta*");
        String html = renderer.render(document); // "<p>This is <em>Sparta</em></p>\n"
        System.out.println(html);

        return null;
    }
}
