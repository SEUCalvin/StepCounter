# StepCounter
A module for calculating the user's real-time walking steps on the Android platform.

# Quick Useage
To get a Git project into your build:

Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:
```
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```
Step 2. Add the dependency
```
dependencies {
  implementation 'com.github.SEUCalvin:StepCounter:1.0.0'
}
```
Step 3. See the example in MainActivity, Apply for permission, then user StepCounterService.init(), you can get step counter int the service.

# Feature
- This module supports all Android devices above version 4.4. If the mobile phone has a step sensor, it will be used first, otherwise the acceleration sensor will be used to estimate the number of steps.
- This module, occupy a separate process, will use the foreground notification method to obtain the highest process priority, and the notification will notify the user in real time how many steps have been taken.
- This module will output the number of steps today which you can use ***getCurrentStepCounter()*** to get, increasing from 0, and reset to 0 the next day. The shutdown operation does not affect the step count of the day.
- This module will use the database to store the daily steps, you can use ***getAllStepData()*** to get the list of all steps in the database.
