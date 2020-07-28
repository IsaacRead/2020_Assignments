


<h1>Test Coverage Reports</h1>

![JSON Layout Test Coverage](https://gitlab.ecs.vuw.ac.nz/course-work/swen301/2020/readisaa/readisaa-a1/-/raw/533730515f351947f20530c88631085875ea480a/a1-readisaa/JSONLayoutTestCoverage.png "JSON Layout Test Coverage")


![MemAppender Test Coverage](https://gitlab.ecs.vuw.ac.nz/course-work/swen301/2020/readisaa/readisaa-a1/-/raw/533730515f351947f20530c88631085875ea480a/a1-readisaa/MemAppenderTestCoverage.png "MemAppender Test Coverage")

The Test coverage reports indicate that the tests covered all the instructions of JSONLayout and almost all the instructions of MemAppender.
The instructions missed in Memappender were the Close Method and the requiresLayout Method (both overide methods from AppenderSkeleton). Close() contained no code as there was nothing to close. Therefore I did not find it neccassary to cover this in unit tests.
requiresLayout() method just returns false so I did not find it nessacary to write unit tests for this method.


<h1>Why I chose a specific Libary - org.json</h1>
I chose org.json as it is the original json package. It contained all necassary functions for the memAppender and JsonLayout, which was just parsing strings into json objects and json arrays and viceversa. 
There was plenty of documentation online for how to do these things. The performace of these functions well exceeded the performace required for this purpose, they caused no notable slowdown even when creating thousands of logs.  