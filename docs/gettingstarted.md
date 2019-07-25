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
  
  ```  implementation project(':offix') ```
