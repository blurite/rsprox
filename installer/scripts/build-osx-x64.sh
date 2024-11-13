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
    SIGNING_IDENTITY="Developer ID Application"
    codesign -f -s "${SIGNING_IDENTITY}" --entitlements installer/osx/signing.entitlements --options runtime $APPBASE || true

    # create-dmg exits with an error code due to no code signing, but is still okay
    # note we use Adam-/create-dmg as upstream does not support UDBZ
    create-dmg --format UDBZ $APPBASE . || true
    mv rsprox\ *.dmg RSProx-x64.dmg

    # dump for CI
    hdiutil imageinfo RSProx-x64.dmg

    if ! hdiutil imageinfo RSProx-x64.dmg | grep -q "Format: UDBZ" ; then
        echo "Format of resulting dmg was not UDBZ, make sure your create-dmg has support for --format"
        exit 1
    fi

    if ! hdiutil imageinfo RSProx-x64.dmg | grep -q "Apple_HFS" ; then
        echo Filesystem of dmg is not Apple_HFS
        exit 1
    fi

    # Notarize app
    if xcrun notarytool submit RSProx-x64.dmg --wait --keychain-profile "AC_PASSWORD" ; then
        xcrun stapler staple RSProx-x64.dmg
    fi
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
