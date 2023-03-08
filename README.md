# qalipsis-plugin-slack

QALIPSIS supports slack to notify users of the status of their campaign after it runs. The process of enabling slack to
trigger notifications can be grouped in two stages:

- Configuring and activating the QALIPSIS publisher for Slack.
- Creating and installing an app into a Slack workspace.

## Configuring and activating the publisher

This step involves adding and enabling the necessary configurations, that allows QALIPSIS to trigger a notification
message at the end of a campaign. Below is a list of the configurations necessary to activate campaign report
publishing:

Configuration namespace: `report.export.slack`

##### Parameters:

- `enabled`: boolean flag that activates/deactivates campaign report publishing to Slack; defaults to `false`; *must*
be set to `true`.

- `status` (required): specifies which campaign report statuses should trigger a notification; allowable values is any set
of `ReportExecutionStatus`values: ALL, FAILED, SUCCESSFUL, WARNING, ABORTED. defaults to ALL which triggers notification
for any of the campaign report statuses above; A combination of ABORTED and FAILED only triggers notification when the
campaign report status is either ABORTED OR FAILED. Finally, selecting just one value e.g SUCCESSFUL triggers
notifications only when the campaign is SUCCESSFUL.

- `channel` (required): a valid Slack channel name within your workspace, where notifications messages should be pushed
to.

- `token` (required): the bot token of the app in charge to posting messages to your specified workspace channel, usually
beginning with `xoxb`. You can find details about creating an app and getting the bot token below.

Below is a valid configuration to activate and enable campaign report publishing to slack.

```yaml
report:
  export:
    slack:
      enabled: true
      token: xoxb-my-awesome-bot-token
      channel: my-qalipsis-campaign-report-channel
      status: #Using a list of SUCCESS, FAILED and WARNING triggers notification only when 
        #the status of a campaign report is success, a warning or a failure. It ignores every other statuses
        - SUCCESS
        - FAILED
        - WARNING
```

[This section of the QALIPSIS documentation](https://docs.qalipsis.io/#_external-property-sources), gives insight on
how to go about adding new configuration properties that sets or override the default.

## Creating and installing an app to a workspace

### Creating an app

To create a new Slack app:

1. Head over to [Slack apps documentation](https://api.slack.com/apps).
2. Click the button that says **Create New App**.
3. Select the **"From an app manifest"** option.
4. Select the development workspace where you will build your app and click on the **Next** button.
5. Replace the content of the json manifest with the json object below and click the **Next** button. You can edit the
   information in the `display_information` and `features` blocks to your preference, but avoid making changes in
   the `oauth-config` block.
6. Review the app summary.
7. Finally, hit the create app button.

```json
{
  "display_information": {
    "name": "QALIPSIS-REPORT-NOTIFIER",
    "description": "QALIPSIS notifications",
    "background_color": "#32373d",
    "long_description": "Receives the notifications from QALIPSIS via Slack, including execution reports. Install the App in Slack, configure the access in QALIPSIS and be notified in realtime when a campaign ends."
  },
  "features": {
    "bot_user": {
      "display_name": "QALIPSIS-REPORT-NOTIFIER",
      "always_online": false
    }
  },
  "oauth_config": {
    "scopes": {
      "bot": [
        "chat:write",
        "chat:write.public",
        "im:read",
        "channels:history",
        "groups:history",
        "mpim:history",
        "im:history",
        "incoming-webhook"
      ]
    }
  },
  "settings": {
    "org_deploy_enabled": false,
    "socket_mode_enabled": false,
    "token_rotation_enabled": false
  }
}
```

### Installing the app to a workspace (This will get your app into your workspace)

After creating the app, you have to install this app to your workspace below. The following steps can be followed to
complete the installation process.

1. select the **Basic information** option on the left-sided panel. This gives you a summary of basic information about
   the app, app credentials, display information, an option for app installation, etc.
2. Click on the **Install to Workspace** button on the sidebar.
3. Select a channel where the app you created above(in our case the app is **QALIPSIS-REPORT-NOTIFIER**), should post
   notifications. You can also create a dedicated channel in your workspace if you don't have one available.
4. Click on **allow** and this automatically does the installation of your created app to your workspace.

After installation, you'll find a bot user oAuth token inside your **app management page** located under the **OAuth &
Permissions** sidebar. This bot token is used in the configuration of the properties to activate the slack publisher.

After these steps, you are all set to receive message notifications in your desired channel. For further information
read the [official slack documentation](https://api.slack.com/authentication/basics#start).