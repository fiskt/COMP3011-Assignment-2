# COMP3011-Assignment-2

[github.com/fiskt/COMP3011-Assignment-2.git](https://github.com/fiskt/COMP3011-Assignment-2.git)

## Project Overview

This project converted the original single threaded video player into multi threaded.

## Features

* Improved performance over single threaded design
* Frame processor parsing for command line input
* Help command to display available options

## Building

```Shell
# Checks that the project is valid
mvn validate

# Compiles src/main/java into target/classes
mvn compile

# Builds a runnable artifact
mvn package
```

## Running

```Shell
mvn javafx:run "-Djavafx.args=[options] <video-file>"
```

### Basic Options

| Short | Long        | Description                  |
| ----- | ----------- | ---------------------------- |
| -h    | --help      | Show help message            |
| -a    | --audio     | Enable audio                 |
| -x    | --maximise  | Open the player maximised    |
| -1    | --monitor-1 | Open the player on display 1 |
| -2    | --monitor-2 | Open the player on display 2 |

### Frame Processors

| Short | Long              | Description                              |
| ----- | ----------------- | ---------------------------------------- |
| -n    | --number-frames   | Render the frame number onto each frame  |
| -s    | --scratch-frames  | Render vertical film scratches           |
| -f    | --flicker-frames  | Randomly dim frames                      |
| -w    | --black-and-white | Convert frames to black and white        |
| -y    | --yellow-frames   | Apply a warmer colour temperature        |
| -v    | --vignette-frames | Darken the frame edges                   |
| -d    | --dust-frames     | Render dust and hair marks               |
| -j    | --jitter-frames   | Randomly displace frames by a few pixels |
| -m    | --mottle-frames   | Add cloudy emulsion mottling             |
| -b    | --bleed-frames    | Bleed light into frames                  |
| -p    | --pepper-frames   | Pepper frames with dark spots/blotches   |

Frame processors are applied in command-line order and repeated. The following are all equivalent:

```Shell
-j -f -d -d -n
-jfddn
-jf --dust-frames -d --number-frames
```

### Example Commands

```Shell
# Show help message
mvn javafx:run "-Djavafx.args=--help"

# Play video.mp4 with audio, maximised, on display 2
mvn javafx:run "-Djavafx.args=-a -x -2 video.mp4"

# Play video.mp4 with audio and several frame processors applied
mvn javafx:run "-Djavafx.args=-a -nsfwy video.mp4"
```

Concurrency Design

* `numCores`: sets the number of cores available to the player. It reserves one core for the FX thread and one for rendering.
* `BlockingQueue<RawFrame> rawFrames`: stores up to `numCores` raw frames for the worker threads to process in parallel. The blocking ensures that the player is not reading frames ahead of the available workers.
* `ConcurrentHashMap<Integer, PreparedFrame> processedFrames`, `nextFrameNumber`: a concurrent hash map stores the prepared frames and uses the frame number as the key. The player uses the key and `nextFrameNumber` to display the correct frame.
* `interrupt()`: every thread is interrupted before joining which forces it out of any `put()` or `acquire()` blocks.
* Separate instance for `FrameMottler`: a thread can set a ready flag for the shared array before the array is fully complete, and another thread accesssing the array could return null. Separate instances ensure that each thread is only accessing a complete array.
* Separate instance for `FrameBleeder`: bleed frames reuse the mask arrays and calculations can be overwritten if multiple threads call `process()` on the same FrameBleeder instance. Separate instances ensure that calculations do not get overwritten by another thread.
* `isGettingFrames`, `playbackOpen`: declared `volatile` so that a change made on one thread (e.g. the FX thread stopping playback) is guaranteed to be visible for other threads checking these flags in a loop. Without `volatile`, a background thread may never notice the flag change and looping forever.
* `Semaphore frameSlots`: acquired before a worker runs `processedFrames.put()`, released once `prepareNextFrame` displays it the prepared frame. This limits how many prepared frames can be stored at once, which prevents processedFrames from growing too large and causing memory errors.

Process

1. `getFramesLoop` calls `readNextVideoFrame` and stores the raw frames in `rawFrames` for processing
2. A pool of `numCores` worker threads run `processFramesLoop` which takes a raw frame, applies the frame processors in order, and converts it to a JavaFX Image. This step is parallel as multiple frames are processed at once and may finish out of order
3. The processed frames are stored in `processedFrames`. `prepareNextFrame` pulls the next frame to display from this map using `nextFrameNumber` to display the correct frame

## Testing

### Command Line

`CommandLineControllerTest.java` is a unit test for:

* Argument parsing
* Help message
* Frame processor combinations
* Missing/non-existent video file
* Unknown options
* Case sensitivity

Run with:

```Shell
mvn test
```

### Frame Processor Threading

`FrameProcessorThreadTest.java` is a unit test for:

* Correct frame processor instance creation
* Separate `FrameMottler` and `FrameBleeder` instances
* Identical `instanceIndex` for `FrameMottler` instances of the same index
* Different `FrameMottler` instances for different index

Run with:

```Shell
mvn test
```

### Load Comparison

`LoadComparisonTestIT.java` is an integration test for:

* Positive and negative headroom
* Playback time improvement between serial and parallel implementations

The file name ends in `IT` so it only runs via Failsafe in `mvn verify`.

Run with:

```Shell
mvn verify
```

## Attribution

Modifications to the supplied files are attributed in the header. Code adapted from external sources are cited with inline comments. AI assisted code is marked with an inline comment at the relevant code block.
