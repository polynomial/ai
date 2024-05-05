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

# sage-app

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
curl -s  -H 'Content-Type: application/json' 'http://localhost:8080/conversations/messages' -d '{"scenario":"translate", "prompt": "Hello World", "parameters": { "language": "en", "target_language": "fr" }}' | jq .
```

# jira-app
Documentation [jira-app](https://github.com/mcyster/ai/blob/main/docs/jira-app.md)

The jira-app waits for ticket creation events from the Jira webhook and attempts to apply the appropriate Runbook.

To build locally
```
cd $AI_HOME
./gradlew :jira-app:bootRun
```

Test helping support on a ticket with
```
curl -s  -H 'Content-Type: application/json' 'http://localhost:8090/conversations/messages' -d '{"scenario":"extoleSupportTicket", "prompt": "SUP-NNNNN" }' | jq .
```

Test mapping a ticket to a runbook
```
curl -s  -H 'Content-Type: application/json' 'http://localhost:8090/conversations/messages' -d '{"scenario":"extoleTicketRunbook", "prompt": "SUP-NNNNN" }' | jq .
```

## deployment

Deploy the jira-app to production with (you will need ssh credentials on prod-ai):
```
jira-app-deploy
```

# Development Environment Setup

Clone this github repository:
```
git clone git@github.com:mcyster/ai.git ai
```

Setup the development environment, for example in your .bashrc, put something like:
```
ai_up() {
  (

    export AI_HOME=$HOME/ai
    export OPENAI_API_KEY="XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" 
    export BRANDFETCH_API_KEY="XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
    export JIRA_API_KEY="XXXXXXXXXXXXXXXXXXXXXXXXXXXX"
    export EXTOLE_SUPER_USER_API_KEY="XXXXXXXXXXXXXXXXXXXXXX"  # if using extole scenarios

    cd $AI_HOME
    nix-shell --command 'alias "cd-"="cd $AI_HOME"; PS1="\[\033[$PROMPT_COLOR\]\[\e]0;\u@\h>\w\a\]\u@\h:\w#>\[\033[0m\] "; return'
  )
}

alias "ai-up"=ai_up
```

After editing, source your .bashrc:
```
. .bashrc
```

To setup the development envionment
```
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
- https://github.com/mcyster/ai/tree/main/docs
  - https://github.com/mcyster/ai/blob/main/docs/jira-app.md

