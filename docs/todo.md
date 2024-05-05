

# Minor Changes

- Support mention of BeepBoop in jira to have it evaluate ticket, support prompt as commment
- Schedule Continuation of Conversation Tool (perhaps large, unless no context provided)
- Scenario parameters ignored 

# Major Chnages

## 3 Concepts necessary?

Do we need 3 concepts
- Scenario
  - Describes an api and context for having a Conversation
  - Use by the rest api to describe the questions it can answer and the context that needs to be provided
- Tool
  - Describe an api for asking a question (One response not a Conversation like a Scenaio)
- Agent
  - Perhaps should be named Assistant (as it sits directly on Assistant)

## Rest API

Need to update to support richer understanding of conversation for prompt development

## Conversation

- Support an ascynronous api
- Support rich conversation logs
- Seperate memory from logs


## Documentation

Mechanism to crawl / upload documentation

QDRant

### Public Code

Upload code directly from github


## systemd

Update scripts to work with systemd

Leverage secret store for keys
