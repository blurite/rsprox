#!/bin/bash

set -e

APPBASE="installer/build/macos-x64/RSProx.app"

build() {
    pushd installer/native
    cmake -DCMAKE_OSX_ARCHITECTURES=x86_64 -B build-x64 .
    cmake --build build-x64 --config Release
    popd

    source installer/scripts/.jdk-versions.sh

    rm -rf installer/build/macos-x64
    mkdir -p installer/build/macos-x64

    if ! [ -f mac64_jdk.tar.gz ] ; then
        curl -Lo mac64_jdk.tar.gz $MAC_AMD64_LINK
    fi

    echo "$MAC_AMD64_CHKSUM  mac64_jdk.tar.gz" | shasum -c

    mkdir -p $APPBASE/Contents/{MacOS,Resources}

    cp installer/native/build-x64/src/RSProx $APPBASE/Contents/MacOS/
    cp rsprox-launcher.jar $APPBASE/Contents/Resources/
    cp installer/packr/macos-x64-config.json $APPBASE/Contents/Resources/config.json
    cp installer/osx/Info.plist $APPBASE/Contents/
    cp installer/osx/rsprox.icns $APPBASE/Contents/Resources/icons.icns

    tar zxf mac64_jdk.tar.gz
    mkdir $APPBASE/Contents/Resources/jdk
    mv jdk-$MAC_AMD64_VERSION/Contents/Home/* $APPBASE/Contents/Resources/jdk

    echo Setting world execute permissions on RSProx
    pushd $APPBASE
    chmod g+x,o+x Contents/MacOS/RSProx
    popd

    otool -l $APPBASE/Contents/MacOS/RSProx
}

dmg() {
    codesign --force --deep --sign - "$APPBASE"
    codesign --verify --deep --strict --verbose=2 "$APPBASE"
    hdiutil create -volname RSProx -srcfolder "$APPBASE" -ov -format UDZO RSProx-x64.dmg
    hdiutil verify RSProx-x64.dmg
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
