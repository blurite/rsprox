cmake_minimum_required(VERSION 3.21)
project(launcher CXX)

set(CMAKE_CXX_STANDARD 17)

find_package(JNI REQUIRED)

add_library(launcher SHARED main.cpp reg.cpp elevation.cpp acl.cpp)
include_directories(../detours/include ${JNI_INCLUDE_DIRS})
if (CMAKE_GENERATOR_PLATFORM STREQUAL "x64")
    set_target_properties(launcher PROPERTIES OUTPUT_NAME "launcher_amd64")
    target_link_libraries(launcher ${CMAKE_SOURCE_DIR}/../detours/lib.X64/detours.lib)
elseif (CMAKE_GENERATOR_PLATFORM STREQUAL "Win32")
    set_target_properties(launcher PROPERTIES OUTPUT_NAME "launcher_x86")
    target_link_libraries(launcher ${CMAKE_SOURCE_DIR}/../detours/lib.X86/detours.lib)
elseif (CMAKE_GENERATOR_PLATFORM STREQUAL "ARM64")
    set_target_properties(launcher PROPERTIES OUTPUT_NAME "launcher_aarch64")
    target_link_libraries(launcher ${CMAKE_SOURCE_DIR}/../detours/lib.ARM64/detours.lib)
endif()
set_property(TARGET launcher PROPERTY MSVC_RUNTIME_LIBRARY "MultiThreaded") # multi-threaded statically-linked runtime
target_compile_options(launcher PRIVATE /W4 /WX /wd4100)