# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

project(FastMixer)

cmake_minimum_required(VERSION 3.4.1)

include_directories(taskqueue)

include_directories(audio)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        recordingEngine

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        recording/RecordingIO.cpp
        recording/RecordingEngine.cpp
        recording/streams/StreamConstants.cpp
        recording/streams/RecordingStream.cpp
        recording/streams/LivePlaybackStream.cpp
        recording/streams/PlaybackStream.cpp
        recording/streams/RecordingBaseStream.cpp
        streams/BaseStream.cpp

        synthesizers/BaseSynthesizer.h
        synthesizers/HighPassFilter.cpp
        synthesizers/LowPassFilter.cpp

        audio/FileDataSource.cpp
        audio/Player.cpp
        audio/FFMpegExtractor.cpp
        recording/recording-lib.cpp)

add_library( # Sets the name of the library.
        mixingEngine

        #Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        mixing/MixingIO.cpp
        mixing/MixingEngine.cpp
        mixing/streams/StreamConstants.cpp
        mixing/streams/PlaybackStream.cpp
        mixing/streams/MixingBaseStream.cpp
        streams/BaseStream.cpp

        audio/FileDataSource.cpp
        audio/Player.cpp
        audio/FFMpegExtractor.cpp
        mixing/mixing-lib.cpp)

# Build the Oboe library
set (OBOE_DIR ../../../../cpp_dependencies/oboe)
add_subdirectory (${OBOE_DIR} ./oboe)

# Make the Oboe public headers available to our app
include_directories (${OBOE_DIR}/include)

### ADD LIBSNDFILE LIBRARY
set(LIB_SND_FILE_DIR ../../../../cpp_dependencies/libsndfile)
add_subdirectory(${LIB_SND_FILE_DIR} ./sndfile)
include_directories(${LIB_SND_FILE_DIR}/src)

set(KISSFFT_TEST OFF)
set(KISS_FFT_DIR ../../../../cpp_dependencies/kissfft)
add_subdirectory(${KISS_FFT_DIR} ./kissfft)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log )

set(FFMPEG_DIR ${PROJECT_SOURCE_DIR}/../../../../cpp_dependencies/ffmpeg-scripts/ffmpeg-build/${ANDROID_ABI})
include_directories(audioEngine ${FFMPEG_DIR}/include)

add_library( avformat SHARED IMPORTED)
set_target_properties(avformat PROPERTIES IMPORTED_LOCATION
        ${FFMPEG_DIR}/lib/libavformat.so)

add_library( avutil SHARED IMPORTED)
set_target_properties(avutil PROPERTIES IMPORTED_LOCATION
        ${FFMPEG_DIR}/lib/libavutil.so)

add_library( avcodec SHARED IMPORTED)
set_target_properties(avcodec PROPERTIES IMPORTED_LOCATION
        ${FFMPEG_DIR}/lib/libavcodec.so)

add_library( swresample SHARED IMPORTED)
set_target_properties(swresample PROPERTIES IMPORTED_LOCATION
        ${FFMPEG_DIR}/lib/libswresample.so)

add_library( avfilter SHARED IMPORTED)
set_target_properties(avfilter PROPERTIES IMPORTED_LOCATION
        ${FFMPEG_DIR}/lib/libavfilter.so)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

if(USEHWASAN)
    target_compile_options(recordingEngine PUBLIC -fsanitize=hwaddress -fno-omit-frame-pointer)
    set_target_properties(recordingEngine PROPERTIES LINK_FLAGS -fsanitize=hwaddress)

    target_compile_options(mixingEngine PUBLIC -fsanitize=hwaddress -fno-omit-frame-pointer)
    set_target_properties(mixingEngine PROPERTIES LINK_FLAGS -fsanitize=hwaddress)
endif(USEHWASAN)

if(USEASAN)
    target_compile_options(recordingEngine PUBLIC -fsanitize=address -fno-omit-frame-pointer)
    set_target_properties(recordingEngine PROPERTIES LINK_FLAGS -fsanitize=address)

    target_compile_options(mixingEngine PUBLIC -fsanitize=address -fno-omit-frame-pointer)
    set_target_properties(mixingEngine PROPERTIES LINK_FLAGS -fsanitize=address)
endif(USEASAN)

target_link_libraries(
        recordingEngine
        ${log-lib}
        oboe
        android
        sndfile
        kissfft
        avformat
        avutil
        avcodec
        swresample
        avfilter
)

target_link_libraries(
        mixingEngine
        ${log-lib}
        oboe
        android
        sndfile
        kissfft
        avformat
        avutil
        avcodec
        swresample
        avfilter
)
