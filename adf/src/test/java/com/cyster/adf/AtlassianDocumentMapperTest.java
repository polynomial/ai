package com.cyster.adf;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AtlassianDocumentMapperTest {
    
    @Test    
    public void testText() {
        var expectedResult = """
{
  "version" : 1,
  "type" : "doc",
  "content" : [ {
    "type" : "paragraph",
    "content" : [ {
      "type" : "text",
      "text" : "This is Sparta"
    } ]
  } ]
}
""";
        var markdown = "This is Sparta ";

        checkResult(markdown, expectedResult);  
    }
    
    
    @Test    
    public void testWithEmphasisAndStrongEmphasis() {   
        var expectedResult = """
{
  "version" : 1,
  "type" : "doc",
  "content" : [ {
    "type" : "paragraph",
    "content" : [ {
      "type" : "text",
      "text" : "text "
    }, {
      "type" : "text",
      "text" : "empahsis",
      "marks" : [ {
        "type" : "em"
      } ]
    }, {
      "type" : "text",
      "text" : " after "
    }, {
      "type" : "text",
      "text" : "bold",
      "marks" : [ {
        "type" : "strong"
      } ]
    }, {
      "type" : "text",
      "text" : " and "
    }, {
      "type" : "text",
      "text" : "crazy ",
      "marks" : [ {
        "type" : "em"
      } ]
    }, {
      "type" : "text",
      "text" : "both",
      "marks" : [ {
        "type" : "em"
      }, {
        "type" : "strong"
      } ]
    }, {
      "type" : "text",
      "text" : " emphasis",
      "marks" : [ {
        "type" : "em"
      } ]
    } ]
  } ]
}
""";
        var markdown = "text *empahsis* after **bold** and *crazy **both** emphasis*";

        checkResult(markdown, expectedResult);        
    }
    
    @Test    
    public void testCode() {
        var expectedResult = """
{
  "version" : 1,
  "type" : "doc",
  "content" : [ {
    "type" : "paragraph",
    "content" : [ {
      "type" : "text",
      "text" : "Check this out: "
    }, {
      "type" : "text",
      "text" : "alert(\\\"testing\\\");",
      "marks" : [ {
        "type" : "code"
      } ]
    } ]
  } ]
}
""";
        
        var markdown = "Check this out: `alert(\"testing\");`";

        checkResult(markdown, expectedResult);                
    }
    
    @Test    
    public void testCodeBlock() {
        var expectedResult = """
{
  "version" : 1,
  "type" : "doc",
  "content" : [ {
    "type" : "paragraph",
    "content" : [ {
      "type" : "text",
      "text" : "Check this out:"
    } ]
  }, {
    "type" : "code",
    "content" : [ {
      "type" : "text",
      "text" : "alert(\\\"testing\\\");\\n"
    } ]
  } ]
}
""";
   
        var markdown = """
Check this out: 
```
alert(\"testing\");
```
""";


        checkResult(markdown, expectedResult);                    
    }
    
    @Test    
    public void testFencedCodeBlock() {
        var expectedResult = """
{
  "version" : 1,
  "type" : "doc",
  "content" : [ {
    "type" : "paragraph",
    "content" : [ {
      "type" : "text",
      "text" : "Check this out:"
    } ]
  }, {
    "type" : "code",
    "content" : [ {
      "type" : "text",
      "text" : "alert(\\\"testing\\\");\\n"
    } ],
    "attrs" : {
      "language" : "javascript"
    }
  } ]
}
""";
   
        var markdown = """
Check this out: 
```javascript
alert(\"testing\");
```
""";


        checkResult(markdown, expectedResult);                    
    }
    
    @Test    
    public void testLink() {        
        var expectedResult = """
{
  "version" : 1,
  "type" : "doc",
  "content" : [ {
    "type" : "paragraph",
    "content" : [ {
      "type" : "text",
      "text" : "Check this link: "
    }, {
      "type" : "text",
      "text" : "https://github.com/mcyster/ai",
      "marks" : [ {
        "type" : "link",
        "attrs" : {
          "href" : "https://github.com/mcyster/ai"
        }
      } ]
    } ]
  } ]
}
""";
        var markdown = """
Check this link: <https://github.com/mcyster/ai>
""";

        checkResult(markdown, expectedResult);
    }

    @Test    
    public void testFormattedLink() {       
        var expectedResult = """
{
  "version" : 1,
  "type" : "doc",
  "content" : [ {
    "type" : "paragraph",
    "content" : [ {
      "type" : "text",
      "text" : "Check this "
    }, {
      "type" : "text",
      "text" : "AI Cyster",
      "marks" : [ {
        "type" : "link",
        "attrs" : {
          "href" : "https://github.com/mcyster/ai"
        }
      } ]
    }, {
      "type" : "text",
      "text" : "AI Cyster",
      "marks" : [ {
        "type" : "link",
        "attrs" : {
          "href" : "https://github.com/mcyster/ai"
        }
      } ]
    } ]
  } ]
}
""";
        var markdown = """
Check this [AI Cyster](https://github.com/mcyster/ai)
""";

        checkResult(markdown, expectedResult);       
    }
    
    private void checkResult(String markdown, String expectedResult) {
        ObjectMapper jsonMapper = new ObjectMapper();
        AtlassianDocumentMapper mapper = new AtlassianDocumentMapper();

        var result = mapper.fromMarkdown(markdown);
        
        System.out.println(result.toPrettyString());
        
        try {
            assertTrue(result.equals(jsonMapper.readTree(expectedResult)), "Generated Json did not match expected result");
        } catch (JsonProcessingException exception) {
            fail("Failed to parse expectedResult", exception);
        }
    }
}
