# Java Bindings

The Java language bindings for Darknet/YOLO uses the **FFM API** (Java Foreign Function and Memory) to perform object detection from within Java applications.

> [!IMPORTANT]
> The FFM API requires Java 21 or newer!

For example, on Ubuntu 24.04.3:

```sh
# Visit this page to find a recent version of the JDK:  https://www.oracle.com/ca-en/java/technologies/downloads/
wget https://download.oracle.com/java/24/latest/jdk-24_linux-x64_bin.deb
sudo dpkg -i jdk-24_linux-x64_bin.deb
javac --version
javac 24.0.2
```

# Installing Darknet/YOLO

This project **requires** that Darknet/YOLO be installed.  Before proceeding, please make sure you have built _and_ **installed** Darknet/YOLO.

The recommended Darknet/YOLO repo to use is the modern one maintained by Stéphane Charette:  https://codeberg.org/CCodeRun/darknet#table-of-contents

- If using Windows, you *must* edit your `PATH` to include `C:/Program Files/Darknet/bin` (or wherever you installed it).  You must restart your command prompt once you edit the `PATH`.
- If using Linux, then both `darknet` and `libdarknet.so` are installed in a common location, so there should be no need to edit anything further.

Once you've followed the instructions to build _and_ install Darknet/YOLO, confirm that it works correctly by running `darknet --version`.  The output should look somewhat similar to this:

```sh
darknet --version
Darknet V5 "Moonlit" v5.0-138-ga061d2f0 [v5]
CUDA runtime version 12000 (v12.0), driver version 12060 (v12.6)
cuDNN version 12020 (v8.9.7), use of half-size floats is ENABLED
=> 0: NVIDIA GeForce RTX 3090 [#8.6], 23.6 GiB
Protobuf 3.21.12, OpenCV 4.6.0, Ubuntu 24.04
```

# Building

The Java bindings for Darknet/YOLO must be compiled.  Run the following commands:

```sh
cd ~/src/
git clone https://codeberg.org/CCodeRun/DarknetJava.git
cd DarknetJava
mkdir build
javac -d build/ src/Darknet/*.java
java -cp build --enable-native-access=ALL-UNNAMED Darknet.Main
```

## JAR File

To create the `.jar` file:

```sh
cd build
jar --create --verbose --file=Darknet.jar Darknet/*.class
```
