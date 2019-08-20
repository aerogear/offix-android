## Test Offline Capabilities 

- Create any mutation. (Procedure shown on previous page)
- If your network is connected, then you will get a successful reponse from the server in ApolloCalback's `onResponse()` method. You can perfrom UI Bindings here according to your needs.
- If your network is not connected, your mutation will be successfully stored in an offline store.
- Once your network connection is regained, the mutations done when you were offline will try to replicate back to the server till all the offline mutations successfully hit the server and you get a response back from them.
- To get the most recent data in your application, **refresh** your app once after the network comes back by swiping down on the screen.
