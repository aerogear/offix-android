## Conflict Resolution 

### Steps to support Conflict Resolution in your app:

- Create a class, let say `UserConflictResolutionHandler` which extends ConfliceResolutionInterface provided by the library.
- Override the `resolveConflict()` method of the interface in your class.
- You get the **`server state`** and the **`client state`** data associated with that mutation in which conflict occurred along with the **`operation type`** of that mutation.
- You can resolve conflicts based on your business logic and again make a call to the server.
