## Extend Background Offline Support of the library
 
 - Create a class `Worker` that extends `OffixWorker` of the library.
 - Follow the below mentioned steps in the Worker's `doWork()` method.
 
   - Get the list of mutations by calling `getListOfMutations()` function of the parent class.
   - Run a loop on list of mutations.
   - On each mutation fetched from the list, create an object of Mutation<D,T,V> by passing the fetched mutation to the 
     `getMutation(mutation)` function of library.
   - You can use the above object while creating an instance of ApolloCall and make a call to the server.
   - Make sure to delete the fetched mutation from the list in the onResponse() of your callback by calling              
   `deleteMutation(mutation)`.
   
 ### In Kotlin
  
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


### In Java

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
