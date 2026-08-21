#!/bin/bash

set -e

APPBASE="installer/build/macos-aarch64/RSProx.app"

build() {
    pushd installer/native
    cmake -DCMAKE_OSX_ARCHITECTURES=arm64 -B build-aarch64 .
    cmake --build build-aarch64 --config Release
    popd

    source installer/scripts/.jdk-versions.sh

    rm -rf installer/build/macos-aarch64
    mkdir -p installer/build/macos-aarch64

    if ! [ -f mac_aarch64_jdk.tar.gz ] ; then
        curl -Lo mac_aarch64_jdk.tar.gz $MAC_AARCH64_LINK
    fi

    echo "$MAC_AARCH64_CHKSUM  mac_aarch64_jdk.tar.gz" | shasum -c

    mkdir -p $APPBASE/Contents/{MacOS,Resources}

    cp installer/native/build-aarch64/src/RSProx $APPBASE/Contents/MacOS/
    cp rsprox-launcher.jar $APPBASE/Contents/Resources/
    cp installer/packr/macos-aarch64-config.json $APPBASE/Contents/Resources/config.json
    cp installer/osx/Info.plist $APPBASE/Contents/
    cp installer/osx/rsprox.icns $APPBASE/Contents/Resources/icons.icns

    tar zxf mac_aarch64_jdk.tar.gz
    mkdir $APPBASE/Contents/Resources/jdk
    mv jdk-$MAC_AARCH64_VERSION/Contents/Home/* $APPBASE/Contents/Resources/jdk

    echo Setting world execute permissions on RSProx
    pushd $APPBASE
    chmod g+x,o+x Contents/MacOS/RSProx
    popd

    otool -l $APPBASE/Contents/MacOS/RSProx
}

dmg() {
    codesign --force --deep --sign - "$APPBASE"
    codesign --verify --deep --strict --verbose=2 "$APPBASE"
    hdiutil create -volname RSProx -srcfolder "$APPBASE" -ov -format UDZO RSProx-aarch64.dmg
    hdiutil verify RSProx-aarch64.dmg
}

while test $# -gt 0; do
  case "$1" in
    --build)
      build
      shift
      ;;
    --dmg)
      dmg
      shift
      ;;
    *)
      break
      ;;
  esac
done
