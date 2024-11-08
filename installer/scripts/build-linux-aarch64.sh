#!/bin/bash

set -e

pushd native
cmake -DCMAKE_TOOLCHAIN_FILE=arm64-linux-gcc.cmake -B build-aarch64 .
cmake --build build-aarch64 --config Release
popd

APPIMAGE_VERSION="13"

umask 022

source .jdk-versions.sh

rm -rf build/linux-aarch64
mkdir -p build/linux-aarch64

if ! [ -f linux_aarch64_jre.tar.gz ] ; then
    curl -Lo linux_aarch64_jre.tar.gz $LINUX_AARCH64_LINK
fi

echo "$LINUX_AARCH64_CHKSUM linux_aarch64_jre.tar.gz" | sha256sum -c

# Note: Host umask may have checked out this directory with g/o permissions blank
chmod -R u=rwX,go=rX appimage
# ...ditto for the build process
chmod 644 target/RuneLite.jar

cp native/build-aarch64/src/RuneLite build/linux-aarch64/
cp target/RuneLite.jar build/linux-aarch64/
cp packr/linux-aarch64-config.json build/linux-aarch64/config.json
cp target/filtered-resources/runelite.desktop build/linux-aarch64/
cp appimage/runelite.png build/linux-aarch64/

tar zxf linux_aarch64_jre.tar.gz
mv jdk-$LINUX_AMD64_VERSION-jre build/linux-aarch64/jre

pushd build/linux-aarch64
mkdir -p jre/lib/amd64/server/
ln -s ../../server/libjvm.so jre/lib/amd64/server/ # packr looks for libjvm at this hardcoded path

# Symlink AppRun -> RuneLite
ln -s RuneLite AppRun

# Ensure RuneLite is executable to all users
chmod 755 RuneLite
popd

if ! [ -f appimagetool-x86_64.AppImage ] ; then
    curl -Lo appimagetool-x86_64.AppImage \
        https://github.com/AppImage/AppImageKit/releases/download/$APPIMAGE_VERSION/appimagetool-x86_64.AppImage
    chmod +x appimagetool-x86_64.AppImage
fi

echo "df3baf5ca5facbecfc2f3fa6713c29ab9cefa8fd8c1eac5d283b79cab33e4acb  appimagetool-x86_64.AppImage" | sha256sum -c

if ! [ -f runtime-aarch64 ] ; then
    curl -Lo runtime-aarch64 \
	    https://github.com/AppImage/AppImageKit/releases/download/$APPIMAGE_VERSION/runtime-aarch64
fi

echo "d2624ce8cc2c64ef76ba986166ad67f07110cdbf85112ace4f91611bc634c96a  runtime-aarch64" | sha256sum -c

ARCH=arm_aarch64 ./appimagetool-x86_64.AppImage \
	--runtime-file runtime-aarch64  \
	build/linux-aarch64/ \
	RuneLite-aarch64.AppImage
