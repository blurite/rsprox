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
jlink \
  --compress 2 \
  --strip-debug \
  --no-header-files \
  --no-man-pages \
  --output installer/build/win-x86/jdk \
  --module-path jdk-$WIN32_VERSION/jmods \
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
  --add-modules jdk.crypto.mscapi \
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
  --add-modules jdk.security \
  --add-modules jdk.unsupported \
  --add-modules jdk.xml.dom \
  --add-modules jdk.zipfs

echo RSProx.exe 32bit sha256sum
sha256sum installer/build/win-x86/RSProx.exe

dumpbin //HEADERS installer/build/win-x86/RSProx.exe

# We use the filtered iss file
iscc installer/innosetup/rsprox32.iss
