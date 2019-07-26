<p align="center">
  <img width="400" src="https://github.com/aerogear/offix/raw/master/resources/logo.png">
  <br/>
  Offix extends the capabilities of Apollo GraphQL providing a
  fully featured Offline Workflow and Conflict Resolution for Android Platform.
</p>

## Introduction

Offix enables working with localy cached GraphQL compilant data without access to server.
It will leverage Apollo GraphQL cache do deliver seamless experience for querying data even when server side data is not reachable.

For more information about the Apollo and GraphQL please go to https://www.apollographql.com/

## Features 

### Offline support
- Mutations are persisted when Offline and replicated back to server when online.
- Offline Listeners for performing UI Bindings according to the requirements.

## Example application

See [sample](https://github.com/aerogear/offix-android) for example application.<br/>

## Limitations

1. No UI Bindings, you will have to update your UI manually.
2. The library only supports apollo-android for the time being.

## Features in Development 

1. Adding Conflict Resolution mechanism to the library.
2. Leveraging Offline Support to nested schema structure.

## Contributing 

See [contribution guidelines](./CONTRIBUTING.md) file for more information
