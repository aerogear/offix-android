# Setup

## 1. Setup of Backened Server

- Clone the [ionic showcase](https://github.com/aerogear/ionic-showcase.git) repository which serves as a backened server for now.
- Run the following commands **to start the server:**
  - cd ./server
  - docker-compose up
  - npm install
  - npm run start
- This will start your server. 

## 2. Android Setup

- Clone [this](https://github.com/aerogear/offix-android.git) repository. 
- Open Android Studio, choose `Import project` navigate to the repository folder that was cloned and select open.
- For **Apollo Setup**, refer to the [apollo repository](https://github.com/apollographql/apollo-android).
- Ensure that the app's build.gradle has the apollo plugin and dependencies of the apollo libraries.


## Include the library 

Add the following dependency in your **module's build.gradle**:
  
  ```  implementation 'org.aerogear.offix:offix:0.2.0' ```

For **maven**, add the following dependency: <br/> 

```kotlin
<dependency>
	<groupId>org.aerogear.offix</groupId>
	<artifactId>offix</artifactId>
	<version>0.2.0</version>
	<type>pom</type>
</dependency>
```
