# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)

project("fdkaac_audio")

set(FDK_AAC_PATH ${CMAKE_SOURCE_DIR}/fdk_aac)
set(FDK_AAC_LIB ${FDK_AAC_PATH}/libs/${CMAKE_ANDROID_ARCH_ABI}/libfdk-aac.a)
set(FDK_AAC_H ${FDK_AAC_PATH}/include/)


include_directories(${FDK_AAC_H})

add_library(fdk-aac STATIC IMPORTED)
set_target_properties(fdk-aac PROPERTIES IMPORTED_LOCATION ${FDK_AAC_LIB})


add_library(${CMAKE_PROJECT_NAME} SHARED
        decode/AACDecoder.cpp
        encode/AACEncoder.cpp
        fdkaac_audio.cpp)

target_link_libraries(${CMAKE_PROJECT_NAME}
        android
        fdk-aac
        log)