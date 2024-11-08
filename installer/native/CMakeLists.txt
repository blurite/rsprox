cmake_minimum_required(VERSION 3.21)
cmake_policy(SET CMP0091 NEW)
project(packr C CXX)

set(CMAKE_C_STANDARD 11)
set(CMAKE_CXX_STANDARD 11)

find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})

set(THREADS_PREFER_PTHREAD_FLAG ON)
find_package(Threads REQUIRED)

set(CMAKE_SKIP_RPATH TRUE)
set(CMAKE_OSX_DEPLOYMENT_TARGET "10.9")

if(MSVC)
    # this is undocumented - it replaces the timestamp in the PE with -1. requires incremental linking to be off.
    add_link_options("/Brepro")
endif()

include_directories(dropt/include)
add_library(dropt STATIC
    dropt/src/dropt.c
    dropt/src/dropt_handlers.c
    dropt/src/dropt_string.c
    dropt/include/dropt.h
    dropt/include/dropt_string.h)
set_property(TARGET dropt PROPERTY MSVC_RUNTIME_LIBRARY "MultiThreaded")

include_directories(sajson/include)

add_subdirectory(src)
