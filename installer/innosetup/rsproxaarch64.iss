[Setup]
AppName=RSProx Launcher
AppPublisher=RSProx
UninstallDisplayName=RSProx
AppVersion=1.0.0
AppSupportURL=https://rsprox.net/
DefaultDirName={localappdata}\RSProx

; ~30 mb for the repo the launcher downloads
ExtraDiskSpaceRequired=30000000
ArchitecturesAllowed=arm64
PrivilegesRequired=lowest

WizardSmallImageFile=rsprox_small.bmp
SetupIconFile=rsprox.ico
UninstallDisplayIcon={app}\RSProx.exe

Compression=lzma2
SolidCompression=yes

OutputDir=.
OutputBaseFilename=RSProxSetupAArch64

[Tasks]
Name: DesktopIcon; Description: "Create a &desktop icon";

[Files]
Source: "..\build\win-aarch64\RSProx.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\build\win-aarch64\rsprox-launcher.jar"; DestDir: "{app}"
Source: "..\build\win-aarch64\launcher_aarch64.dll"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\build\win-aarch64\config.json"; DestDir: "{app}"
Source: "..\build\win-aarch64\jdk\*"; DestDir: "{app}\jdk"; Flags: recursesubdirs

[Icons]
; start menu
Name: "{userprograms}\RSProx\RSProx"; Filename: "{app}\RSProx.exe"
Name: "{userdesktop}\RSProx"; Filename: "{app}\RSProx.exe"; Tasks: DesktopIcon

[Run]
Filename: "{app}\RSProx.exe"; Description: "&Open RSProx"; Flags: postinstall skipifsilent nowait

[InstallDelete]
; Delete the old jvm so it doesn't try to load old stuff with the new vm and crash
Type: filesandordirs; Name: "{app}\jdk"
; previous shortcut
Type: files; Name: "{userprograms}\RSProx.lnk"

[UninstallDelete]
Type: filesandordirs; Name: "{%USERPROFILE}\.rsprox\caches"
Type: filesandordirs; Name: "{%USERPROFILE}\.rsprox\clients"
Type: filesandordirs; Name: "{%USERPROFILE}\.rsprox\launcher"
Type: filesandordirs; Name: "{%USERPROFILE}\.rsprox\runelite"
Type: filesandordirs; Name: "{%USERPROFILE}\.rsprox\runelite-launcher"
Type: filesandordirs; Name: "{%USERPROFILE}\.rsprox\signkey"
Type: filesandordirs; Name: "{%USERPROFILE}\.rsprox\sockets"
Type: filesandordirs; Name: "{%USERPROFILE}\.rsprox\key.rsa"
; includes install_id, settings, etc
Type: filesandordirs; Name: "{app}"

[Code]
#include "upgrade.pas"
#include "usernamecheck.pas"
#include "dircheck.pas"
