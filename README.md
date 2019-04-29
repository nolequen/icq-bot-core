# ICQ Bot Core
[![Build Status](https://travis-ci.org/nolequen/icq-bot-core.svg?branch=master)](https://travis-ci.org/nolequen/icq-bot-core)
[![Maven Central](https://img.shields.io/maven-central/v/su.nlq/icq-bot-core.svg)](https://maven-badges.herokuapp.com/maven-central/su.nlq/icq-bot-core)
[![Codebeat](https://codebeat.co/badges/22ab4de3-4f09-44ab-9219-d9e044f58a21)](https://codebeat.co/a/nolequen/projects/github-com-nolequen-icq-bot-core-master)

Easy interface to ICQ Bot API powered by Kotlin.

## Getting started

* Create your own bot by sending the `/newbot` command to [MegaBot](https://icq.com/people/70001) and follow the instructions
* Note a bot can only reply after the user has added it contact list, or if the user was the first to start a conversation

## Usage

You can find latest release on Maven Central:

* Maven:
```xml
<dependency>
  <groupId>su.nlq</groupId>
  <artifactId>icq-bot-core</artifactId>
  <version>1.2.1</version>
</dependency>
```

* Gradle:
```kotlin
compile("su.nlq:icq-bot-core:1.2.1")
```

## How-To

The only thing you should know to create a new bot is token:
```kotlin
val bot = Bot("001.1234567890.1234567890:123456789")
```

### Contacts and messages

To be able to send a message you should create a conversation (no matter is it chat or dialogue):
```kotlin
val penpal = PenPal("42")
val conversation = bot.conversation(penpal)   
// not neccessary but looks nice to set typing status
conversation.typing()
conversation.message("Hi, how are you?")
```
It is possible to operate the contact list, for example:
```kotlin
bot.contacts().all().onSuccess { buddies ->
  buddies.find { it.name == "Adolf" }?.apply { remove() }
}
```

### Chats

To get any chat information you want is neccessary to create a chat instance:
```kotlin
val chat = bot.chat("123456789@chat.agent")
chat.description().onSuccess {
  println("Chat \"${it.name}\" was created ${it.created} by ${it.creator}")
}
```
There are various actions you can do with chat members or history, e.g.:
```kotlin
chat.invite(listOf(PenPal("42")))

chat.members().onSuccess { members -> members.forEach { it.block() } }

chat.history(0, 10).onSuccess { history ->
  history.messages.forEach { println("${it.id}: $it") }
  val message = history.messages[0]
  if (message is Chat.Text) {
    message.pin()
  }
}
```

### Files

It is possible to get the file info, upload or download the file:
```kotlin
val files = bot.files()

files.upload(File("myfile.txt")).map { URL(it) }.onSuccess { url ->
  files.download(url)
    .map { it.toInputStream() }
    .onSuccess { stream ->
      println(stream.use { String(it.readAllBytes()) })
    }
}
```