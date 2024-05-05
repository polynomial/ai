


# Setup

## Setup outbound Webhook
Goto Jira, e.g. [Extole Jira](https://extole.atlassian.net/)
  - Click COG icon (top right)
  - System
  - Webhooks
  - Select / Create Webhook
    - Specify URL to which events should be posted
      - I'm using an ngrok url that relays requests to the jira-app server
    - Events
      - JQL: All Issues
      - Issue: Created
      - Comment: Created

## Public IP for Jira Webhook

An easy way to get a publicly accessible endpoint for development is ngrok
- https://ngrok.com/

To start the jira-app in development:
In your Jira account, you will need to setup a webhook
- https://extole.atlassian.net/plugins/servlet/webhooks
  - url: $NGROK_URL/tickets
  - issue requests for: create, comment create


## References
- https://developer.atlassian.com/server/jira/platform/webhooks/
- https://ngrok.com/

