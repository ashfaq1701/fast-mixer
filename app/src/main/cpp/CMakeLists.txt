# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

set(FFMPEG_DIR ${PROJECT_SOURCE_DIR}/../../../../cpp_dependencies/ffmpeg-build/${ANDROID_ABI})
include_directories(${FFMPEG_DIR}/include)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             native-lib.cpp )

# Build the Oboe library
set (OBOE_DIR ../../../../cpp_dependencies/oboe)
add_subdirectory (${OBOE_DIR} ./oboe)

# Make the Oboe public headers available to our app
include_directories (${OBOE_DIR}/include)

### ADD LIBSNDFILE LIBRARY
set(LIB_SND_FILE_DIR ../../../../cpp_dependencies/libsndfile)
add_subdirectory(${LIB_SND_FILE_DIR} ./sndfile)
include_directories(${LIB_SND_FILE_DIR}/src)

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

target_link_libraries( # Specifies the target library.
                       native-lib
                       avformat
                       avutil
                       avcodec
                       swresample
                       avfilter
                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib}
                       oboe
                       sndfile)