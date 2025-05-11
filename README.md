# Java Bindings

The Java language bindings for Darknet/YOLO uses the FFM API (Java Foreign Function and Memory) to perform object detection from within Java applications.

The FFM API requires Java 21 or newer.

Example:

    Lorem ipsum dolor sit amet consectetur adipiscing elit.
    Sit amet consectetur adipiscing elit quisque faucibus ex.
    Adipiscing elit quisque faucibus ex sapien vitae pellentesque.

# Installing Darknet/YOLO

This project *requires* Darknet/YOLO.  Before proceeding, please make sure you have built _and_ installed Darknet/YOLO.

The recommended Darknet/YOLO repo to use is the modern Hank.ai one maintained by Stéphane Charette:  https://github.com/hank-ai/darknet#building

Once you've followed the instructions to build and install Darknet/YOLO, confirm that it works correctly by running `darknet --version` (Mac, Linux) or `"C:/Program Files/Darknet/bin/darknet.exe" --version` (Windows).

# Building

    cd ~/src/
    git clone https://github.com/stephanecharette/DarknetJava.git
    cd DarknetJava
    mkdir build
    javac -d build/ src/darknet/*.java
    cd build
    jar --create --verbose --file=Darknet.jar darknet/*.class

# Regenerate Java Source

Download JExtract from https://jdk.java.net/jextract/

    cd openjdk-22-jextract+6-47_linux-x64_bin/jextract-22/bin/
    ./jextract --define-macro DARKNET_INCLUDE_ORIGINAL_API --header-class-name darknet --target-package darknet -l :libdarknet.so --output src /usr/include/darknet*.h
