#!/bin/bash

# Compile the Java file
echo "Compiling KeyboardTrainer.java..."
javac KeyboardTrainer.java

# Create jar file
echo "Creating JAR file..."
jar -cvfm KeyboardTrainer.jar manifest.txt *.class

echo "Build complete! You can now run the program with: java -jar KeyboardTrainer.jar" 