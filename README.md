# ICQ Bot Core
[![Build Status](https://travis-ci.org/nolequen/icq-bot-core.svg?branch=master)](https://travis-ci.org/nolequen/icq-bot-core)
[![Maven Central](https://img.shields.io/maven-central/v/su.nlq/icq-bot-core.svg)](https://maven-badges.herokuapp.com/maven-central/su.nlq/icq-bot-core)
[![codebeat badge](https://codebeat.co/badges/ec147cb1-9ed7-4e48-8a46-b726b40925ab)](https://codebeat.co/a/nolequen/projects/github-com-nolequen-icq-bot-core-master)

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
```groovy
implementation 'su.nlq:icq-bot-core:1.2.1'
```
or  
```kotlin
compile("su.nlq:icq-bot-core:1.2.1")
```
