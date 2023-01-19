# qalipsis-plugin-slack



###Creating an app
To create a new Slack app head over to https://api.slack.com/apps, fill out the
AppName and select the development workspace where you will build your app,
then hit the create app button.

###Installing the app to a workspace
Select the **install app** button on the sidebar, After clicking through one more green
**Install App To Workspace** button, you'll be sent through the **Slack OAuth UI**.
If you are adding your app to a different workspace besides the development workspace,
this flow would be completed by a user from that workspace, not you.
After installation, you'll find an access token inside your app management page located
under the OAuth & Permissions sidebar.

###Requesting Scope for your app
Scopes give your app permission to do things within a workspace e.g posting
or reading a message in a particular channel. 
To select the desired scope for our application, we follow the following steps:

- Go to the **OAuth & Permissions** sidebar
- Scroll down to the **Scopes** section and click to **Add an OAuth Scope**.
- Add the `chat:write` scope to our bot token which allows for post messages 
in approved channels and conversations

###Generating the Bot Token
To be able to send message as a bot associated with an app you have to generate 
a bot token usually starts with `xoxb` that is passed into the config 
files, and used to authenticate requests.

The following steps can be used to generate a bot token:
-Navigate to **OAuth & Permissions**
- Scroll till you find the bot user auth token
- Copy the values for this field and pass in as the token property in the config files


###Adding the App to a channel

To receive messages from a bot in a particular channel, you need to add 
the **App** that was created before to the channel where you want to receive
messages. This can be found under the Channel Details `>>` More `>>` Add Apps
`>>` then you select the app you want to add to the channel

After these steps, you are all set to receive message notifications in your 
desired channel.

For further information contact the [official slack documentation](https://api.slack.com/authentication/basics#start)