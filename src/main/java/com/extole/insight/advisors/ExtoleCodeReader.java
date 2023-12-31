package com.extole.insight.advisors;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

public class ExtoleCodeReader implements DocumentReader {

    public static final String CHARSET_METADATA = "charset";

    public static final String SOURCE_METADATA = "source";

    private final Resource resource;

    /**
     * @return Character set to be used when loading data from the
     */
    private Charset charset = StandardCharsets.UTF_8;

    private Map<String, Object> customMetadata = new HashMap<>();

    public ExtoleCodeReader(String resourceUrl) {
        this(new DefaultResourceLoader().getResource(resourceUrl));
    }

    public ExtoleCodeReader(Resource resource) {
        this.resource = resource;
    }

    public Charset getCharset() {
        return this.charset;
    }

    public Map<String, Object> getCustomMetadata() {
        return this.customMetadata;
    }

    @Override
    public List<Document> get() {
        try {
            String filePath = this.resource.getFilename();
                
            String document = StreamUtils.copyToString(this.resource.getInputStream(), this.charset);

            this.customMetadata.put(CHARSET_METADATA, this.charset.name());
            this.customMetadata.put(SOURCE_METADATA, this.resource.getFilename());

            return List.of(new Document(document, this.customMetadata));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}