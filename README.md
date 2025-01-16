# KeyboardTrainer

## Quick Start
```
# Build the program
./build.sh

# Run the program
java -jar KeyboardTrainer.jar

# Clean up build files (optional)
./remove.sh
```

## Manual Build Steps

### Build program
```
javac KeyboardTrainer.java
```

### Create the jar file with all class files
```
jar -cvfm KeyboardTrainer.jar manifest.txt *.class
```

### Run program
```
java -jar KeyboardTrainer.jar
```