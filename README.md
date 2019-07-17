# Offix Android

Offix Android extends capabilities of Apollo GraphQL Android providing fully featured Offline Workflow and Conflict Resolution.

## Features 

- Offline support. Mutations are persisted when Offline.

## Example application

See `sample` for example application.<br/>
For now it's using ionic showcase as its backened (https://github.com/aerogear/ionic-showcase)

## Setup

### 1. Setup of Backened Server

- Clone the [ionic showcase](https://github.com/aerogear/ionic-showcase.git) repository.
- Run the following commands **to start the server:**
  - cd ./server
  - docker-compose up
  - npm install
  - npm run start
- This will start your server. 

### 2. Android Setup

- Clone [this](https://github.com/aerogear/offix-android.git) repository. 
- Open Android Studio, choose `Import project` navigate to the repository folder that was cloned and select open.
- For **Apollo Setup**, refer to the [apollo repository](https://github.com/apollographql/apollo-android) 

- Ensure that the app's build.gradle has the apollo plugin and dependencies of the apollo libraries.

#### Include the library **offix-offline**.

  Put the following dependency in your **app's build.gradle**.
  
  ``` implementation project(":offix-offline") ```

**Sample project's build.gradle**

```kotlin
    // Top-level build file where you can add configuration options common to all sub-projects/modules.
    buildscript {
        // ..other code..
        dependencies {
          classpath 'com.android.tools.build:gradle:3.4.1'
          classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
          classpath 'com.apollographql.apollo:apollo-gradle-plugin:1.0.0'
            // NOTE: Do not place your application dependencies here; they belong
            // in the individual module build.gradle files
        }
    }
``` 

- Generate your queries and mutations `.graphql files`, add the schema file for your app, build up the project to generate       the apollo generated code for the abbove files, and create an apollo-client in your application as mentioned in the apollo-   android documentation.

 **Consuming Code**

  **Code for performing Mutations** 
  
  - **In Kotlin**
  
  ```kotlin
    //Create an Object of mutation by passing in the build parameters according to the scehma.
    val mutation = UpdateCurrentTaskMutation.builder().id(id).title(title).version(version).build()
    
    //Create a client which is an object of ApolloCall on which call would be made.
     val client = apolloClient.mutate(mutation)?.refetchQueries(apolloQueryWatcher?.operation()?.name())
     
    //Create a callback object of type ApolloCall.Callback<UpdateCurrentTaskMutation.Data>
     val callback = object : ApolloCall.Callback<UpdateCurrentTaskMutation.Data>() {
            override fun onFailure(e: ApolloException) {
                e.printStackTrace()
            }

            override fun onResponse(response: Response<UpdateCurrentTaskMutation.Data>) {
              //Perform UI Bindings here.
            }
        }
        
     /*Call the enqueue function on the instance of APolloClient and pass in two parameters here:
       1. mutation object typecasted as mutation as 
          com.apollographql.apollo.api.Mutation<Operation.Data, Any, Operation.Variables>                 
       2. callback object typecasted as callback as ApolloCall.Callback<Any>
     */  
    apolloClient.enqueue(
            mutation as com.apollographql.apollo.api.Mutation<Operation.Data, Any, Operation.Variables>,
            callback as ApolloCall.Callback<Any>
        )
```

  - **In Java**
  
  ```java
    //Create an Object of mutation by passing in the build parameters according to the scehma.
    Mutation mutation = UpdateCurrentTaskMutation.builder().id(id).title(title).version(version).build();
    
    //Create a client which is an object of ApolloCall on which call would be made.
    ApolloMutationCall<UpdateCurrentTaskMutation.Data> client = apolloClient.mutate(mutation)
                .refetchQueries(apolloQueryWatcher.operation().name());  
                
    //Create a callback object of type ApolloCall.Callback<UpdateCurrentTaskMutation.Data>
     ApolloCall.Callback callback = new ApolloCall.Callback<UpdateCurrentTaskMutation.Data>(){
            @Override
            public void onResponse(@NotNull Response<UpdateCurrentTaskMutation.Data> response) {
            //Perform UI Bindings here.
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
            }
        };
        
     /* Call the enqueue function present in the file ExtensionKt which takes in 3 parameters:
        1. apollo client 
        2. mutation object              
        3. callback object
      */
        OffixKt.enqueue(apolloClient, mutation, callback);
  ```

## Run the sample app

- Make any mutation or query to the server.
- Refresh the app to see the updated results.

### Display of Offline Capabilities 

1. Make any mutation by going offline.
2. Then, after you come online your mutations (made when you are offline) will hit the server and you will get the response      back from the server.
3. To get the most recent data in your application, **refresh** your app once after the network comes back by swiping down on    the screen.

## Demo 

![OfflineFore1gif](https://user-images.githubusercontent.com/33238323/61216474-1177b180-a72b-11e9-883a-8592d09ee290.gif)


## Bugs

1. Offline mutations are lost when the app is closed.
2. No UI Bindings, you will have to update your UI manually.

## Features in Development 

1. Adding Background Sync capabilities to offix-offline so that mutations are pesisted after the app is closed.
2. Adding Conflict Resolution Strategy to the library.

