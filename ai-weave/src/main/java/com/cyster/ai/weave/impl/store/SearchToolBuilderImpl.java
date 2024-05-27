package com.cyster.ai.weave.impl.store;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.cyster.ai.weave.service.advisor.SearchTool;
import com.cyster.ai.weave.service.advisor.SearchTool.Document;

import io.github.stefanbratanov.jvm.openai.CreateVectorStoreRequest;
import io.github.stefanbratanov.jvm.openai.ExpiresAfter;
import io.github.stefanbratanov.jvm.openai.OpenAI;
import io.github.stefanbratanov.jvm.openai.UploadFileRequest;

public class SearchToolBuilderImpl<CONTEXT> implements SearchTool.Builder<CONTEXT> {
    private OpenAI openAi;
    private List<Document> documents = new ArrayList<Document>();
    private String name;
    
    public SearchToolBuilderImpl(OpenAI openAi) {
        this.openAi = openAi;
    }

    @Override
    public SearchToolBuilderImpl<CONTEXT> withName(String name) {
        this.name = name;    
        return this;
    }
    
    @Override
    public SearchToolBuilderImpl<CONTEXT> addDocument(String name, String contents) {
        this.documents.add(new StringDocument(name, contents));
        return this;
    }

    @Override
    public SearchToolBuilderImpl<CONTEXT> addDocument(Document document) {
        this.documents.add(document);
        return this;
    }

    @Override
    public SearchTool<CONTEXT> create() {
        List<String> files = new ArrayList<String>();
        
        try {
            var directory = Files.createTempDirectory("store-" + safeName(this.name));

            for(var document: this.documents) {
                var name = document.getName();
                var extension = ".txt";
                        
                int lastDotIndex = name.lastIndexOf('.');
                if (lastDotIndex != -1) {
                    extension = name.substring(lastDotIndex + 1);
                    name = name.substring(0, lastDotIndex);
                }
            
                var safeName = safeName(name);
                var safeExtension = "." + safeName(extension);
          
                Path realFile = Paths.get(directory.toString(), safeName + safeExtension);
    
                Files.createFile(realFile);
                try (InputStream inputStream = document.getInputStream()) {
                    Files.copy(inputStream, realFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    
                    var fileUpload = new UploadFileRequest(realFile, "assistants");
                    var file = this.openAi.filesClient().uploadFile(fileUpload);
                    files.add(file.id());
                }
                Files.delete(realFile);
            }
            
            if (directory != null) {
                Files.delete(directory);
            }            
        } catch (IOException e) {
            // TODO better error
            throw new RuntimeException(e);
        }
     
        var createVectorStoreRequest = CreateVectorStoreRequest.newBuilder()
            .name(this.name)
            .fileIds(files)
            //.metadata(null)
            .expiresAfter(ExpiresAfter.lastActiveAt(1))
            .build();
        
        var vectorStore = this.openAi.vectorStoresClient().createVectorStore(createVectorStoreRequest);
                        
        return new SearchToolImpl<CONTEXT>( new ArrayList<>(Arrays.asList(vectorStore)));
    }
 
    private static String safeName(String name) {
        return name.replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9_]", "");
    }
    
    private class StringDocument implements Document {
        private String name;
        private String contents;
        
        public StringDocument(String name, String contents) {
            this.name = name;
            this.contents = contents;
        }
        
        @Override
        public String getName() {
            return name;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(contents.getBytes());
        } 
    }
    
}
