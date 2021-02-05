# What are FlatBuffers and why are we using them?

See the architecture document for reasons why the code is architected to pass messages between modules.
We'd like to be able to record all messages being sent to assist us in debugging the code.  So we'd like
our messages to be as small as possible, and we'd also like the writing and reading of these messages 
to be as fast as possible.  [FlatBuffers](https://google.github.io/flatbuffers/) is a library that does this.
It was started by Google for game developers on mobile platforms who require this kind of high performance.

It is slightly difficult to use, but there are plenty of examples to follow
The [Tutorial](https://google.github.io/flatbuffers/flatbuffers_guide_tutorial.html) and
[JavaTest code](https://github.com/google/flatbuffers/blob/master/tests/JavaTest.java) are good places to start
if you can't find a similar message in our own code.


# Installing the FlatBuffers Compiler

To use FlatBuffers, you'll first need the executable so you can compile the schema files into Java.

Go here and download the latest release:
https://github.com/google/flatbuffers/releases

Unzip and install `flatc.exe` in the `$PROJECT_ROOT$/tools` folder


# Update `build.gradle`

## Adding FlatBuffers dependency to `build.gradle`

Add the following dependency to build.gradle to load Google's Flatbuffer code

```
dependencies {
    implementation 'com.google.flatbuffers:flatbuffers-java:1.12.0'
}
```


## Add task to compile the flatbuffer schemas to `build.gradle`

Add the following task to build.gradle

```
task compileFlatbuffers {
    String fbs_folder = "src/main/java/frc/taurus/messages/schema" 
    
    FileTree files = fileTree(fbs_folder).matching{ include "**/*.fbs"}
    
    files.each{ File file ->
            println "Compiling: $file.name"
            exec {
                executable = "tools/flatc.exe"
                args = ["--java", "--gen-mutable", "-o", "src/main/java", file.absolutePath]
            }
    }
}
```