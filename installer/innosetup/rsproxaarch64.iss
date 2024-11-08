[Setup]
AppName=RSProx Launcher
AppPublisher=RSProx
UninstallDisplayName=RSProx
AppVersion=${project.version}
AppSupportURL=https://rsprox.net/
DefaultDirName={localappdata}\RSProx

; ~30 mb for the repo the launcher downloads
ExtraDiskSpaceRequired=30000000
ArchitecturesAllowed=arm64
PrivilegesRequired=lowest

WizardSmallImageFile=${basedir}/innosetup/rsprox_small.bmp
SetupIconFile=${basedir}/innosetup/rsprox.ico
UninstallDisplayIcon={app}\RSProx.exe

Compression=lzma2
SolidCompression=yes

OutputDir=${basedir}
OutputBaseFilename=RSProxSetupAArch64

[Tasks]
Name: DesktopIcon; Description: "Create a &desktop icon";

[Files]
Source: "${basedir}\build\win-aarch64\RSProx.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "${basedir}\build\win-aarch64\rsprox-launcher.jar"; DestDir: "{app}"
Source: "${basedir}\build\win-aarch64\launcher_aarch64.dll"; DestDir: "{app}"; Flags: ignoreversion
Source: "${basedir}\build\win-aarch64\config.json"; DestDir: "{app}"
Source: "${basedir}\build\win-aarch64\jre\*"; DestDir: "{app}\jre"; Flags: recursesubdirs

[Icons]
; start menu
Name: "{userprograms}\RSProx\RSProx"; Filename: "{app}\RSProx.exe"
Name: "{userprograms}\RSProx\RSProx (configure)"; Filename: "{app}\RSProx.exe"; Parameters: "--configure"
Name: "{userprograms}\RSProx\RSProx (safe mode)"; Filename: "{app}\RSProx.exe"; Parameters: "--safe-mode"
Name: "{userdesktop}\RSProx"; Filename: "{app}\RSProx.exe"; Tasks: DesktopIcon

[Run]
Filename: "{app}\RSProx.exe"; Parameters: "--postinstall"; Flags: nowait
Filename: "{app}\RSProx.exe"; Description: "&Open RSProx"; Flags: postinstall skipifsilent nowait

[InstallDelete]
; Delete the old jvm so it doesn't try to load old stuff with the new vm and crash
Type: filesandordirs; Name: "{app}\jre"
; previous shortcut
Type: files; Name: "{userprograms}\RSProx.lnk"

[UninstallDelete]
Type: filesandordirs; Name: "{%USERPROFILE}\.rsprox"
; includes install_id, settings, etc
Type: filesandordirs; Name: "{app}"

[Code]
#include "upgrade.pas"
#include "usernamecheck.pas"
#include "dircheck.pas"
