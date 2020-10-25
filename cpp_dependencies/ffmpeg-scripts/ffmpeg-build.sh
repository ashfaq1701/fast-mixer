#!/bin/bash
if [ -z "$ANDROID_NDK" ]; then
  echo "Please set ANDROID_NDK to the Android NDK folder"
  exit 1
fi

# Set ANDROID_NDK environment variable example, export ANDROID_NDK="/Users/ashfaq/Library/Android/sdk/ndk/21.3.6528147"

HOST_OS_ARCH=darwin-x86_64

function configure_ffmpeg {
  ABI=$1
  PLATFORM_VERSION=$2
  TOOLCHAIN_PATH=$ANDROID_NDK/toolchains/llvm/prebuilt/${HOST_OS_ARCH}/bin
  local STRIP_COMMAND

  case ${ABI} in
  armeabi-v7a)
    TOOLCHAIN_PREFIX=armv7a-linux-androideabi
    STRIP_COMMAND=arm-linux-androideabi-strip
    ARCH=armv7-a
    ;;
  arm64-v8a)
    TOOLCHAIN_PREFIX=aarch64-linux-android
    ARCH=aarch64
    ;;
  x86)
    TOOLCHAIN_PREFIX=i686-linux-android
    ARCH=x86
    EXTRA_CONFIG="--disable-asm"
    ;;
  x86_64)
    TOOLCHAIN_PREFIX=x86_64-linux-android
    ARCH=x86_64
    EXTRA_CONFIG="--disable-asm"
    ;;
  esac

  if [ -z ${STRIP_COMMAND} ]; then
    STRIP_COMMAND=${TOOLCHAIN_PREFIX}-strip
  fi

  echo "Configuring FFmpeg build for ${ABI}"
  echo "Toolchain path ${TOOLCHAIN_PATH}"
  echo "Command prefix ${TOOLCHAIN_PREFIX}"
  echo "Strip command ${STRIP_COMMAND}"

  ./configure \
  --prefix=build/${ABI} \
  --target-os=android \
  --arch=${ARCH} \
  --enable-cross-compile \
  --cc=${TOOLCHAIN_PATH}/${TOOLCHAIN_PREFIX}${PLATFORM_VERSION}-clang \
  --strip=${TOOLCHAIN_PATH}/${STRIP_COMMAND} \
  --enable-small \
  --disable-programs \
  --disable-doc \
  --enable-shared \
  --disable-static \
  --disable-everything \
  --enable-demuxer=aac \
  --enable-demuxer=ac3 \
  --enable-demuxer=avi \
  --enable-demuxer=flac \
  --enable-demuxer=flv \
  --enable-demuxer=matroska \
  --enable-demuxer=mpc \
  --enable-demuxer=mpc8 \
  --enable-demuxer=ogg \
  --enable-demuxer=mp3 \
  --enable-demuxer=wav \
  --enable-demuxer=pcm_s32be \
  --enable-demuxer=pcm_s32le \
  --enable-demuxer=pcm_s24be \
  --enable-demuxer=pcm_s24le \
  --enable-demuxer=pcm_s16be \
  --enable-demuxer=pcm_s16le \
  --enable-demuxer=pcm_s8 \
  --enable-demuxer=pcm_u32be \
  --enable-demuxer=pcm_u32le \
  --enable-demuxer=pcm_u24be \
  --enable-demuxer=pcm_u24le \
  --enable-demuxer=pcm_u16be \
  --enable-demuxer=pcm_u16le \
  --enable-demuxer=pcm_u8 \
  --enable-decoder=aac \
  --enable-decoder=aac_latm \
  --enable-decoder=ac3 \
  --enable-decoder=flac \
  --enable-decoder=mp1 \
  --enable-decoder=mp1float \
  --enable-decoder=mp2 \
  --enable-decoder=mp2float \
  --enable-decoder=mp3 \
  --enable-decoder=mp3adu \
  --enable-decoder=mp3adufloat \
  --enable-decoder=mp3float \
  --enable-decoder=mp3on4 \
  --enable-decoder=mp3on4float \
  --enable-decoder=mpc7 \
  --enable-decoder=mpc8 \
  --enable-decoder=wavpack \
  --enable-decoder=wmalossless \
  --enable-decoder=wmapro \
  --enable-decoder=wmav1 \
  --enable-decoder=wmav2 \
  --enable-decoder=wmavoice \
  --enable-decoder=pcm_f32be \
  --enable-decoder=pcm_f32le \
  --enable-decoder=pcm_f64be \
  --enable-decoder=pcm_f64le \
  --enable-decoder=pcm_s8 \
  --enable-decoder=pcm_s8_planar \
  --enable-decoder=pcm_s16be \
  --enable-decoder=pcm_s16be_planar \
  --enable-decoder=pcm_s16le \
  --enable-decoder=pcm_s16le_planar \
  --enable-decoder=pcm_s24be \
  --enable-decoder=pcm_s24daud \
  --enable-decoder=pcm_s24le \
  --enable-decoder=pcm_s24le_planar \
  --enable-decoder=pcm_s32be \
  --enable-decoder=pcm_s32le \
  --enable-decoder=pcm_s32le_planar \
  --enable-decoder=pcm_u8 \
  --enable-decoder=pcm_u16be \
  --enable-decoder=pcm_u16le \
  --enable-decoder=pcm_u24be \
  --enable-decoder=pcm_u24le \
  --enable-decoder=pcm_u32be \
  --enable-decoder=pcm_u32le \
  --enable-parser=aac \
  --enable-parser=aac_latm \
  --enable-parser=ac3 \
  --enable-parser=flac \
  --enable-parser=mpegaudio \
  ${EXTRA_CONFIG}
  
  return $?
}

function build_ffmpeg {
  configure_ffmpeg $1 $2

  if [ $? -eq 0 ]
  then
          make clean
          make -j12
          make install

  else
          echo "FFmpeg configuration failed, please check the error log."
  fi
}

cd ./ffmpeg
build_ffmpeg armeabi-v7a 16
build_ffmpeg arm64-v8a 21
build_ffmpeg x86 16
build_ffmpeg x86_64 21
rm -rf ../ffmpeg-build && mv ./build ../ffmpeg-build
