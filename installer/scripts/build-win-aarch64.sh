#!/bin/bash

set -e

cmake -S liblauncher -B liblauncher/buildaarch64 -A ARM64
cmake --build liblauncher/buildaarch64 --config Release

pushd native
cmake -B build-aarch64 -A ARM64
cmake --build build-aarch64 --config Release
popd

source .jdk-versions.sh

rm -rf build/win-aarch64
mkdir -p build/win-aarch64

if ! [ -f win-aarch64_jre.zip ] ; then
    curl -Lo win-aarch64_jre.zip $WIN_AARCH64_LINK
fi

echo "$WIN_AARCH64_CHKSUM win-aarch64_jre.zip" | sha256sum -c

unzip win-aarch64_jre.zip
jlink \
  --compress 2 \
  --strip-debug \
  --no-header-files \
  --no-man-pages \
  --output build/win-aarch64/jre \
  --module-path jdk-$WIN_AARCH64_VERSION/jmods \
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

cp native/build-aarch64/src/Release/RuneLite.exe build/win-aarch64/
cp target/RuneLite.jar build/win-aarch64/
cp packr/win-aarch64-config.json build/win-aarch64/config.json
cp liblauncher/buildaarch64/Release/launcher_aarch64.dll build/win-aarch64/

echo RuneLite.exe aarch64 sha256sum
sha256sum build/win-aarch64/RuneLite.exe

dumpbin //HEADERS build/win-aarch64/RuneLite.exe

# We use the filtered iss file
iscc target/filtered-resources/runeliteaarch64.iss