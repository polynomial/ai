package com.cyster.app.store.repository;

import java.util.List;
import java.util.Set;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cyster.rest.RestException;
import com.cyster.store.SimpleVectorStoreService;

@RestController
public class RepositoryController {

    private SimpleVectorStoreService storeService;

    public RepositoryController(SimpleVectorStoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping("/repositories")
    public Set<String> index() {
        return storeService.getRepositories();
    }

    @GetMapping("/repositories/{name}/query")
    public List<Document> query(
        @PathVariable("name") String name, @RequestParam String query) {
        return this.storeService.getRepository(name).similaritySearch(SearchRequest.query(query));
    }

    @PostMapping("/repositories")
    public Boolean load_store(@RequestBody RepositoryLoadRequest load) throws RestException {
        if (load.getName() == null) {
            throw new RestException(HttpStatus.BAD_REQUEST, "name not specified");
        }
        if (load.getLoadPath() == null) {
            throw new RestException(HttpStatus.BAD_REQUEST, "loadPath not specified");
        }
        if (load.getUriPrefix() == null) {
            throw new RestException(HttpStatus.BAD_REQUEST, "uriPrefix not specified");
        }
        this.storeService.buildRepository(load.getName(), load.getUriPrefix(), load.getLoadPath());
        return Boolean.TRUE;
    }

}
