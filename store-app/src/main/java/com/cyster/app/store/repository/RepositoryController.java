package com.cyster.app.store.repository;

import java.util.List;
import java.util.Set;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cyster.rest.RestException;
import com.cyster.store.SimpleVectorStoreService;

//
// Load example (note, replace E_HOME):
// curl -s -H 'Content-Type: application/json;charset=UTF-8' 'http://localhost:8070/repositories' -d '{ "name": "extole-code", "uriPrefix": "https://github.com/extole/pluribus/tree/master/context/api/src/main/java/com/extole/api/", "loadPath": "$E_HOME/code/pluribus/context/api/src/main/java/com/extole/api/" }'
//

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
    public ResponseEntity<Void> load_store(@RequestBody RepositoryLoadRequest load) throws RestException {
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

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/repositories/{name}")
    public ResponseEntity<Void> delete_store(@PathVariable("name") String name) {
        this.storeService.deleteRespository(name);
        return ResponseEntity.ok().build();
    }

}
