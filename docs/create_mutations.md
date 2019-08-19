## Execute mutations via the library
  
  ### In Kotlin
  
  ```kotlin
    //Create a mutation object
    val input = TaskInput.builder().title(title).version(version).description(description).status("test").build()
    val mutation = UpdateTaskMutation.builder().id(id).input(input).build()
    
    //Create an object of apolloCall
    val mutationCall = apolloClient.mutate(mutation)?.refetchQueries(apolloQueryWatcher?.operation()?.name())
     
    //Create a callback object of type ApolloCall.Callback
     val callback = object : ApolloCall.Callback<UpdateTaskMutation.Data>() {
     
           override fun onResponse(response: Response<UpdateTaskMutation.Data>) {              
                val result = response.data()?.updateTask()              
                
                //In case of conflicts data returned from the server is null.
                result?.let {
                    //Perform UI Bindings.                 
                }
            }
            
       /* Called when the request could not be executed due to cancellation, a connectivity problem or timeout.
       */      
          override fun onFailure(e: ApolloException) {              
                e.printStackTrace()
            }
     }
        
     /*Call the enqueue function on ApolloClient on the apollo mutation call and pass callback to it.
     */  
     mutationCall?.enqueue(callback)
```

  ### In Java
  
  ```java
   //Create a mutation object
   TaskInput input= TaskInput.builder().title(title).version(version).description(description).status("test").build();
   Mutation mutation = UpdateTaskMutation.builder().id(id).input(input).build();
    
   //Create an object of apolloCall
   ApolloMutationCall<UpdateTaskMutation.Data> call = apolloClient.mutate(mutation)
               .refetchQueries(apolloQueryWatcher.operation().name());  
                
   //Create a callback object of type ApolloCall.Callback
    ApolloCall.Callback callback= new ApolloCall.Callback() {
            @Override
            public void onResponse(@NotNull Response response) {
            //Perform UI bindings accordingly. 
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
            e.printStackTrace();
            }
        };
        
    /*Call the enqueue function on ApolloClient on the apollo mutation call and pass callback to it.
     */  
     mutationCall.enqueue(callback);
  ```
