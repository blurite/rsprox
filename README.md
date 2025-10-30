# RSProx

[![GitHub Actions][actions-badge]][actions] [![MIT license][mit-badge]][mit]

## Introduction
RSProx is a locally hosted proxy server intended to act as a middleman between the clients and servers in Old School RuneScape.
Support for RuneScape 3 may be added later in the future, if a maintainer for packet decoders can be found.

RSProx will work by patching a root client, allowing it to connect to the locally-hosted proxy server rather than the real servers.
The original information that the patcher overwrote will be passed onto the proxy tool, allowing it to establish connections with
what the client had originally intended. We intend to support most java clients, as well as the C++ client.
A technical breakdown of the processes involved can be found in [issues/RSProx](https://github.com/blurite/rsprox/issues/1).

## Installer

> [!TIP]
> Use the installer for better compatibility and ease of use.

The installer for RSProx can be found [under releases](https://github.com/blurite/rsprox/releases),
the latest installer is [v1.0](https://github.com/blurite/rsprox/releases/tag/v1.0).

The installer comes with a bundled JDK which it utilizes. The launcher auto-updates
RSProx whenever a new version is published.

## Guide
Below is a quick guide demonstrating how to use RSProx.

### Cloning
As of right now, RSProx can only be used by cloning the repository yourself.

### Launching
RSProx project can be launched by either running the class
`net.rsprox.gui.ProxyToolGuiKt` directly in gui/proxy-tool module,
or by it via gradle as `./gradlew proxy`.

> [!NOTE]
> Native client can currently only be run on Windows and Linux (requiring wine).
> MacOS support will be added in the future.

### Usage
Upon launching RSProx tool, you will be met with a relatively empty GUI.
In order to start using it, select the Jagex Account Mode from the first
dropdown as seen below, the client type you wish to use from the second
dropdown and press the launch button to the right.
The 'Default' Jagex Account mode means no Jagex Account will be used, and
you will be prompted for an e-mail and password to login.

> [!CAUTION]
> MacOS support requires whitelisting certain loopback addresses!
> See the chapter below.


![Launching client](https://media.z-kris.com/2024/10/javaw_Zdj10a5jq8.png)

After launching a client, a process will occur which will download the necessary
files and patch them, so they can pass data through the proxy itself.
On RuneLite, this can take tens of seconds as everything loads up. Caching
mechanism is included on RuneLite that makes consecutive launches faster than
the first one (or whenever the cache is invalidated).

Once the client has fully booted up, you may log in. All the data will flow
through the proxy and get logged on your PC. No data will leave your PC without
your permission.
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

#### MacOS Support
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
Besides live transcribing which happens on the UI directly, it is possible to
transcribe .bin files previously created. To do so, one can simply drag the
.bin files they wish to transcribe anywhere onto the GUI, and a background
process will take place, which ends up making .txt files with the same name.
The transcription uses the currently-active filters as it would during live
transcripts.

![Example](https://media.z-kris.com/2025/08/java_ywUBskAkZ4.gif)

### Jagex Accounts
Jagex Accounts are now fully supported. There are two ways of using a Jagex
account:
[build.gradle.kts](build.gradle.kts)
1. Using the built-in authentication system. RSProx allows you to login via
your browser to authenticate yourself for the proxy tool, allowing easy access
to all the characters under that Jagex account. You can also link multiple
different Jagex accounts to make the process of switching accounts easier.
2. The Default, legacy behaviour. In this mode, you need to export the
credentials.properties file in `user.home/.runelite/credentials.properties` via
[this](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts).
Once you have exported the credentials.properties as shown in the tutorial,
the RSProx tool will always load them up from `user.home/.runelite/credentials.properties`.
If you wish to stop using a Jagex Account in this Default mode,
simply delete the credentials file. In this Default mode, you may only have
one character/account, as it always reads from the same file when launching
the client.

#### Linux Setup
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

### Security
We have taken many measures to ensure the players can securely use this tool,
without having to worry about getting banned or having their information leaked.

#### So, is it safe to use?
It depends. We are very confident in the Native client being safe for use,
as there are very minor modifications done to that client, and knowing the limits
of C++, it is not possible for Jagex to identify these changes.

However, when it comes to RuneLite, there are quite a bit more changes done.
While we are certain about RuneLite being safe to use right now, nothing prevents
RuneLite developers from adding in more checks in the future that this tool might
not catch. Is this likely? No. Could it still happen? Absolutely.

People may have concerns over so many third-party clients getting banned as of
recent, and while the concerns are valid, they do not apply to RSProx. We simply
do not modify enough for it to be detectable. Every bit of information sent on
login, which is how third party clients tend to get caught, is unaffected.
This was achieved via numerous clever tricks that will not be explored here,
as to avoid people maliciously using them.

### Private Server Usage
RSProx can currently be used to connect to private servers, but only under
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
    jav_config_url: https://client.blurite.io/jav_local_227.ws
    revision: 227.3
    modulus: d2a780dccbcf534dc61a36deff725aabf9f46fc9ea298ac8c39b89b5bcb5d0817f8c9f59621187d448da9949aca848d0b2acae50c3122b7da53a79e6fe87ff76b675bcbf5bc18fbd2c9ed8f4cff2b7140508049eb119259af888eb9d20e8cea8a4384b06589483bcda11affd8d67756bc93a4d786494cdf7b634e3228b64116d
  - name: Local Test World
    jav_config_url: https://example.com/jav_config.ws
    modulus: 9fca1d4f3eaa48d1ac6b189b5c9a88c6fbf7f8b2b5b9f4923c9c088da6cb34c1f02f55d30f9ec0e25f20fba8eea741e2cb9408c4de61b935f1c36b3b21b7aa15
    game_server_port: 50000
```

If you are hosting the configuration yourself on a url and prefer a shorter document, the importer also understands the following equivalent structure:

```yaml
- name: Blurite
  jav_config_url: https://client.blurite.io/jav_local_227.ws
  revision: 227.3
```

Properties breakdown:

- `name` - The name given to the client. Any references to `OldSchool RuneScape` will be replaced by this. This is a required property to ensure caches don't overwrite and cause crashing at runtime when loading different games simultaneously.
- `jav_config_url` - The URL to the jav_config that will be used to load initial world and world list. This is a required property.
- `modulus` - A hexadecimal (base-16) RSA modulus used to encrypt the login packet sent to the client. This is a required property.
- `revision` – A revision number used to pick the client and correct decoders. The default is the latest stable Old School RuneScape version. For Native clients, if no subrevision is provided (e.g. `234`), `.1` is automatically appended (becoming `234.1`). See: [https://archive.lostcity.rs/oldschool.runescape.com/native/osrs-win/](https://archive.lostcity.rs/oldschool.runescape.com/native/osrs-win/). This is an optional property.
- `varp_count` - (OPTIONAL) Changes the array length used for varps in the client, the default value is 5000. This is an optional property. As of revision 232, it is no longer patched as the value is now based on cache.
- `runelite_bootstrap_commithash` - (OPTIONAL) A hash pointing to a version of RuneLite you wish to use. This is an optional property, not defining it will use the latest for your revision.
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
Below is a small task list showing a rough breakdown of what the tool will consist of, and how far the progress is at any given moment.

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
- [x] Privacy concerns
  - [x] Bank Pin erasure (4-digit code)
  - [x] Private message content erasure
  - [x] Login tokens in URL open packets
  - [x] 192-bit UID (linked to account recoveries)
  - [x] Site settings (linked to account recoveries)
  - [x] Erases all keyboard presses
- [x] Graphical User Interface (Proxy tool)
- [x] Graphical User Interface (Live Transcriber)
- [x] Live cache loading
- [x] Historical cache loading
- [ ] Public archive
  - [ ] Automated binary blob uploading at the end of a session
  - [x] Indexing of binary files
  - [ ] Ability to download any binary blobs
- [x] Decoders (Every revision starting from 223)
- [x] Transcriber
- [x] Launchers
  - [x] Proxy launcher/updater
  - [x] RuneLite launcher (necessary to avoid detection)

[actions-badge]: https://github.com/blurite/rsprox/actions/workflows/proxy-gui.yml/badge.svg
[actions]: https://github.com/blurite/rsprox/actions
[mit-badge]: https://img.shields.io/badge/license-MIT-informational
[mit]: https://opensource.org/license/MIT
