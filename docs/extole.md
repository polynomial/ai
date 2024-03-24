
# Extole

Extole is a SaaS marketing platform that leverages Sage
- in the administirative UI
- in the ticketing system to assist with classification and resolution of tickets

## Extole Ticket Scenarios

To ask a general question as a super user:
```
> curl -s  -H 'Content-Type: application/json' 'http://localhost:8080/conversations/messages' -d '{"scenario":"extole_support_help", "prompt": "What is the client short name for Nuts.com" }' | jq -r .response
The client short name for Nuts.com is "nuts".
```

Classify a support ticket, example to classify support ticket SUP-41162:
```
> curl -s  -H 'Content-Type: application/json' 'http://localhost:8080/conversations/messages' -d '{"scenario":"extole_ticket_runbook", "prompt": "SUP-41162" }' | jq .
```

Find and apply the best runbook to a support ticket, example for SUP-41162:
```
> curl -s  -H 'Content-Type: application/json' 'http://localhost:8080/conversations/messages' -d '{"scenario":"extole_support_ticket", "prompt": "SUP-41162" }' | jq .
```

## jira-app

Listens on a webhook for ticket creation and runs the 'extole_support_ticket' scenario.
