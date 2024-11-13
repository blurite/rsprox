#!/bin/bash

set -e

cmake -S installer/liblauncher -B installer/liblauncher/build64 -A x64
cmake --build installer/liblauncher/build64 --config Release

pushd installer/native
cmake -B build-x64 -A x64
cmake --build build-x64 --config Release
popd

source installer/scripts/.jdk-versions.sh

rm -rf installer/build/win-x64
mkdir -p installer/build/win-x64

if ! [ -f win64_jdk.zip ] ; then
    curl -Lo win64_jdk.zip $WIN64_LINK
fi

echo "$WIN64_CHKSUM win64_jdk.zip" | sha256sum -c

cp installer/native/build-x64/src/Release/RSProx.exe installer/build/win-x64/
cp rsprox-launcher.jar installer/build/win-x64/
cp installer/packr/win-x64-config.json installer/build/win-x64/config.json
cp installer/liblauncher/build64/Release/launcher_amd64.dll installer/build/win-x64/

unzip win64_jdk.zip
mv jdk-$WIN64_VERSION-jdk installer/build/win-x64/jdk

echo RSProx.exe 64bit sha256sum
sha256sum installer/build/win-x64/RSProx.exe

dumpbin //HEADERS installer/build/win-x64/RSProx.exe

# We use the filtered iss file
iscc installer/innosetup/rsprox.iss
