# ai
Restful ai app


# Development

Build everything:
```
cd $AI_HOME
./gradlew build
```

To start, for example, the sage-app:
```
./gradlew :sage-app:bootRun
```

Debug the sage-app:
```
./gradlew :sage-app:bootRun --debug-jvm
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

## sage-app

To start the sage-app in development:
```
cd $AI_HOME
./gradlew :sage-app:bootRun
```

Check the sage-app is up with:
```
curl -s 'http://localhost:8080/' 
```

List scenarios
```
curl -s 'http://localhost:8080/scenarios'  | jq -r '.[].name'
```

Synchronously run a scenario:
```
curl -s  -H 'Content-Type: application/json' 'http://localhost:8080/conversations/messages' -d '{"scenario":"translate", "prompt": "Hello World", "context": { "language": "en", "target_language": "fr" }}' | jq .
```

## jira-app

In your Jira account, you will need to setup a webhook
- https://extole.atlassian.net/plugins/servlet/webhooks
  - issue requests for: create, comment create

An easy way to get a publicly accessible endpoint for development is ngrok
- https://ngrok.com/

To start the jira-app in development:
```
cd $AI_HOME
./gradlew :sage-app:bootRun
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

