# Unit Testing

JUint allows programmers to run tests on your code to verify it's correct behavoir

Mockito allows users to 'mock' out hardware components so that robot code can be tested without a Robot!

See FRC 997 Spartan Robotics excellent video on Testing Without a Robot.  
https://www.youtube.com/watch?v=vmRFiF9hd2E

See FRC 971 Spartan Robotics excellend video on Test Driven Development.
https://www.youtube.com/watch?v=uGtT8ojgSzg

# Setting up VSCode for JUnit and Mockito

JUnit is already supported by WPILib's default build.gradle
Mockito is not supported by WPILib's base project, so we must do the following.

See https://site.mockito.org/#how for the latest directions.


Add jcenter to the list of repositories
```
repositories { 
    jcenter()
}
```

Add the following line to the dependencies section (update the version number as needed)
```
dependencies {
    testImplementation 'org.mockito:mockito-core:2.+'
}
```

Add the following import to all Test files
```
import static org.mockito.Mockito.*;
```

Use `WPILib: Test Robot Code` command to run the unit tests.  (click WPILib icon on toolbar, or type Ctrl-Shift-P)



# Other WPILib woes

If you run into other WPILib problems, including HAL errors, try adding the following
https://www.chiefdelphi.com/t/unit-testing-java-io-ioexception-wpihaljni/372288/19

Perform one build, then retry the unit test.