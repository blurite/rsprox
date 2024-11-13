#!/bin/bash

set -e

cmake -S installer/liblauncher -B installer/liblauncher/build32 -A Win32
cmake --build installer/liblauncher/build32 --config Release

pushd installer/native
cmake -B build-x86 -A Win32
cmake --build build-x86 --config Release
popd

source installer/scripts/.jdk-versions.sh

rm -rf installer/build/win-x86
mkdir -p installer/build/win-x86

if ! [ -f win32_jdk.zip ] ; then
    curl -Lo win32_jdk.zip $WIN32_LINK
fi

echo "$WIN32_CHKSUM win32_jdk.zip" | sha256sum -c

cp installer/native/build-x86/src/Release/RSProx.exe installer/build/win-x86/
cp rsprox-launcher.jar installer/build/win-x86/
cp installer/packr/win-x86-config.json installer/build/win-x86/config.json
cp installer/liblauncher/build32/Release/launcher_x86.dll installer/build/win-x86/

unzip win32_jdk.zip
mv jdk-$WIN32_VERSION-jdk installer/build/win-x86/jdk

echo RSProx.exe 32bit sha256sum
sha256sum installer/build/win-x86/RSProx.exe

dumpbin //HEADERS installer/build/win-x86/RSProx.exe

# We use the filtered iss file
iscc installer/innosetup/rsprox32.iss
