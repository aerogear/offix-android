# Conflict Resolution and Offline Support for the Aerogear Android SDK

The aim is to extend Data Sync (Data Synchronization using Voyager Framework) to android by porting of the current aerogear-js-sdk (https://github.com/aerogear/aerogear-js-sdk/tree/2.5.0) to Android to provide the services of Offline Support, Conflict Resolution, and the Data Sync services to the users which is one of the key features provided by the AeroGear mobile services.

## Features 

While the Apollo SDK provides us with CRUD functionalities and cache support for querying, adding cache support for mutation queries, offline support and cache resolution will be the main focus of this project.

- CRUD functionality : Using the Apollo GraphQL client to provide the users with the basic CRUD (Create, Read, Update and Delete) functionalities for making the required changes.
- Cache Support : Use of the existing Apollo Cache along with providing some additional functionalities to implement a cache for mutation queries too.
- Offline Support : Queries and Mutations will be persisted when Offline.
- Offline Listeners and workflows for seamless UI.
- Conflict Resolution Implementations.



