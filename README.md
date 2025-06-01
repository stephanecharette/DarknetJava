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
	javac -d build/ src/Darknet/*.java
	java -cp build --enable-native-access=ALL-UNNAMED Darknet.Main

## JAR File

To create the @p .jar file:

	cd build
	jar --create --verbose --file=Darknet.jar Darknet/*.class
