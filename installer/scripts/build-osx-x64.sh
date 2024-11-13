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
    jlink \
			--compress 2 \
			--strip-debug \
			--no-header-files \
			--no-man-pages \
			--output $APPBASE/Contents/Resources/jdk \
			--module-path jdk-$MAC_AMD64_VERSION/Contents/Home/jmods \
			--add-modules java.base \
			--add-modules java.compiler \
			--add-modules java.datatransfer \
			--add-modules java.desktop \
			--add-modules java.instrument \
			--add-modules java.logging \
			--add-modules java.management \
			--add-modules java.management.rmi \
			--add-modules java.naming \
			--add-modules java.net.http \
			--add-modules java.prefs \
			--add-modules java.rmi \
			--add-modules java.scripting \
			--add-modules java.se \
			--add-modules java.security.jgss \
			--add-modules java.security.sasl \
			--add-modules java.smartcardio \
			--add-modules java.sql \
			--add-modules java.sql.rowset \
			--add-modules java.transaction.xa \
			--add-modules java.xml \
			--add-modules java.xml.crypto \
			--add-modules jdk.accessibility \
			--add-modules jdk.charsets \
			--add-modules jdk.crypto.cryptoki \
			--add-modules jdk.crypto.ec \
			--add-modules jdk.dynalink \
			--add-modules jdk.httpserver \
			--add-modules jdk.internal.ed \
			--add-modules jdk.internal.le \
			--add-modules jdk.jdwp.agent \
			--add-modules jdk.jfr \
			--add-modules jdk.jsobject \
			--add-modules jdk.localedata \
			--add-modules jdk.management \
			--add-modules jdk.management.agent \
			--add-modules jdk.management.jfr \
			--add-modules jdk.naming.dns \
			--add-modules jdk.naming.ldap \
			--add-modules jdk.naming.rmi \
			--add-modules jdk.net \
			--add-modules jdk.pack \
			--add-modules jdk.scripting.nashorn \
			--add-modules jdk.scripting.nashorn.shell \
			--add-modules jdk.sctp \
			--add-modules jdk.security.auth \
			--add-modules jdk.security.jgss \
			--add-modules jdk.unsupported \
			--add-modules jdk.xml.dom \
			--add-modules jdk.zipfs


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
