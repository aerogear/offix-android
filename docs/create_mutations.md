## Execute mutations via the library
  
  ### In Kotlin
  
  ```kotlin
    //Create a mutation object
    val mutation = UpdateCurrentTaskMutation.builder().id(id).title(title).version(version).build()
    
    //Create an object of apolloCall
    val client = apolloClient.mutate(mutation)?.refetchQueries(apolloQueryWatcher?.operation()?.name())
     
    //Create a callback object of type ResponseCallback
    val customCallback = object : ResponseCallback {
          override fun onSuccess(response: Response<Any>) {
             //Perform UI bindings accordingly.
          }
            
       /* Called when the request could not be executed due to cancellation, a connectivity problem or timeout.
       */      
          override fun onSchedule(e: ApolloException, mutation: Mutation<Operation.Data, Any, Operation.Variables>) {
             e.printStackTrace()
             //Perferm local UI Bindings.
            }
     }
        
     /*Call the enqueue function on ApolloClient and pass in two parameters :
       1. mutation object typecasted as mutation as 
          com.apollographql.apollo.api.Mutation<Operation.Data, Any, Operation.Variables>                 
       2. customCallback
     */  
     
    apolloClient.enqueue(
            mutation as com.apollographql.apollo.api.Mutation<Operation.Data, Any, Operation.Variables>,
            customCallback
    )
```

  ### In Java
  
  ```java
   //Create a mutation object
   Mutation mutation = UpdateCurrentTaskMutation.builder().id(id).title(title).version(version).build();
    
   //Create an object of apolloCall
   ApolloMutationCall<UpdateCurrentTaskMutation.Data> client = apolloClient.mutate(mutation)
               .refetchQueries(apolloQueryWatcher.operation().name());  
                
   //Create a callback object of type ResponseCallback
   ResponseCallback customCallback = new ResponseCallback(){
          @Override
          public void onSuccess(@NotNull Response<Object> response) {
          //Perform UI bindings accordingly.
       }
       
       /* Called when the request could not be executed due to cancellation, a connectivity problem or timeout.
       */ 
          @Override
          public void onSchedule(@NotNull ApolloException e, @NotNull Mutation<Operation.Data, Object, Operation.Variables> mutation) {
          //Perform UI bindings accordingly.
        }
   };
        
    /* Call the enqueue function present in the file Offix which takes in 3 parameters:
        1. apollo client 
        2. mutation object              
        3. ResponseCallback object
    */
    Offix.enqueue(apolloClient, mutation, customCallback);
  ```
