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
jlink \
  --compress 2 \
  --strip-debug \
  --no-header-files \
  --no-man-pages \
  --output installer/build/win-x64/jdk \
  --module-path jdk-$WIN64_VERSION/jmods \
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
  --add-modules jdk.security.auth \
  --add-modules jdk.security.jgss \
  --add-modules jdk.unsupported \
  --add-modules jdk.xml.dom \
  --add-modules jdk.zipfs

echo RSProx.exe 64bit sha256sum
sha256sum installer/build/win-x64/RSProx.exe

dumpbin //HEADERS installer/build/win-x64/RSProx.exe

# We use the filtered iss file
iscc installer/innosetup/rsprox.iss
