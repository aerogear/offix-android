# Offix Android

Offix Android extends capabilities of Apollo GraphQL Android providing
fully featured Offline Workflow and Conflict Resolution.

## Features 

- Offline support. Mutations are persisted when Offline.
- Offline Listeners and workflows for seamless UI.
- Flexible, out of the box Conflict Resolution implementations
- Subscriptions and Binary Upload that works offline.

#### Implemented offline support.

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

## Running the sample app

1. Make a mutation by going offline.
2. Then, come online and click on "FromDb" button and you will see the results.
3. Also, refresh the application to see the updated cache.


### Demo 

Here the client performs a mutation of updating the Task3 when it is offline and when it comes online back again, changes are refelected back in the application on clicking on the "FromDB" button present on the top of the screen.


![aa1gif](https://user-images.githubusercontent.com/33238323/60811412-d0bced00-a1ac-11e9-907a-e5a903c001fc.gif)
