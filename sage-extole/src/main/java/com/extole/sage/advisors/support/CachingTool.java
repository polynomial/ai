package com.extole.sage.advisors.support;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.cyster.sherpa.impl.advisor.Tool;
import com.cyster.sherpa.impl.advisor.ToolException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class CachingTool<Request, Context> implements Tool<Request, Context> {
    private final Tool<Request, Context> tool;
    private final Cache<Key<Request, Context>, Object> cache;
    
    CachingTool(Tool<Request, Context> tool) {
        this.tool = tool;
        
        this.cache = CacheBuilder.newBuilder() // TODO add builder, make configurable
            .maximumSize(1000)
            .expireAfterWrite(2, TimeUnit.HOURS)  
            .build();
    }

    @Override
    public String getName() {
        return this.tool.getName();
    }

    @Override
    public String getDescription() {
        return this.tool.getDescription();
    }

    @Override
    public Class<Request> getParameterClass() {
        return this.tool.getParameterClass();
    }

    @Override
    public Object execute(Request request, Context context) throws ToolException {
        Key<Request, Context> key = new Key<Request, Context>(this.tool, request, context);
        
        try {
            return cache.get(key, () -> execute(key));
        } catch(ExecutionException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof ToolException) {
                throw (ToolException)cause;         
            } 
            else { 
               throw new RuntimeException("Unexpected exception while executing tool: " + this.tool.getName(), exception);
            }
        }        
    }
  
    private static <Request, Context> Object execute(Key<Request, Context> key) throws ToolException {
        return key.getTool().execute(key.getRequest(), key.getContext());
    }
    
    private static class Key<Request, Context> {
        private Request request;
        private Context context;
        private Tool<Request, Context> tool;
        
        Key(Tool<Request, Context> tool, Request request, Context context) {
            this.tool = tool;
            this.request = request;
            this.context = context;
        }
        
        Tool<Request, Context> getTool() {
            return this.tool;
        }
        
        Request getRequest() {
            return this.request;
        }
        
        Context getContext() {
            return this.context;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key<?, ?> key = (Key<?, ?>) o;
            return Objects.equals(request, key.request) &&
                   Objects.equals(context, key.context);
        }

        @Override
        public int hashCode() {
            return Objects.hash(request, context);
        }   
    }
}
