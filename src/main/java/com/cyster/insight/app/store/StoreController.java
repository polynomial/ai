package com.cyster.insight.app.store;

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

import com.cyster.ai.vector.simple.SimpleVectorStoreService;
import com.cyster.insight.app.RestException;

@RestController
public class StoreController {

    private SimpleVectorStoreService storeService;

    public StoreController(SimpleVectorStoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping("/stores")
    public Set<String> index() {
        return storeService.getStores();
    }

    @GetMapping("/stores/{name}/query")
    public List<Document> query(
        @PathVariable("name") String name, @RequestParam String query) {
        return this.storeService.getStore(name).similaritySearch(SearchRequest.query(query));
    }

    @PostMapping("/stores")
    public Boolean load_store(@RequestBody StoreLoadRequest load) throws RestException {
        if (load.getName() == null) {
            throw new RestException(HttpStatus.BAD_REQUEST, "name not specified");
        }
        if (load.getLoadPath() == null) {
            throw new RestException(HttpStatus.BAD_REQUEST, "loadPath not specified");
        }
        if (load.getUriPrefix() == null) {
            throw new RestException(HttpStatus.BAD_REQUEST, "uriPrefix not specified");
        }
        this.storeService.buildStore(load.getName(), load.getUriPrefix(), load.getLoadPath());
        return Boolean.TRUE;
    }

}
