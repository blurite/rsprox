#!/bin/bash

set -e

pushd installer/native
cmake -B build-x64 .
cmake --build build-x64 --config Release
popd

APPIMAGE_VERSION="13"

umask 022

source installer/scripts/.jdk-versions.sh

rm -rf build/linux-x64
mkdir -p build/linux-x64

if ! [ -f linux64_jdk.tar.gz ] ; then
    curl -Lo linux64_jdk.tar.gz $LINUX_AMD64_LINK
fi

echo "$LINUX_AMD64_CHKSUM linux64_jdk.tar.gz" | sha256sum -c

# Note: Host umask may have checked out this directory with g/o permissions blank
chmod -R u=rwX,go=rX installer/appimage
# ...ditto for the build process
chmod 644 rsprox-launcher.jar

cp installer/native/build-x64/src/RSProx build/linux-x64/
cp rsprox-launcher.jar build/linux-x64/
cp installer/packr/linux-x64-config.json build/linux-x64/config.json
cp installer/appimage/rsprox.desktop build/linux-x64/
cp installer/appimage/rsprox.png build/linux-x64/

tar zxf linux64_jdk.tar.gz
jlink \
  --compress 2 \
  --strip-debug \
  --no-header-files \
  --no-man-pages \
  --output build/linux-x64/jdk \
  --module-path jdk-$LINUX_AMD64_VERSION/jmods \
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

pushd build/linux-x64/
mkdir -p jdk/lib/amd64/server/
ln -s ../../server/libjvm.so jdk/lib/amd64/server/ # packr looks for libjvm at this hardcoded path

# Symlink AppRun -> RSProx
ln -s RSProx AppRun

# Ensure RSProx is executable to all users
chmod 755 RSProx
popd

if ! [ -f appimagetool-x86_64.AppImage ] ; then
    curl -Lo appimagetool-x86_64.AppImage \
        https://github.com/AppImage/AppImageKit/releases/download/$APPIMAGE_VERSION/appimagetool-x86_64.AppImage
    chmod +x appimagetool-x86_64.AppImage
fi

echo "df3baf5ca5facbecfc2f3fa6713c29ab9cefa8fd8c1eac5d283b79cab33e4acb  appimagetool-x86_64.AppImage" | sha256sum -c

./appimagetool-x86_64.AppImage \
	build/linux-x64/ \
	RSProx.AppImage
