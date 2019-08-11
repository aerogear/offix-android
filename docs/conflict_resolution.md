## Conflict Resolution 

### Steps to support Conflict Resolution in your app:

- Create a class, let say `UserConflictResolutionHandler` which extends ConfliceResolutionInterface provided by the library.
- Override the `resolveConflict()` method of the interface in your class.
- You get the **`server state`** and the **`client state`** data associated with that mutation in which conflict occurred.
- You also get the **`operation type`** of the conflicted mutation. Run a switch case on the operation type to detect which type of mutation is it in which conflict occured and accordingly create an object of that mutation while resolving conflicts.
- You can resolve conflicts based on your business logic and again make a call to the server.

### Code:

```kotlin
/*
UserConflictResolutionHandler extends ConfliceResolutionInterface.
Here the user provides the custom implementation of resolving conflicts.
 */
class UserConflictResolutionHandler(val context: Context) : ConfliceResolutionInterface {

    /*
    Function which resolve the conflicts based on the user business logic.
     */
    override fun resolveConflict(
        serverState: Map<String, Any>,
        clientState: Map<String, Any>,
        operationType: String
    ) {
        /*
        Version based approach of Conflict Resolution.
        */
        val serverMap = serverState
        val containsVersion = serverMap.containsKey("version")

        if (containsVersion) {
            var versionAfterConflict = serverMap.get("version") as Int + 1

        /* You can run a switch case on the operation type to detect which type of mutation is it in which conflict occured
           and accordingly create an object of that mutation, resolve conflict and make a server call with it.
         */  
            when (operationType) {
                /*
                According to the schema structure, used a version based approach of resolving conflicts.
                If operationType is "UpdateCurrentTaskMutation" perform the following steps to resolve conflicts.
                 */
                "UpdateCurrentTaskMutation" -> {
                    var mut = UpdateCurrentTaskMutation.builder().id(clientState["id"].toString())
                        .title(clientState["title"].toString()).version(
                            versionAfterConflict
                        ).build()

                    val customCallback = object : ResponseCallback {

                        override fun onSuccess(response: Response<Any>) {
                            Log.e("onSuccess() updateTask", "${response.data()}")
                            val result = response.data()

                            //In case of conflicts data returned from the server id null.
                            result?.let {
                                Log.e(TAG, "onResponse-UpdateTask- $it")
                            }
                        }

                        override fun onSchedule(
                            e: ApolloException,
                            mutation: Mutation<Operation.Data, Any, Operation.Variables>
                        ) {
                            Log.e("onSchedule() updateTask", "${mutation.variables().valueMap()}")
                            e.printStackTrace()
                        }
                    }

                    Utils.getApolloClient(context)?.enqueue(
                        mut as com.apollographql.apollo.api.Mutation<Operation.Data, Any, Operation.Variables>,
                        customCallback
                    )
                }
            }
        }
    }
}
```
