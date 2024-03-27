
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


## Extole Runbooks

The 'extole_support_ticket' scenario, determines that best Runbook that matches a ticket then executes that Runbook.

There are a number of predefined runbooks
- extole_runbook_other - the runbook of last resort
- extole_runbook_notification_traffic_decrease - this runbook handles decrease in traffic notitication tickets
- extole_runbook_notification_traffic_inrease  - this runbook handles increase in traffic notitication tickets
- ... 

It is also possible to configure Runbooks using YAML, by putting .yml files in the resource directory [extole/runbook](https://github.com/mcyster/ai/tree/main/sage-extole/src/main/resources/extole/runbooks), with the following attributes:
- name - name of the runbook (extole_runbook is automatically prefixed)
- descripiton - description of where the runbook should be applied
- keywords - words that would appear in a ticket that would match this runbook
- instructions - instructions to the advisor on what to do.

Restart sage-app and check your runbook is loaded with:
```
> curl -s  -H 'Content-Type: application/json' 'http://localhost:8080/scenarios' | jq -r '.[].name'   | grep extole_runbook
extole_runbook_notification_webhook
extole_runbook_notification_email_render
extole_runbook_notification_prehandler
extole_runbook_other
extole_runbook_notification_traffic_increase
extole_runbook_notification_other
extole_runbook_notification_traffic_decrease
```

Search for then runbook:
```
> curl -s  -H 'Content-Type: application/json' 'http://localhost:8080/conversations/messages' -d '{"scenario":"extole_runbook", "prompt": "find the best runbook for: test, ai" }' | jq -r '.response'
{ "runbook": "extole_runbook_ai_test" }
```

Test the runbook:
```
> curl -s  -H 'Content-Type: application/json' 'http://localhost:8080/conversations/messages' -d '{"scenario":"extole_runbook_ai_test", "prompt": "Rabbit" }' | jq -r '.response'

Why did the rabbit go to the barber?


Because he had too many split hares! 🐰✂️"
```

## Extole Report Tools

It is also possible to configure Report Tools using YAML, by putting .yml files in the resource directory [extole/runbook](https://github.com/mcyster/ai/tree/main/sage-extole/src/main/resources/extole/reports), with the following attributes:
- name - name of tool and report, needs to be snake case 
- description - description of tool for ai to know to use
- reportName - name of report type
- rowLimit (optional, defaults to 10)
- parameters (a map of attributes)

If you have a report that lists consumer input event names, you could test it with:
```
> curl -s  -H 'Content-Type: application/json' 'http://localhost:8080/conversations/messages' -d '{"scenario":"extole_support_help", "prompt": "For the client nuts.com get a list of all the consumer input event names" }' | jq .
```

## jira-app

Listens on a webhook for ticket creation and runs the 'extole_support_ticket' scenario.
