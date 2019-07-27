<p align="center">
  <img width="400" src="https://github.com/aerogear/offix/raw/master/resources/logo.png">
  <br/>
  Offix extends capabilities of Apollo GraphQL providing</br>
  fully featured Offline Workflow and Conflict Resolution for Android Platform
</p>


## Introduction

Offix enables working with localy cached GraphQL compilant data without access to server.
It will leverage capabilities of Apollo GraphQL cache do deliver seamless experience for querying data even when server side data is not reachable.

For more information about the Apollo and GraphQL please go to https://www.apollographql.com/
## Features 

#### Offline support
Mutations are persisted when Offline
and replicated back to server when online.

## Documentation

See [offix](https://android.offix.dev) for usage and more details.

## Releases

- **Gradle**: </br>

  `implementation 'org.aerogear.offix:offix:1.0.0-alpha01'`
  
- **Maven**: 
  ```xml
  <dependency>
	<groupId>org.aerogear.offix</groupId>
	<artifactId>offix</artifactId>
	<version>1.0.0-alpha01</version>
	<type>pom</type>
</dependency>



## Example application

See `sample` for example application.<br/>

## Limitations

1. No UI Bindings, you will have to update your UI manually.
2. The library only supports apollo-android for the time being.

## Features in Development 

1. Adding Conflict Resolution mechanism to the library.

## Contributing 

See [contribution guidelines](./CONTRIBUTING.md) file for more information

