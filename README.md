# Offix Android

Offix Android extends capabilities of Apollo GraphQL Android providing
fully featured Offline Workflow and Conflict Resolution.

## Features 

 
- Offline support. Mutations are persisted when Offline.
- Offline Listeners and workflows for seamless UI.
- Flexible, out of the box Conflict Resolution implementations
- Subscriptions and Binary Upload that works offline.

## Example application

See `sample` for example application. For now it's using ionic showcase as its backened (https://github.com/aerogear/ionic-showcase)

## How to run the application

- Clone the ionic showcase repository (https://github.com/aerogear/ionic-showcase.git)
- Run the following commands **to start the server:**
  - cd ./server
  - docker-compose up
  - npm install
  - npm run start
- This will start your server.  
- Now clone this repository (https://github.com/aerogear/voyager-android.git)
- Run the application to send query and mutation to the server and displaying to the user.
