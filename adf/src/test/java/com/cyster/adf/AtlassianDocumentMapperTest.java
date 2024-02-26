package com.cyster.adf;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class AtlassianDocumentMapperTest {

    @Test    
    public void dumpMarkdown() {        
//        var markdown = "This is *Sparta*";
        var markdown = "before *Sparta* after";

        AtlassianDocumentMapper mapper = new AtlassianDocumentMapper();

        System.out.println(mapper.fromMarkdown(markdown).toPrettyString());
        
        assertTrue(false, "fail");
    }
}
