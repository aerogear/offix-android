<p align="center">
  <img width="400" src="https://github.com/aerogear/offix/raw/master/resources/logo.png">
  <br/>
  Offix extends capabilities of Apollo GraphQL providing</br>
  fully featured Offline Workflow and Conflict Resolution for Android Platform
</p>

> NOTE: Project is no longer maintained and updated to latest version of the Apollo Android SDK 

## Introduction

Offix extends capabilities of Apollo GraphQL providing fully featured Offline Workflow and Conflict Resolution for Android Platform. User can perform queries and mutations when offline and these would be automatically synced to the server when they come online. It also enables working with locally cached GraphQL compliant data without access to server. It will leverage capabilities of Apollo GraphQL cache do deliver seamless experience for querying data even when server side data is not reachable.

For more information about the Apollo and GraphQL please go to https://www.apollographql.com/
## Features 

#### Offline support
Mutations are persisted when Offline
and replicated back to server when online.

#### Conflict Resolution
Provide custom conflict resolution strategies to solve conflicts among the mutations and replicate them back to the server after resolving conflicts.

## Documentation

See [offix website](https://android.offix.dev) for usage and more details.

## Releases

- **Gradle**:<br/>

  `implementation 'org.aerogear.offix:offix:0.4.0'`
  
- **Maven**:<br/> 

```xml
  <dependency>
	<groupId>org.aerogear.offix</groupId>
	<artifactId>offix</artifactId>
	<version>0.4.0</version>
	<type>pom</type>
  </dependency>
```





## Example application

See [sample](https://github.com/aerogear/offix-android/tree/master/sample) for example application.<br/>

## Presentation Link

https://docs.google.com/presentation/d/1SzAfRQTqfMtXbp6jFfr99jkIjjZWslX3tU2Wkoec2Uk/edit?usp=sharing

## Contributing 

See [contribution guidelines](./CONTRIBUTING.md) file for more information

