# Fast Mixer

### Video Preview

[![See app preview](https://user-images.githubusercontent.com/73867778/111960007-c3cb6300-8b21-11eb-917e-ce8d401548c2.pn)](https://youtu.be/v9aF8kAckFM)

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
