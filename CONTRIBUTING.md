# Getting Started

# Setup

## 1. Setup of Backened Server

- Clone this [offix-android](https://github.com/aerogear/offix-android.git) repository.
- The [server]((https://github.com/aerogear/offix-android/tree/master/sample/server)) in sample app server as a backened server for now.
- Run the following commands **to start the server:**
  - cd ./sample
  - cd ./server
  - graphback generate
  - npm run build
  - npm run start
- This will start your server. 


## 2. Android Setup

- Clone [this](https://github.com/aerogear/offix-android.git) repository. 
- Open Android Studio, choose `Import project` navigate to the repository folder that was cloned and select open.
- For **Apollo Setup**, refer to the [apollo repository](https://github.com/apollographql/apollo-android).
- Ensure that the app's build.gradle has the apollo plugin and dependencies of the apollo libraries.


## Include the library 

Add the following dependency in your **module's build.gradle**:
  
  ```  implementation 'org.aerogear.offix:offix:0.4.0' ```

For **maven**, add the following dependency: <br/> 

```kotlin
<dependency>
	<groupId>org.aerogear.offix</groupId>
	<artifactId>offix</artifactId>
	<version>0.4.0</version>
	<type>pom</type>
</dependency>
```


#### Note: The library works with the offix-sever that is node.js based and [conflict protocol](https://offix.dev/#/ref-conflict-server?id=structure-of-the-conflict-error) is descibed on [offix.dev](https://offix.dev/). Also ensure that the latest version of [Android Studio](https://developer.android.com/studio?gclid=CjwKCAjwnf7qBRAtEiwAseBO_LMFi8vRaMcSWJYnAZuIqr5--WTp0RAu0IWSPwcGjyHNZgblqmmw5RoCTs4QAvD_BwE) is installed on your system.
