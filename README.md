# Offix Android

Offix Android extends capabilities of Apollo GraphQL Android providing
fully featured Offline Workflow and Conflict Resolution.

## Features 

- Offline support. Mutations are persisted when Offline.
- Offline Listeners and workflows for seamless UI.
- Flexible, out of the box Conflict Resolution implementations
- Subscriptions and Binary Upload that works offline.

## Offline Support is implemented where the mutations are persisted when the client is offline.

#### Steps Followed:
A database is maintained which stores the class name of the mutation and the value map of variables given by user along with some other details like operationID, etc.

- When the client comes online, clicking on the "FROMDB" button will take mutation from DB by iterating over a list of mutations stored in the offline queue.
- We extract the class name of the mutation and the value map variables.
- Then we take the constructor of the class and the parameters that the constructor takes in.
- Then we have stored the values of json in an array list of objects and pass this arraylist as an argument in the constructor.
- Then we make an object of mutation and make a call by passing the above object in mutation call.

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

### To check offline part:

1. Make a mutation by going offline.
2. Then, come online and click on "FromDb" button and you will see the results.
3. Also, refresh the application to see the updated cache.
