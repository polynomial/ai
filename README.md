# ai
Restful ai app


# Development

```
cd $AI_HOME
./gradlew
```

For debug on port 5005
```
./gradlew bootRun :sage-app:bootRun --debug-jvm
```

Check your environment with
```
curl -s 'http://localhost:8080/ai-check' | jq .
```

# Development Environment Setup

```
git clone git@github.com:mcyster/ai.git ai

# in your .bashrc, put something like
ai_up() {
  (
    export OPENAI_API_KEY=XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    export AI_HOME=$HOME/ai

    cd $AI_HOME
    nix-shell --command 'alias "cd-"="cd $AI_HOME"; PS1="\[\033[$PROMPT_COLOR\]\[\e]0;\u@\h>\w\a\]\u@\h:\w#>\[\033[0m\] "; return'
  )
}
alias "ai-up"=ai_up

# source your .bashrc
. .bashrc
ai-up
```


## References
- https://platform.openai.com/assistants
- https://platform.openai.com/docs/api-reference/chat
- https://spring.io/projects/spring-boot
  - https://spring.io/guides/gs/spring-boot/
  - https://github.com/spring-projects/spring-boot/tree/main
- https://docs.spring.io/spring-ai/reference
  - https://repo.spring.io/ui/native/snapshot/org/springframework/experimental/ai/spring-ai-openai-spring-boot-starter/
- https://platform.openai.com/api-keys

