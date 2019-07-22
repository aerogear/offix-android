<p align="center">
  <img width="400" src="https://github.com/aerogear/offix/raw/master/resources/logo.png">
  <br/>
  Offix extends capabilities of Apollo GraphQL providing</br>
  fully featured Offline Workflow and Conflict Resolution for Android Platform
</p>

## Features 

### Offline support
Mutations are persisted when Offline
and replicated back to server when online.



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
- For **Apollo Setup**, refer to the [apollo repository](https://github.com/apollographql/apollo-android).
- Ensure that the app's build.gradle has the apollo plugin and dependencies of the apollo libraries.

### Include the library 
Add the following dependency in your **module's build.gradle**.
  
  ``` implementation project(":offix-offline") ```

**Execute mutations via the library**
  
  - **In Kotlin**
  
  ```kotlin
    //Create a mutation object
    val mutation = UpdateCurrentTaskMutation.builder().id(id).title(title).version(version).build()
    
    //Create apolloClient
     val client = apolloClient.mutate(mutation)?.refetchQueries(apolloQueryWatcher?.operation()?.name())
     
    //Create a callback object
     val callback = object : ApolloCall.Callback<UpdateCurrentTaskMutation.Data>() {
            override fun onFailure(e: ApolloException) {
                e.printStackTrace()
            }

            override fun onResponse(response: Response<UpdateCurrentTaskMutation.Data>) {
              //Perform UI Bindings here.
            }
        }
        
     /*Call the enqueue function on ApolloClient and pass in two parameters :
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
    //Create a mutation object
    Mutation mutation = UpdateCurrentTaskMutation.builder().id(id).title(title).version(version).build();
    
    //Create apolloClient
    ApolloMutationCall<UpdateCurrentTaskMutation.Data> client = apolloClient.mutate(mutation)
                .refetchQueries(apolloQueryWatcher.operation().name());  
                
    //Create a callback object
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
        Offix.enqueue(apolloClient, mutation, callback);
  ```
  
  
 **Extend Background Offline Support of the library**
 
 - Create a class `Worker` that extends `OffixWorker` of the library.
 - Follow the below mentioned steps in the Worker's `doWork()` method.
 
   - Get the list of mutations by calling `getListOfMutations()` function of the parent class.
   - Run a loop on list of mutations.
   - On each mutation fetched from the list, create an object of Mutation<D,T,V> by passing the fetched mutation to the 
     `getMutation(mutation)` function of library.
   - You can use the above object while creating an instance of ApolloCall and make a call to the server.
   - Make sure to delete the fetched mutation from the list in the onResponse() of your callback by calling              
   `deleteMutation(mutation)`.
   
 - **In Kotlin**
  
  ```kotlin
  
   class SampleWorker(context: Context, workParameters: WorkerParameters) : OffixWorker(context, workParameters) {
   
    override fun doWork(): Result {
        /*
         getListOfMutations() returns the list of mutations stored in database in the library.
         It's present in the parent class, i.e OffixWorker.
         */
        val listOfMutations = getListOfMutations()
        
            //get the mutation one by one from list
            listOfMutations.forEach { storedmutation ->
            
                /*
                 Create an object of Mutation<D,T,V> from the stored mutation in the database.
                 */
                val obj = getMutation(storedmutation)

                /* Make an apollo client which takes in mutation object and makes a call to server.
                */
                val customClient = apolloClient.mutate(obj)

                customClient?.enqueue(object : ApolloCall.Callback<Operation.Data>() {
                    override fun onFailure(e: ApolloException) {
                        e.printStackTrace()
                    }

                    /*
                     On getting a successful response back from the server, delete mutation from the database.
                     */
                    override fun onResponse(response: Response<Operation.Data>) {
                       // Do your work and delete that mutation by calling the deleteMutation() function.
                        deleteMutation(storedmutation)
                    }
                })
        }
        return Result.success()
    }

```


- **In Java**

  ```java
  
  class SampleWorker extends OffixWorker{
  
  public doWork(){
  
     /* getListOfMutations() returns the list of mutations stored in database in the library.
         It's present in the parent class, i.e OffixWorker.
     */
     List<Mutation> listOfMutations=getListOfMutations();
   
      //get the mutation one by one from list.
      for(int i=0;i<listOfMutations.size();i++){
      
      Mutation storedmutation = listOfMutations.get(i);
      
      com.apollographql.apollo.api.Mutation<Operation.Data,Operation.Data,Operation.Variables>         
      obj=Offix.getMutation(storedmutation);

      ApolloMutationCall<Operation.Data> customClient= apolloClient.mutate(obj);
      
      customClient?.enqueue(object : ApolloCall.Callback<Operation.Data>() {
                 override fun onFailure(e: ApolloException) {
                        e.printStackTrace()
                    }

               /* On getting a successful response back from the server, delete mutation from the database.
               */
                 override fun onResponse(response: Response<Operation.Data>) {
                       // Do your work and delete that mutation by calling the deleteMutation() function.
                        deleteMutation(storedmutation)
                    }
                })
         return Result.success();
     }
   ```  
   


In the onStop() mehtod of your activity, call **`scheduleWorker(YourWorker::class.java)`** function of the library.


## Run the sample app

- Make any mutation or query to the server.
- Refresh the app to see the updated results.

### Test Offline Capabilities 

1. Make any mutation by going offline.
2. Then, after you come online your mutations (made when you are offline) will hit the server and you will get the response      back from the server.
3. To get the most recent data in your application, **refresh** your app once after the network comes back by swiping down on    the screen.

## Demo 

![OfflineFore1gif](https://user-images.githubusercontent.com/33238323/61216474-1177b180-a72b-11e9-883a-8592d09ee290.gif)


## Limitations

1. No UI Bindings, you will have to update your UI manually.
2. The library only supports apollo-android for the time being.

## Features in Development 

1. Adding Conflict Resolution mechanism to the library.
