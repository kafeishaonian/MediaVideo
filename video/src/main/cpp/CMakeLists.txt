# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)

# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# Since this is the top level CMakeLists.txt, the project name is also accessible
# with ${CMAKE_PROJECT_NAME} (both CMake variables are in-sync within the top level
# build script scope).
project("video")

# 支持gnu++11
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++17")
find_library(Open_LIBRARY PATHS ${CMAKE_ANDROID_NDK}/sources/android/native_app_glue)


# 1. 定义so库和头文件所在目录，方面后面使用
set(ffmpeg_lib_dir ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI})
set(ffmpeg_head_dir ${CMAKE_SOURCE_DIR})

# 2. 添加头文件目录
include_directories(${ffmpeg_head_dir}/include)


# 3. 添加ffmpeg相关的so库
add_library(ffmpeg SHARED IMPORTED)
set_target_properties(ffmpeg PROPERTIES IMPORTED_LOCATION ${ffmpeg_lib_dir}/libffmpeg.so)



add_library(${CMAKE_PROJECT_NAME} SHARED
        video.cpp
        BaseDecoder.cpp)


target_link_libraries(${CMAKE_PROJECT_NAME}
        android
        ffmpeg
        Open_LIBRARY
        log)