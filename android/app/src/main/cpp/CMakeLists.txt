cmake_minimum_required(VERSION 3.22.1)

project("pocketgpt_native")

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -ffast-math -std=c++17")

add_library(
        pocketgpt_native
        SHARED
        native_lib.cpp)

find_library(
        log-lib
        log)

target_link_libraries(
        pocketgpt_native
        ${log-lib})
