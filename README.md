# RSProx

[![GitHub Actions][actions-badge]][actions] [![MIT license][mit-badge]][mit]

## Introduction
RSProx is a locally hosted proxy server intended to act as a middleman between the clients and servers in Old School RuneScape.
Support for RuneScape 3 may be added later in the future, if a maintainer for packet decoders can be found.

RSProx will work by patching a root client, allowing it to connect to the locally-hosted proxy server rather than the real servers.
The original information that the patcher overwrote will be passed onto the proxy tool, allowing it to establish connections with
what the client had originally intended. We intend to support most java clients, as well as the C++ client.
A technical breakdown of the processes involved can be found in [issues/RSProx](https://github.com/blurite/rsprox/issues/1).

## Guide
Below is a quick guide demonstrating how to use RSProx.

### Cloning
As of right now, RSProx can only be used by cloning the repository yourself.

### Launching
RSProx project can be launched by either running the class
`net.rsprox.gui.ProxyToolGuiKt` directly in gui/proxy-tool module,
or by it via gradle as `./gradlew proxy`.

> [!NOTE]
> Native client can currently only be ran on Windows and Linux (requiring wine).
> MacOS support will be added in the future.

### Usage
Upon launching RSProx tool, you will be met with a relatively empty GUI.
In order to start using it, select the Jagex Account Mode from the first
dropdown as seen below, the client type you wish to use from the second
dropdown and press the launch button to the right.
The 'Default' Jagex Account mode means no Jagex Account will be used, and
you will be prompted for an e-mail and password to login.

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

### Jagex Accounts
Jagex Accounts are now fully supported. There are two ways of using a Jagex
account:

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

## Progress
Below is a small task list showing a rough breakdown of what the tool will consist of, and how far the progress is at any given moment.

- [ ] Patch Tool
  - [x] Native (Win)
    - [x] Supports Unix via `wine`
  - [ ] ~~Native (Mac)~~ (Automated patching too fragile)
  - [x] RuneLite (All Operating systems)
  - [ ] RuneLite Forks
  - [ ] ~~HDOS~~ (Cancelled, too difficult to patch)
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
