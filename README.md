# Mixi

### Video Preview

[![See app preview](https://i.imgur.com/Ab8pHij.png)](https://youtu.be/v9aF8kAckFM)

This project is an open-source sound recorder and mixer for Android based mobile devices and tablets.

### Technical Overview

* This project is done using the latest android architecture components, MVVM, LiveData, Navigation, Data Binding, Coroutines etc.
* Used Hilt for dependency injection.
* Used RxJava and reactive programming patten to act against various view events.
* Designed some complex custom views doing heavy rendering jobs. Used RxJava observers heavily inside those views to react promptly against UI actions.
* Performed all heavy computation and IO tasks in background threads (IO Dispatcher and Default Dispatcher).
* NDK heavy project. Most of the operational parts of the project are done as native codes. The solution is scalable and will allow many layers of further improvements without major structural refactor.
* The recording done through this app and the mixed files written through this app will have the following properties,
  * Sample rate: 48kHz (will be improved in future)
  * Channel count: 2 (Stereo)
* Uses FFMpeg for decoding audio and libsndfile to write into wav file.
* To track memory leaks ASAN and HWASAN are used.


### Installation

To run this project install NDK and CMake in Android Studio.

This project uses some C++ projects (oboe, libsndfile and FFMpeg) as submodules. After cloning run below command,

```bash
git submodule update --init
```

to download all of the C++ dependencies required by this project. Then this project can be built and run.

A build of FFMpeg is already pushed into this project. But if you want to build FFMpeg yourself then run the following bash script.

```bash
cd cpp_dependencies/ffmpeg-scripts
./ffmpeg-build.sh
```

This will build ffmpeg and move it to the expected directory.

Then copy ```local.example.properties``` content in ```local.properties``` and tweak it according to your need.

  
### Interesting code chunks and implementation details
* The way recording buffer is written into a file is non-blocking and done in a background thread. 
  * Recorded buffer is taken and added to a bigger buffer with a capacity of 30 seconds audio.  
    [RecordingIO::write()](app/src/main/cpp/recording/RecordingIO.cpp#L95)
  * Once the bigger buffer is about to become full, flush_buffer function triggers. This function copies those data into a new pre-allocated temporary buffer, and frees up the main buffer. Then it notifies by the mutex that main buffer is ready to accept new audio samples. After that it enqueues a lambda worker to the TaskQueue which is a FIFO queue,  
    [RecordingIO::flush_buffer()](app/src/main/cpp/recording/RecordingIO.cpp#L118)
  * Inside of the fifo queue the worker creates an LIBSNDFile handle and appends the audio data to the passed file descriptor. This is done in separate FIFO queue, so this is non-blocking.  
    [RecordingIO::flush_to_file()](app/src/main/cpp/recording/RecordingIO.cpp#L85)
  * But there is a catch, the lock used while flushing buffer, inside of the write function, still locks the callback thread for a very short time. This should not be done, callback thread should not be locked at all. In a later fix, this lock will be removed by adding a new backup buffer.  
    [RecordingIO::write()](app/src/main/cpp/recording/RecordingIO.cpp#L99)

* Live playback stream reads from the audio write buffer when there is data available. It maintains it's own read pointer which eventually resets to the current write pointer, when recording is paused and replayed.  
  [RecordingIO::read_live_playback()](app/src/main/cpp/recording/RecordingIO.cpp#L145)
  
* TaskQueue is a fifo queue that takes write buffer flushing tasks and executes them one after another, in a background thread.  
  [TaskQueue](app/src/main/cpp/taskqueue/TaskQueue.h#L53)
   
* The way audio samples of a source are summarized to a specified number of samples (for visualization) is interesting and optimal, this code is worth looking.  
  [FileDataSource::readData](app/src/main/cpp/audio/FileDataSource.cpp#L104)

* While mixing multiple sources we need to normalize the mixed audio to preserve original loudness. For this we need max(A[i] + B[i] + C[i]), where A, B and C are 3 loaded sources. When A, B and C are decoded audios loaded into RAM, this operation can be very time consuming, delaying the total mixing operation. This is done by dividing the work into multiple worker threads and then combining the results. The number of workers are kept dynamic.  
  [SourceStore::updateAddedMax](app/src/main/cpp/audio/SourceStore.cpp#L12)
  
* The way player renders specific chunk mixed from multiple sources in real time, in a non-blocking manner, is interesting.  
  [Player::renderAudio](app/src/main/cpp/audio/Player.cpp#L55)
  
* The way mixed audio is written into a file, in multiple passes, doing all of the computation in place, is an interesting implementation too.  
  [MixedAudioWriter::writeToFile](app/src/main/cpp/audio/MixedAudioWriter.cpp#L10)
  
* Obviously the decoded sources are kept in a single place and in an efficient manner. While sharing them with Writer, Player or any other object, a shared_ptr or a map with shared_ptr's are shared and data copy is avoided at all cost. Also the lifecycle of those objects are carefully monitored, to prevent any unintentional crash.  
  [SourceMapStore::sourceMap](app/src/main/cpp/SourceMapStore.h)
  
* To update ui state properly when a audio finishes playing, reverse call from C++ - Kotlin is made. The way it is done is interesting too,  
  [RecordingEngine::setStopPlayback](app/src/main/cpp/recording/RecordingEngine.cpp#L305)  
  [prepare_kotlin_recording_method_ids](app/src/main/cpp/recording/recording-lib.cpp#L17)
  
* In Kotlin side while reading from file, we used new Scoped Storage, which doesn't need any permission. From Kotlin to native code a FileDescriptor is passed, which is obtained from the ContentManager URI.  
  [RecordingScreenViewModel::addRecordedFilePath](app/src/main/java/com/bluehub/mixi/screens/mixing/MixingScreenViewModel.kt#L188)  
  [FileManager::getReadOnlyFdForPath](app/src/main/java/com/bluehub/mixi/common/utils/FileManager.kt#L33)
  
* While writing file as media using scoped storage, we first get the file descriptor for the media storage and then pass the file descriptor to C++ for writing,  
  [WriteViewModel::performWrite](app/src/main/java/com/bluehub/mixi/screens/mixing/modals/WriteViewModel.kt#L54)  
  [FileManager::getFileDescriptorForMedia](app/src/main/java/com/bluehub/mixi/common/utils/FileManager.kt#L45)
  
* The whole custom view FileWaveView, pulls audio data into background thread, does the scaling efficiently and then does heavy rendering on the screen in an efficient and non-blocking manner too. This can be seen by navigating through the series of function calls fetchPointsToPlot, processPlotPoints and createAndDrawCanvas.  
  [fetchPointsToPlot](app/src/main/java/com/bluehub/mixi/common/views/FileWaveView.kt#L183)  
  
Other than these too, the whole codebase has many other solutions of interesting and complex problems. While doing this I faced countless issues and tried to solve them in standard manners and following the best practices.
  

### Features

### Recording Screen
* Record audio from device's primary microphone or input device attached to the device.
* While recording, it shows a visualizer and timer to visualize the record operation.
* There is a feature called `Live Playback`, which can be enabled only if wired headphones are connected. This will relay what the user is recording to the connected headphone in real time. The checkbox to enable live playback will be enabled if wired headphones are connected to the device.
* There is a feature called `Mixing Play`. This will play the audios loaded into the app in the mixing screen, while recording. This will make it easy to load a track into the app and then sing with the track. To avoid echo, user should use headphone while using the `Mixing Play feature`.
* The recorded audio can be played from the recording screen and can be seeked using the seekbar in the recording screen.
* From recording screen user can play the recorded sound along with the other sounds loaded into the mixing screen. This can be done using `Play Mixed` button.
* Recording is incremental. User can pause and record again. `Mixing Play` will always align with the current recording position.
* User can reset the recording at any time. This action will delete the recorded file and will reset all of the timers.
* User can press `Go back` button or press hardware back button to get back to the mixing screen. Going back will load the recorded audio into memory and show a widget into the Mixing Screen.

### Mixing Screen
* In the mixing screen user can press `Record` to navigate to the recording screen.
* User can press `Read from file`. This will launch a storage file selector, user can select any `mp3` or `wav` file. The file will be decoded and loaded into the RAM as a new data source. Once the read is finished, user will see a widget, inside which there will be a wave view of the newly added file and also some controls to manipulate the ui / cached audio.
* Wave view of different sources will be plotted based on their relative duration. Longest video will take the full screen width, while the shorter ones will take width according to their relative proportion with the longest source's width. 
* If the user rotates the device, the longest audio will take the new full-width and the shorter ones will adjust themselves accordingly.
* User can Zoom in the waveview by pressing the "+" button located at the bottom of each waveview. This will increase the waveview resolution by a factor of 1.5.
* After zooming in if the waveview width gets bigger than the device width, then a Scrollbar will get visible, using which user can slide the waveview.
* If after many zoom in actions, the scrollbar becomes over narrow, and can't be dragged, then user can slide the waveview using the arrow buttons placed above of each waveview.
* Similarly user can Zoom out by pressing the `-` button located at the bottom of each waveview.
* From this webview user can press play icon to play each individual source. There will be a play marker which will progress along the playback progression.
* Any position inside of the wave view can be long pressed to reposition the play marker at that position.
* Inside each of the wave view widget, there is a segment selection icon, which when pressed, will create an initial segment boundary inside of the waveview.
* Area inside the segment selector can be long pressed to resize the segment selector. Long pressing near the left edge and then dragging to a new position will reposition the left side. Long pressing near the right edge and then dragging to a new position will reposition the right edge.
* Segment selector can be adjusted more precisely by clicking into Segment Adjustment menu item and then entering start and duration in milliseconds.
* Selected segment can be copied, cut into clipboard, or muted by selecting the appropriate dropdown menu item.
* When there is data into the clipboard, paste menu item will be enabled. Pressing the paste menu item will then paste the segment from the clipboard into the current position of the play marker. 
* Any source can be given a certain gain (in dB unit) by clicking on the Gain Adjustment menu item and then positioning the slider to the appropriate gain value.
* Any position in the audio source can be shifted by a certain millisecond value by clicking the `Shift` menu item from the dropdown menu.
* The arrow in the bottom - left position of the screen can be tapped to pop-up the bottom drawer.
* Bottom drawer has a item called `Paste New`, which when pressed will paste the segment from clipboard as a new source.
* Pressing the play button in the bottom drawer will mix the audio in real-time and will play the mixed sound in the audio output. This is called the `Group Play` feature.
* Group play will show a transparent overlay on the screen. Group play position can be slided by sliding on the seekbar.
* A segment can be selected from the `Group play overlay` by adjusting the range slider and then tapping on group play icon again. `Apply` button in the overlay will apply the bounds defined by the range slider to each of the sources in the screen by setting `Segment Selectors` in each of them.
* `Zoom In` button in the bottom drawer can be pressed to Zoom in all of the sources together, maintaining their display width ratio.
* `Zoom Out` button in the bottom drawer can be pressed to Zoom out all of the sources together, maintaining their display width ratio.
* `Reset` button in the bottom drawer can be pressed to reset each of the source's Zoom Level to a value of 1, restoring their relative width ratio.
* Finally `Write to disk` button at the top can be pressed to write the mixed audio to the public media storage. A auto generated file name will be suggested in the beginning, which can be changed to give the output file a preferred name.


### Code Walk-through
A brief navigation to the project's architecture is given below,

#### Kotlin (UI) part:
UI portion of the app is built with Kotlin,

* [screens](app/src/main/java/com/bluehub/mixi/screens) - This folder contains the screen related classes, such as activities, fragments and view models.
* [screens/mixing](app/src/main/java/com/bluehub/mixi/screens/mixing) - This folder contains the mixing screen fragment, view model, repository and shared stores to share ui states between multiple view widgets.
* [screens/mixing/FileWaveViewStore](app/src/main/java/com/bluehub/mixi/screens/FileWaveViewStore.kt) - As audio sources in this application needs to interact with each other to maintain relative width, this class is used as a centralized source of shared UI states. Instance of this object is dependency injected in all of the wave view instances.
* [screens/mixing/AudioFileStore](app/src/main/java/com/bluehub/mixi/screens/mixing/AudioFileStore.kt) - Audio file UI states need to be preserved across app's navigation and also need to shared with the recording screen. This class acts a centralized source for the current list of loaded files.
* [screens/mixing/PlayFlagStore](app/src/main/java/com/bluehub/mixi/screens/mixing/PlayFlagStore.kt) - Play states and Group Play state of the application needs to be shared among different parts of the code. So this is also implemented as a separate store, so that it can be dependency injected.
* [screens/mixing/modals](app/src/main/java/com/bluehub/mixi/screens/mixing/modals) - Popup models inside of the mixing screen.
* [screens/recording](app/src/main/java/com/bluehub/mixi/screens/recording) - Recording screen related stuffs. Fragment, view model and repository.
* [common/di/screens](app/src/main/java/com/bluehub/mixi/common/di/screens) - This project uses hilt, not dagger, so for DI I didn't need to create a lot of modules, components etc. For creating an instance of RxPermission, I needed to create a module, which is injected into FragmentComponent directly. Hilt is great.
* [common/fragments](app/src/main/java/com/bluehub/mixi/common/fragments) - Base fragment and base dialog fragment with bootstrapping code.
* [common/repositories](app/src/main/java/com/bluehub/mixi/common/repositories) - Just some audio utility classes.
* [common/utils](app/src/main/java/com/bluehub/mixi/common/utils) - Some random utility classes.
* [common/viewmodel](app/src/main/java/com/bluehub/mixi/common/viewmodel) - Base view model with some bootstrapping code.
* [common/views](app/src/main/java/com/bluehub/mixi/common/views) - All the custom views used in the app.
* [common/views/FileWaveView](app/src/main/java/com/bluehub/mixi/common/views/FileWaveView.kt) - File wave view custom view class. This does heavy rendering job and heavily use RxJava observers.
* [common/views/FileWaveViewWidget](app/src/main/java/com/bluehub/mixi/common/views/FileWaveViewWidget.kt) - Wrapper view around file wave view with control buttons and drop down menu. This view also registers and transforms many RxJava observers.
* [common/views/CustomHorizontalScrollBar](common/views/CustomHorizontalScrollBar.kt) - Custom made horizontal scrollbar. I could not use ScrollView because the scroll gesture I needed for segment resizing. So this scrollbar is created.
* [broadcastReceivers/AudioDeviceChangeListener](app/src/main/java/com/bluehub/mixi/broadcastReceivers/AudioDeviceChangeListener.kt) - Broadcast receiver to listen for changes in connected audio devices. Used to detect headphone or BT Headset connection / disconnection.
* [audio](app/src/main/java/com/bluehub/mixi/audio) - C++ interfaces for this project.
* [audio/MixingEngine](app/src/main/java/com/bluehub/mixi/audio/MixingEngine.kt) - C++ interfaced native functions used in mixing screen.
* [audio/RecordingEngineProxy](app/src/main/java/com/bluehub/mixi/audio/RecordingEngineProxy.kt) - Wrapper class around RecordingEngine to avail dependency injection.
* [audio/MixingEngineProxy](app/src/main/java/com/bluehub/mixi/audio/MixingEngineProxy.kt) - Wrapper class around MixingEngine to avail dependency injection.

#### C++ (Engine) part:
Audio and IO part of the app is done using C++,

* [audio](app/src/main/cpp/audio) - Audio, player, decoder and mixed file writer classes.
* [mixing](app/src/main/cpp/mixing) - Functions used in mixing screen of the application.
* [recording](app/src/main/cpp/recording) - Functions used in recording screen of the application.
* [streams](app/src/main/cpp/streams) - Base stream classes.
* [synthesizers](app/src/main/cpp/synthesizers) - Synthesizers created for this project. Now only one is there, later many synthesizers are planned to be added.
* [taskqueue](app/src/main/cpp/taskqueue) - A single background threaded FIFO queue.
* [utils](app/src/main/cpp/utils) - Utility functions.
* [jvm_env](app/src/main/cpp/jvm_env.h) - An encapsulation class to do operations on JVM environment. Used for calling some functions in reverse path (cpp - java).
* [SourceMapStore](app/src/main/cpp/SourceMapStore.cpp) - A shared singleton class to store the encoded audio files. The singleton instance is shared between recording and mixing engine classes.
* [structs](app/src/main/cpp/structs.h) - Simple structures needed for this app.
* [mixing/mixing-lib](app/src/main/cpp/mixing/mixing-lib.cpp) - Entry point for mixing screen related functions.
* [mixing/MixingEngine](app/src/main/cpp/mixing/MixingEngine.cpp) - Mixing engine class, entrypoint for all mixing related functions.
* [mixing/MixingIO](app/src/main/cpp/mixing/MixingIO.cpp) - Mixing IO class, to perform all IO operations.
* [mixing/streams](app/src/main/cpp/mixing/streams) - Mixing stream classes, playback stream for mixing screen.
* [recording/recording-lib](app/src/main/cpp/recording/recording-lib.cpp) - Entry point for recording screen related functions.
* [recording/RecordingEngine](app/src/main/cpp/recording/RecordingEngine.cpp) - Recording engine class, entrypoint for all recording related functions.
* [recording/RecordingIO](app/src/main/cpp/recording/RecordingIO.cpp) - Recording IO class, to perform all IO operations.
* [recording/streams](app/src/main/cpp/recording/streams) - Recording stream classes, recording stream, live playback stream and playback stream for mixing screen.
* [audio/FFMpegExtractor](app/src/main/cpp/audio/FFMpegExtractor.cpp) - Decoder based on FFMpeg. Decodes audio files and returns a buffer containing all of the decoded audio samples. Works with the file descriptor passed from Kotlin side. This class contains the function which summarizes the audio file to a specified number of points. Used by the visualizer.
* [audio/FileDataSource](app/src/main/cpp/audio/FileDataSource.cpp) - Class with necessary states to represent a decoded file source in the memory.
* [audio/BufferedDataSource](app/src/main/cpp/audio/BufferedDataSource.cpp) - Class with necessary states to represent a buffered source (into clipboard) in the memory.
* [audio/Player](app/src/main/cpp/audio/Player.cpp) - Player class which reads specific number of samples, added and normalized, from all of the loaded sources into the memory. Also holds states such as current play head pointer.
* [audio/MixedAudioWriter](app/src/main/cpp/audio/MixedAudioWriter.cpp) - Class which writes the mixed audio from all of the sources loaded into the memory into the specified file. Target file is passed as a file descriptor from Kotlin.
* [audio/SourceStore](app/src/main/cpp/audio/SourceStore.cpp) - Super class for Player and MixedAudioWriter. Player and MixedAudioWriter shares some common implementation and requires same set of functions. For sharing these implementation without necessarily copying code, we inherited those classes from SourceStore.
* [CMakeLists.txt](app/src/main/cpp/CMakeLists.txt) - CMakeLists file loads all of the necessary classes and external modules into our project.


### Limitation

* The architecture of this project is easily testable. But I didn't get enough time to write unit tests yet, instead I focused on implementing the features. Unit tests will be added later.
* While writing file I used to lock to block the callback for a very short time. Although this doesn't create any problem till now, but this is not a good practice. I will optimize this part later.
* Live playback feature is not instant but has a little delay. I will improve this part later by working with Oboe team.
* Although I hunted down all memory leaks and crashes I could found. But still there can be few left. Please report if you find any.
* UI is not properly done. I will design the UI in a private fork.


### Credits

* The project is mainly done around [google/oboe](https://github.com/google/oboe), which is a low latency native framework for Android.
* I was inspired by seeing [sheraz-nadeem/oboe_recorder_sample](https://github.com/sheraz-nadeem/oboe_recorder_sample). Got the primary idea for the required structure from this project.
* I used [Armen101/AudioRecordView](https://github.com/Armen101/AudioRecordView) as the visualizer in the recording screen. I was against usage of any external UI library, but as this part is not playing any vital role in the app's core process, so I used this visualizer here. I will replace it with own implemnentation in a later phase.
* For decoding audio I used FFMpeg.
* For writing and appending samples to a wav file I used libsndfile.
