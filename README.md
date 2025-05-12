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

- If using Windows, you *must* edit your `PATH` to include `C:/Program Files/Darknet/bin` (or whever you installed it).  You must restart your command prompt once you edit the `PATH`.
- If using Linux, then both `darknet` and `libdarknet.so` are installed in a common location, so there should be no need to edit anything further.

Once you've followed the instructions to build _and_ install Darknet/YOLO, confirm that it works correctly by running `darknet --version`.

# Building

    cd ~/src/
    git clone https://github.com/stephanecharette/DarknetJava.git
    cd DarknetJava
    mkdir build
    javac -d build/ src/darknet/*.java
    cd build
    jar --create --verbose --file=darknet.jar darknet/*.class
    java --enable-native-access=ALL-UNNAMED Darknet.Main

# Regenerate Java Source

Download JExtract from https://jdk.java.net/jextract/

    cd openjdk-22-jextract+6-47_linux-x64_bin/jextract-22/bin/
    ./jextract --define-macro DARKNET_INCLUDE_ORIGINAL_API --define-macro DARKNET_SKIP_VERSION_MACROS --header-class-name darknet --target-package darknet -l :libdarknet.so --output src /usr/include/darknet*.h

If re-generating the .java files, see the modification that was done to SYMBOL_LOOKUP in darknet.java lines 58-59.
