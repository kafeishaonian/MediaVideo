
cmake_minimum_required(VERSION 3.22.1)


project("x264_video_encode")


set(X264_PATH ${CMAKE_SOURCE_DIR}/x264)
set(X264_LIB ${X264_PATH}/libs/${CMAKE_ANDROID_ARCH_ABI}/libx264.a)
set(X264_H ${X264_PATH}/include/)


include_directories(${X264_H})

add_library(X264 STATIC IMPORTED)
set_target_properties(X264 PROPERTIES IMPORTED_LOCATION ${X264_LIB})

add_library(${CMAKE_PROJECT_NAME} SHARED
        X264Encoder.cpp
        x264_video_encode.cpp)


target_link_libraries(${CMAKE_PROJECT_NAME}
        android
        X264
        log)