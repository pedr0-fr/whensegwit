# Goal

This repository has the code of a twitter bot that replies "When SegWit?" to all tweets of the specified accounts.
As here: https://twitter.com/alistairmilne/status/1293984610611802112

# Features
* **No double-tweets**: this bot saves the tweets it has replied to so it avoids double tweeting even if it's turned on and off several times. The tweets it has replied to are saved in `replies.json`.
* **Reply to old tweets**: when you launch the bot, you can choose to reply to all tweets of the accounts you specified up-to a certain day. This could be useful, for example, if you stop the bot for 3h and then set it to reply to all tweets in the past 3h.

# Configuration

In config.properties you can set your parameters.

```python
# a list of twitter handles to harass
accountsToReply = blockchain, alistairmilne
# at startup, it will reply to tweets no older than this age in minutes. If you set it to 0, it won't reply to any past tweet at startup
considerLastMinues = 400

# your credentials to use the twitter API. Get them at apps.twitter.com
OAuthConsumerKey=uuAhDMsQi6t4tjiqlaDlkyY6k
OAuthConsumerSecret=9aPg2D7vrbjSZFqFWADbA6MyuQrUVqhWEW0L7lxo87xTQxgbLn
OAuthAccessToken=1294016784589258752-gAIshshzy9EAo3taOwSmVjMzZJAy1G
OAuthAccessTokenSecret=QzasNUNo4ZRL7LwzaFHmMwbk8KzjnKEA8XRvXFpE4uUAn

# the name of the twitter handle you will tweet from, only for logging purposes
botHandle=WSegwit
```

# Pre-requisites/Running
Install maven and use it to download all the dependencies needed. On a terminal with current directory as this repository:

`mvn compile exec:java -Dexec.mainClass="com.whensegwit.Application"`

# Monitoring
The activity of the bot is written to the log file `logs/application.log`. This log rolls everyday to a new file with old file being named something like `logFile.2020-08--13.log`.

Here is an example of the log:

```

```
