# Fast Mixer

### Video Preview

[![See app preview](https://i.imgur.com/Ab8pHij.png)](https://youtu.be/v9aF8kAckFM)

This project is an open-source sound recorder and mixer for Android based mobile devices and tablets. This app can,

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

This is an ongoing project using [google/oboe](https://github.com/google/oboe) c++ library. This is going to be an easy to use mixer for recorded streams and external audio files. I want to credit [sheraz-nadeem/oboe_recorder_sample](https://github.com/sheraz-nadeem/oboe_recorder_sample). This project helped me to understand the required architecture of such a project.

To run this project install NDK and CMake in Android Studio.

This project uses some C++ projects (oboe and libsndfile) as submodules. After cloning run below command,

```bash
git submodule update --init
```

to download all of the C++ dependencies required by this project. Then this project can be built and run.

Then run the following commands to build ffmpeg (through the provided bash script).

```bash
cd cpp_dependencies/ffmpeg-scripts
./ffmpeg-build.sh
```

This will build ffmpeg and move it to the expected directory.

Then copy ```local.example.properties``` content in ```local.properties``` and tweak it according to your need.

This README.md file will further be enhanced later.
