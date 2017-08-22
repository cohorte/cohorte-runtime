
Java Native Access (JNA)
========================


https://github.com/java-native-access/jna


The definitive JNA reference (including an overview and usage details) is in the JavaDoc. 
Please read the overview.
Questions, comments, or exploratory conversations should begin on the mailing list, although you may find it easier to find answers to already-solved problems on StackOverflow.

JNA provides Java programs easy access to native shared libraries without writing anything but Java code - no JNI or native code is required. 
This functionality is comparable to Windows' Platform/Invoke and Python's ctypes.

JNA allows you to call directly into native functions using natural Java method invocation. 
The Java call looks just like the call does in native code. Most calls require no special handling or configuration; no boilerplate or generated code is required.

JNA uses a small JNI library stub to dynamically invoke native code. 
The developer uses a Java interface to describe functions and structures in the target native library. This makes it quite easy to take advantage of native platform features without incurring the high overhead of configuring and building JNI code for multiple platforms. Read this more in-depth description.

While significant attention has been paid to performance, correctness and ease of use take priority.

In addition, JNA includes a platform library with many native functions already mapped as well as a set of utility interfaces that simplify native access.