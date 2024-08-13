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
For new users, clean clone can be done via
```
git clone --recurse-submodules git@github.com:blurite/rsprox.git
```

For older users, if the runelite-launcher submodule has not been initialized,
run the following git command
```
git submodule update --init --recursive
```

> [!IMPORTANT]
> If you run into an error stating `fatal: destination path '*' already exists and is not an empty directory.`,
> you will have to run `git submodule deinit -f runelite-launcher` and re-initialize it
> as shown above.

After initializing the submodule, make sure to run `./gradlew --refresh-dependencies clean build`.
This will properly download the submodules and rebuild the project, ensuring
that all the files that are necessary to run, will be available.

### Launching
RSProx project can be launched by either running the class
`net.rsprox.gui.ProxyToolGuiKt` directly in gui/proxy-tool module,
or by it via gradle as `./gradlew proxy`.

> [!NOTE]
> Native client can currently only be ran on Windows and Linux (requiring wine).
> MacOS support will be added in the future.

### Usage
Upon launching RSProx tool, you will be met with a relatively empty GUI.
In order to select a client to boot up, click the `+` sign in the top-right corner,
as seen depicted here:

![Launching client](https://media.z-kris.com/2024/08/java_WjDIiakbS9.png)

After selecting a client, a process will occur which will download the necessary
files and patch them, so they can pass data through the proxy itself.
On RuneLite, this can take tens of seconds as everything loads up. Caching
mechanism is included on RuneLite that makes consecutive launches faster than
the first one (or whenever the cache is invalidated).

Once the client has fully booted up, you may log in. All the data will flow
through the proxy and get logged on your PC. No data will leave your PC without
your permission.
Upon logging in, you should be met with logs being written in your RSProx GUI
as depicted here:

![Active client](https://media.z-kris.com/2024/08/java_XoLwvoxN5e.png)

In order to filter the data that gets logged, you may use the filters panel
that is at the top-right section of the client, as seen depicted here:

![Client filters](https://media.z-kris.com/2024/08/java_c7KjkoeWPd.png)

Within the filters, you can toggle individual packets, entire packet groups,
or even the full `Incoming` or `Outgoing` categories, by right-clicking on said
categories.

> [!TIP]
> The Default preset does not support modifications. If you wish to toggle
> filters, you must make a filter preset of your choice.
> You can have an unlimited amount of filter presets, and they are saved on
> your PC.

### Jagex Accounts
Jagex Accounts are currently supported in a preliminary fashion.
In order to make use of Jagex Accounts, you must export the credentials via
RuneLite. This can be done following the tutorial
[here](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts).

Once you have exported the credentials.properties as shown in the tutorial,
the RSProx tool will always load them up from `user.home/.runelite/credentials.properties`.
If you wish to stop using a Jagex Account, simply delete the credentials file.
In the future, support will be added for a more granular control over Jagex Accounts.

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
- [x] Decoders
  - [x] Plugin system for decoders, with each revision acting as its own plugin
  - [x] Revision 223 (client decoders)
  - [x] Revision 223 (server decoders)
- [x] Transcriber
- [ ] Launchers
  - [ ] Proxy launcher/updater
  - [x] RuneLite launcher (necessary to avoid detection)

[actions-badge]: https://github.com/blurite/rsprox/actions/workflows/ci.yml/badge.svg
[actions]: https://github.com/blurite/rsprox/actions
[mit-badge]: https://img.shields.io/badge/license-MIT-informational
[mit]: https://opensource.org/license/MIT
