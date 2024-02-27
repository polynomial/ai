package com.cyster.adf;

import org.junit.jupiter.api.Test;

public class AtlassianDocumentMapperTest {

    @Test    
    public void plainText() {        
        var markdown = "This is Sparta ";

        AtlassianDocumentMapper mapper = new AtlassianDocumentMapper();

        System.out.println(mapper.fromMarkdown(markdown).toPrettyString());        
    }
    
    
    @Test    
    public void textWithEmphasisAndStrongEmphasis() {        
        var markdown = "text *empahsis* after **bold** and *crazy **both** emphasis*";

        AtlassianDocumentMapper mapper = new AtlassianDocumentMapper();

        System.out.println(mapper.fromMarkdown(markdown).toPrettyString());        
    }
    
    @Test    
    public void textCode() {        
        var markdown = "Check this out: `alert(\"testing\");`";

        AtlassianDocumentMapper mapper = new AtlassianDocumentMapper();

        System.out.println(mapper.fromMarkdown(markdown).toPrettyString());        
    }
    
    @Test    
    public void textCodeBlock() {        
        var markdown = """
Check this out: 
```javascript
alert(\"testing\");
```
""";

        AtlassianDocumentMapper mapper = new AtlassianDocumentMapper();

        System.out.println(mapper.fromMarkdown(markdown).toPrettyString());        
    }
}
