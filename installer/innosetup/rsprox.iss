[Setup]
AppName=RSProx Launcher
AppPublisher=RSProx
UninstallDisplayName=RSProx
AppVersion=1.0.0
AppSupportURL=https://rsprox.net/
DefaultDirName={localappdata}\RSProx

; ~30 mb for the repo the launcher downloads
ExtraDiskSpaceRequired=30000000
ArchitecturesAllowed=x64
PrivilegesRequired=lowest

WizardSmallImageFile=installer/innosetup/rsprox_small.bmp
SetupIconFile=installer/innosetup/rsprox.ico
UninstallDisplayIcon={app}\RSProx.exe

Compression=lzma2
SolidCompression=yes

OutputDir=installer
OutputBaseFilename=RSProxSetup

[Tasks]
Name: DesktopIcon; Description: "Create a &desktop icon";

[Files]
Source: "installer\build\win-x64\RSProx.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "installer\build\win-x64\rsprox-launcher.jar"; DestDir: "{app}"
Source: "installer\build\win-x64\launcher_amd64.dll"; DestDir: "{app}"; Flags: ignoreversion
Source: "installer\build\win-x64\config.json"; DestDir: "{app}"
Source: "installer\build\win-x64\jre\*"; DestDir: "{app}\jre"; Flags: recursesubdirs

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
