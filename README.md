# RSProx

[![GitHub Actions][actions-badge]][actions] [![MIT license][mit-badge]][mit]

## Introduction

RSProx is a locally hosted proxy for inspecting traffic between game clients and servers in
Old School RuneScape and RuneScape 3. It provides live packet transcripts and binary recordings
that can be transcribed later.

RSProx patches the client to connect through the local proxy, which forwards traffic to the
intended game servers. Old School RuneScape supports RuneLite and the Windows-native client.
RuneScape 3 support is experimental and currently targets revision 950's Windows-native client.
See [RuneScape 3](#runescape-3-experimental) for its features and limitations.

A technical breakdown of the original OSRS implementation can be found in
[issues/RSProx](https://github.com/blurite/rsprox/issues/1).

## Installer

> [!TIP]
> Use the installer for better compatibility and ease of use.

The installer for RSProx can be found [under releases](https://github.com/blurite/rsprox/releases).

The installer comes with a bundled JDK which it utilizes. The launcher auto-updates
RSProx whenever a new version is published.

## Guide

Below is a quick guide demonstrating how to use RSProx.

### Building from Source

Clone the repository to develop RSProx or try changes that have not yet been released.
For published versions, the installer is also available as described above.

### Launching

From a source checkout, run `net.rsprox.gui.ProxyToolGuiKt` in the `gui/proxy-tool` module,
or launch it through Gradle with `./gradlew proxy` (`.\gradlew.bat proxy` on Windows).

> [!NOTE]
> The OSRS Windows-native client can run on Windows and Linux using Wine or Proton.
> Native macOS patching is not currently supported. RS3 has been verified on Windows;
> RS3 support on Linux/Wine/Proton has not been verified, and macOS is not supported.

### Usage

In **New Session**, select an **Account** and **Client Type**, then press **Launch Session**.
For OSRS, **Native** selects the Windows-native client and **RuneLite** selects RuneLite.
For RS3, select **RuneScape 3**. OSRS also allows selecting a **Proxy Target**;
for RS3, that selector shows **RuneScape 3** and is disabled.

Select a linked Jagex Account character to launch with that character. **Default** uses the
normal login flow; OSRS RuneLite can also load exported credentials for the official target.
See [Jagex Accounts](#jagex-accounts).

> [!CAUTION]
> Using OSRS RuneLite on macOS requires whitelisting certain loopback addresses.
> See [macOS Support (OSRS)](#macos-support-osrs) below. This does not enable RS3 on macOS.

The screenshots below show an older GUI layout; use the named controls above in the current UI.

![Launching client](https://media.z-kris.com/2024/10/javaw_Zdj10a5jq8.png)

After launching a client, a process will occur which will download the necessary
files and patch them, so they can pass data through the proxy itself.
On RuneLite, this can take tens of seconds as everything loads up. Caching
mechanism is included on RuneLite that makes consecutive launches faster than
the first one (or whenever the cache is invalidated).

Once the client has fully booted up, you may log in. Game traffic flows through the proxy,
with recordings and transcripts stored locally. RSProx also makes external requests for
login, game connections, updates and supporting data; OSRS map XTEA keys are submitted to
OpenRS2. See [Security and Privacy](#security-and-privacy) for the distinction between
forwarded traffic, local recordings and external requests.
Upon logging in, you should be met with logs being written in your RSProx GUI
as depicted here:

![Active client](https://media.z-kris.com/2024/10/javaw_WGRZzk1wtT.png)

In order to filter the data that gets logged, you may use the filters panel
that is at the top-right section of the client, as seen depicted here:

![Client filters](https://media.z-kris.com/2024/10/javaw_7ScB7aJLHn.png)

Within the filters, you can toggle individual packets, entire packet groups,
or even the full `Incoming` or `Outgoing` categories, by right-clicking on said
categories.

Additionally, on-top of the regular filters, one can use the settings above it
to toggle general-purpose preferences that apply regardless of the selected
filter preset.

> [!TIP]
> The Default preset does not support modifications. If you wish to toggle
> filters, you must make a filter preset of your choice.
> You can have an unlimited amount of filter presets, and they are saved on
> your PC.

### RuneScape 3 (Experimental)

> [!WARNING]
> RuneScape 3 support is new and experimental. There may be bugs, and sensitive-information
> removal may be incomplete. Recordings, transcripts and the GUI may still contain sensitive
> information. Review captures before sharing them. Use at your own risk.

The first RS3 launch displays this warning. Choosing **I understand — Continue** saves the
acknowledgement across restarts; cancelling or closing the dialog stops the launch.

#### Supported Features

- Revision 950's Windows-native client, with Windows as the verified environment.
- Automatic client downloading and patching, with support for linked Jagex Account characters.
- Lobby and game traffic inspection in both directions, including player/NPC information and zone updates.
- Lobby-to-game transitions, returning to the lobby, world hopping and reconnects.
- Multiple client instances, each with its own local proxy ports.
- Live GUI transcripts, binary recording and saved-file transcription.
- Cache-definition loading and gameval/clientscript names for richer transcripts.

RS3 currently connects to the official game. Custom proxy targets are OSRS-only.
On Windows, RSProx uses a private copy of the official RuneScape launcher for both
OpenGL and Vulkan. It extracts a verified launcher from Jagex's installer without
running the installer or changing an existing RuneScape installation. Each running
instance has separate launcher preferences and local proxy ports; game-cache assets
are shared. Launcher downloads are cached under `~/.rsprox/rs3-launcher/`.

The launcher downloads the live client and checks the supported revision; it is not a general
launcher for historical RS3 revisions. Later revisions require updated patching and decoders.
**RS3 replay is not implemented**, even though recording and transcription are supported.

#### Recordings and Lobby Visibility

RS3 recordings are saved under `~/.rsprox/binary/RuneScape 3/`
(`%USERPROFILE%\.rsprox\binary\RuneScape 3\` on Windows), alongside the OSRS recording folder.
Filenames use a timestamp and a short account-hash suffix, with collision handling for rapid
successive sessions. This suffix is an identifier, not a guarantee of anonymity.

A recording is created when a game login succeeds and contains the captured lobby context
followed by that game session. Lobby-only activity does not create a standalone recording.
World hopping starts a new recording and copies the preceding lobby context into it, with
an updated `LOBBY_TRANSFER` marker for the new world. Returning to the lobby supplies a new
lobby baseline for the next successful game login. Reconnects remain in the existing recording.

Under **Logging → Miscellaneous**:

- **Hide RS3 Lobby** hides lobby packets in live and saved transcripts without removing
  lobby data from the binary recording.
- **Skip First Tick** skips the first game tick's transcript output, not the lobby's.
  It works together with **Hide RS3 Lobby**.

### macOS Support (OSRS)

These instructions apply to OSRS, not RS3.

MacOS does not whitelist any loopback address other than 127.0.0.1 by default,
which means RSProx cannot establish a connection, as we use unique
loopback addresses per connection established, which describes the world to
which we're connecting.

As such, it is necessary for anyone connecting via MacOS to run some commands.
In order to whitelist the necessary loopback addresses, this script must be run:
(Note that for custom private server targets, the group id must be changed from
2 to 3+, where 3 is the first custom target, 4 is the second and so on)

> [!WARNING]
> Whitelisting a lot of worlds will result in DNS lookups significantly slowing
> down. It is recommended you only select your preferred worlds and whitelist
> those specific ones. Ensure that your default world is configured and
> whitelisted, or the client will not be able to boot up.

```bash
#!/bin/bash
set -euo pipefail

# === Config ====================================================
MIN_WORLD_ID=300      # Minimum world id to whitelist, inclusive.
MAX_WORLD_ID=650      # Maximum world id to whitelist, inclusive.
GROUP_ID=2            # Proxy target (2 is Oldschool, 3 is first custom, etc).
MODE=+                # "+" to whitelist, "-" to un-whitelist
# ===============================================================

for ((w=MIN_WORLD_ID; w<=MAX_WORLD_ID; w++)); do
  a=$(( w / 256 ))
  b=$(( w % 256 ))
  c=$GROUP_ID
  ip="127.$a.$b.$c"

  if [[ "$MODE" == "+" ]]; then
    sudo ifconfig lo0 alias "$ip"
  else
    sudo ifconfig lo0 -alias "$ip"
  fi
done

echo "Alias IPs added for worlds $MIN_WORLD_ID..$MAX_WORLD_ID (group $GROUP_ID)."
```

### Transcribing

Besides live GUI transcripts, previously recorded `.bin` files can be transcribed into
`.txt` files beside the originals, using the same base filename. Drag recordings onto the GUI
to begin. A single OSRS recording offers a choice of replay or transcription; an RS3 recording
goes directly to transcription because RS3 replay is not available.

Transcription uses the currently active filters and settings, including the RS3 lobby/first-tick
settings above. These output filters do not remove packet data from the underlying recording.

![Example](https://media.z-kris.com/2025/08/java_ywUBskAkZ4.gif)

### Jagex Accounts

Linked Jagex Account characters can be used with both OSRS and RS3. For OSRS RuneLite on the
official target, credential export is also available as an alternative to linking an account.
Use a linked character for Jagex Account login on RS3; its launcher does not import RuneLite's
`credentials.properties` file.

#### Linking a Jagex Account

To link a Jagex Account, open the **Account** dropdown and select **Manage Linked Accounts**.

![Example](https://media.z-kris.com/2025/11/java_vhIgMxwunA.png)

Afterward, a popup will show where you can add or delete existing Jagex Accounts.
In order to add one, hit the `+` button. This should launch a new browser window
to `account.jagex.com`, asking you to login. Complete the login, and you should
see a window stating "Account Linked Successfully". You may close the browser
window, and you should then see all your characters show up in the drop-down.
Selecting a linked character launches the client using that character's credentials.
**Default** uses the normal login flow, with the OSRS RuneLite exception described below.

Extra notes:

- Linking a Jagex Account requires port 80 to be free, as it binds to it temporarily
for the linking process. This is how the data is sent from your browser back to
RSProx, allowing it to store the Jagex Account Token. On Linux, this requires
Sudo access, as ports < 1000 are protected.
- The Jagex accounts will refresh themselves every time you boot up RSProx,
ensuring you never have to run the setup process again.

If the process fails for you, please let us know so we can figure out a solution
that will avoid it becoming an issue for other people in the future too.


#### Exporting RuneLite Credentials (OSRS RuneLite)

The second option to using a Jagex Account is by exporting the short-term token
via RuneLite, as explained [here](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts).
Once you have exported the credentials.properties as shown in the tutorial,
RSProx loads them from `user.home/.runelite/credentials.properties` when launching OSRS RuneLite
with the official proxy target and the **Default** account selection.
If you wish to stop using a Jagex Account in this Default mode,
simply delete the credentials file. In this Default mode, you may only have
one character/account, as it always reads from the same file when launching
the client.

#### Linux Setup (OSRS)

On Linux, there are a few extra steps involved in setting up a Jagex Account:

1. Run `RSProx.AppImage` normally to generate the required folders.
2. Afterward, you must run `RSProx.AppImage` with `sudo`, this is because of the Jagex Account login page only allowing redirects to `localhost:80`, which is a protected port by default on Linux, after this you can setup and import your Jagex Account via the GUI.
3. Once the above is done, you must copy the `jagex-accounts.properties` file from `/root/.rsprox/` to `~/.rsprox/` (i.e `sudo cp /root/.rsprox/jagex-accounts.properties ~/.rsprox/`) as RSProx puts its config folder in the user's home folder by default, which is different when ran as root.
4. You must then make yourself the owner of the file, this is done via `sudo chown "$USER" ~/.rsprox/jagex-accounts.properties`.
5. You can now simply run `RSProx.AppImage` and you will be able to access your Jagex Account.

Additionally, if you would like to run the Native Client under Proton (at the time of writing this is currently necessary to use the new renderer), you must create a `protonpath` file in `~/.rsprox/` containing the file path to a proton executable, for example:
```
/home/grian/.steam/steam/steamapps/common/Proton - Experimental/proton
```

### Security and Privacy

We have taken many measures to let players use RSProx securely and protect the
information captured in their recordings.

#### So, is it safe to use?

We are confident in the safety of the established OSRS implementation, which has
been extensively tested. Native clients receive only the small changes needed
for proxying; RSProx is a traffic-inspection tool, not a gameplay automation tool.

RuneLite requires more changes than the Native client. We are confident in the
current implementation, but future client updates can introduce new checks or
change behaviour, so compatibility needs to be maintained as the game evolves.

RS3 follows the same approach and includes recording privacy protections too.
It is marked experimental because the integration is new and has had less
testing than OSRS, not because it is known to be unsafe. We expect it to be safe
to use, while continuing to verify compatibility and sensitive-packet coverage.

#### Recording Privacy

Sensitive fields are erased from both binary recordings and GUI transcripts.
Privacy scrubbing applies only to the recorded copies: the original packet
payloads forwarded to the game client or server are left unchanged.

The protections include private-message contents, login tokens in URL packets,
the 192-bit UID, site settings, keyboard key values and bank-PIN actions. RS3
also scrubs selected account-data fields, including dates of birth and
friend/ignore-list notes. These protections are maintained for each supported
revision.

> [!NOTE]
> RS3's sensitive-packet coverage is still being verified, so review its recordings
> before sharing them. Player names and ordinary gameplay information remain in
> recordings for analysis. Transcript filters control what is displayed; they do
> not erase packets from the binary.

#### External Connections

Recordings and transcripts are stored locally. RSProx connects to game and
authentication servers, downloads clients and updates, and retrieves supporting
cache data and clientscript signatures. Live OSRS sessions also contribute map
XTEA keys to OpenRS2; this does not upload your recordings or transcripts.

### Private Server Usage (OSRS)

This section applies to OSRS only; RS3 custom proxy targets are not implemented.
RSProx can currently be used to connect to OSRS private servers, but only under
certain circumstances. The following criteria must be met in order to do this:

> [!NOTE]
> This list is subject to changes over time, we hope to improve the overall
> support for further platforms and client types.

1. This only works with Windows and Linux, not macOS.
2. The client must not have any protocol-breaking changes, same traditional
networking must be used. The only supported change at this time is changing
the varp count in the client from the size-5000 int array.
3. Must be on revision 223 or higher.

#### Setting Up Custom Targets

In order to use the new proxy targets feature, one has to provide a yaml file containing them.
The file is expected at `user.home/.rsprox/proxy-targets.yaml` (.yml suffix also supported).
The file does not exist by default, so it must be created by the user!
You can either create or edit this file manually, or import a ready-made configuration
directly from the RSProx GUI by pressing the **+** button next to the proxy target selector
and selecting a local `.yaml`/`.yml` file. The importer can also download a configuration
hosted on the internet—choose **From URL…** in the dialog and paste a direct link to the YAML file.
Make sure the link points at the *raw* contents of the YAML file (for example, the "Raw" view of a
GitHub Gist). The importer will copy the configuration into the user folder for you; restart RSProx
after importing to load the new targets.

If you need a starting point, copy [`docs/examples/proxy-targets.sample.yaml`](docs/examples/proxy-targets.sample.yaml)
and adjust the values for your own server. This is the same structure that the importer expects when
you supply a URL.

The YAML format expects one object per target. When the file is stored locally it will be written with a top-level `config` array, but the importer will also accept a bare list when downloading from a URL. Here is an example containing two entries:
```yaml
config:
  - name: Blurite
    jav_config_url: https://client.blurite.io/jav_local_226.ws
    revision: 236.1
    modulus: d2a780dccbcf534dc61a36deff725aabf9f46fc9ea298ac8c39b89b5bcb5d0817f8c9f59621187d448da9949aca848d0b2acae50c3122b7da53a79e6fe87ff76b675bcbf5bc18fbd2c9ed8f4cff2b7140508049eb119259af888eb9d20e8cea8a4384b06589483bcda11affd8d67756bc93a4d786494cdf7b634e3228b64116d
  - name: Local Test World
    jav_config_url: https://example.com/jav_config.ws
    modulus: 9fca1d4f3eaa48d1ac6b189b5c9a88c6fbf7f8b2b5b9f4923c9c088da6cb34c1f02f55d30f9ec0e25f20fba8eea741e2cb9408c4de61b935f1c36b3b21b7aa15
    game_server_port: 50000
```

If you are hosting the configuration yourself on a url and prefer a shorter document, the importer also understands the following equivalent structure:

```yaml
- name: Blurite
  jav_config_url: https://client.blurite.io/jav_local_236.ws
  revision: 236.1
```

Properties breakdown:

- `name` - The name given to the client. Any references to `OldSchool RuneScape` will be replaced by this. This is a required property to ensure caches don't overwrite and cause crashing at runtime when loading different games simultaneously.
- `jav_config_url` - The URL to the jav_config that will be used to load initial world and world list. This is a required property.
- `modulus` - A hexadecimal (base-16) RSA modulus used to encrypt the login packet sent to the client. This is a required property.
- `revision` – A revision number used to pick the client and correct decoders. The default is the latest stable Old School RuneScape version. For Native clients, if no subrevision is provided (e.g. `234`), `.1` is automatically appended (becoming `234.1`). See: [https://archive.lostcity.rs/oldschool.runescape.com/native/osrs-win/](https://archive.lostcity.rs/oldschool.runescape.com/native/osrs-win/). This is an optional property.
- `varp_count` - (OPTIONAL) Changes the array length used for varps in the client, the default value is 5000. This is an optional property. As of revision 232, it is no longer patched as the value is now based on cache.
- `runelite_bootstrap_commithash` - (OPTIONAL) A hash pointing to a version of RuneLite you wish to use. This is an optional property, not defining it will use the latest for your revision.
- `runelite_bootstrap_url` (OPTIONAL) Set the URL to a custom bootstrap entirely. This takes precedence over the commit hash one above.
- `runelite_gamepack_url` - (OPTIONAL) A URL pointing to a valid gamepack file for the given revision. This is only necessary for revisions 228 and older. Not defining it will use the latest for your revision.
- `binary_folder` - (OPTIONAL) The subdirectory name within `.rsprox/binary/` into which the files should be written. If omitted, the `name` of the Proxy Target is used instead.
- `export_binaries` (OPTIONAL) A boolean for whether to write .bin files for this proxy target. Default is true. If set to false, no files are ever written to disk for the given target.
- `game_server_port` (OPTIONAL) Set your server port if it not using 43594.

If you are using an external ip to connect to your server, make sure you have changed your world_list.ws also to the right target.


Short guide:
1. Copy the config file from above and save it as described above.
2. Change the name to what you'd like to call the client.
3. Change the number `227` in the jav_config_url to the revision you're using.
I maintain local javconfigs from revision 223 onwards. You can also host your own, but this will easily get you started.
4. Change the revision number `227.3` to match with the revision you're using.
The list of valid revisions can be seen [here](https://archive.lostcity.rs/oldschool.runescape.com/native/osrs-win/). A safe bet is using `.1` subrevision as that exists in all revisions.
5. Change the modulus to what your server is using. In the case of RSMod, this is exported during the installation.
6. If you wish to use RuneLite and your server is older than the very latest revision, you'll need to update the commithash for RuneLite that indicates the version of RuneLite to use.
The list of valid bootstraps can be seen [here](https://github.com/runelite/static.runelite.net/commits/gh-pages/bootstrap.json).
You'll have to locate the correct bootstrap based on the date when the commit was made, and click the "Copy full SHA for ..." button to get the value for the commithash.
If you don't know the date for your revision, you can approximately date it via [OpenRS2's Caches](https://archive.openrs2.org/caches) - simply look for you revision,
look at the date it was first and last published and then locate a bootstrap that falls in that date range.
7. If you wish to use RuneLite, and your revision is 228 or older, you must assign the runelite_gamepack_url property. The gamepacks can be found [here](https://github.com/runetech/osrs-gamepacks/tree/master/gamepacks).
Simply pick your revision and click the "Copy Link" button in your browser for the Raw File. This is the URL to enter.

## Progress

This describes the implementation in this branch, not necessarily the latest published build.

### Shared Tooling

- [x] Graphical user interface and live transcripts
- [x] Binary recording and saved-file transcription
- [x] Filters and transcript settings
- [x] Linked Jagex Account characters
- [x] Proxy launcher/updater
- [ ] Public archive
  - [ ] Automated binary blob uploading at the end of a session
  - [x] Indexing of OSRS binary files
  - [ ] Ability to download any binary blobs

### Old School RuneScape

- [ ] Patch Tool
  - [x] Native (Win)
    - [x] Supports Unix via `wine` or `proton`
  - [ ] ~~Native (Mac)~~ (Automated patching too fragile)
  - [x] RuneLite (All Operating systems)
- [x] World identification via localhost address
- [x] World-hop host address injection
- [x] Login re-encoding & obtaining ISAAC Seed
- [x] HTTP Server (worldlist.ws, jav_config.ws)
- [x] Binary header building
- [x] Binary blob reader/writer
- [x] Known-field privacy scrubbing (not a guarantee of complete sanitization)
  - [x] Bank Pin erasure (4-digit code)
  - [x] Private message content erasure
  - [x] Login tokens in URL open packets
  - [x] 192-bit UID (linked to account recoveries)
  - [x] Site settings (linked to account recoveries)
  - [x] Erases all keyboard presses
- [x] Live cache loading
- [x] Historical cache loading
- [x] Revision-specific decoders starting from revision 223
- [x] Transcriber
- [x] RuneLite launcher
- [x] Replay for supported recordings
- [x] Custom proxy targets

### RuneScape 3 (Experimental)

- [x] Revision-950 Windows-native client patching
- [x] World/lobby routing and multiple client instances
- [x] Lobby/game transitions, world hopping and reconnects
- [x] Client/server packet decoders and transcribers
- [x] Player/NPC information and zone packet decoding
- [x] Binary recording with lobby/game initialization and transfer markers
- [x] Live cache definitions and historical definition retrieval from OpenRS2
- [x] Bundled gamevals and archived clientscript signatures
- [x] Separate lobby visibility and game-only first-tick filtering
- [x] One-time experimental/privacy warning
- [x] Known-field privacy scrubbing
- [ ] Further live validation and privacy coverage review
- [ ] Replay
- [ ] Custom proxy targets
- [ ] Verified non-Windows support

[actions-badge]: https://github.com/blurite/rsprox/actions/workflows/proxy-gui.yml/badge.svg
[actions]: https://github.com/blurite/rsprox/actions
[mit-badge]: https://img.shields.io/badge/license-MIT-informational
[mit]: https://opensource.org/license/MIT
