# RSProx

[![GitHub Actions][actions-badge]][actions] [![MIT license][mit-badge]][mit]

## Introduction
RSProx is a locally hosted proxy server intended to act as a middleman between the clients and servers in Old School RuneScape.
Support for RuneScape 3 may be added later in the future, if a maintainer for packet decoders can be found.

RSProx will work by patching a root client, allowing it to connect to the locally-hosted proxy server rather than the real servers.
The original information that the patcher overwrote will be passed onto the proxy tool, allowing it to establish connections with
what the client had originally intended. We intend to support most java clients, as well as the C++ client.
A technical breakdown of the processes involved can be found in [issues/RSProx](https://github.com/blurite/rsprox/issues/1).

## Progress
Below is a small task list showing a rough breakdown of what the tool will consist of, and how far the progress is at any given moment.

- [ ] Patch Tool
  - [x] Native (Win)
    - [x] Supports Unix via `wine`
  - [x] Native (Mac)
  - [ ] RuneLite
  - [ ] RuneLite Forks
  - [ ] HDOS
- [x] World identification via localhost address
- [x] World-hop host address injection
- [x] Login re-encoding & obtaining ISAAC Seed
- [x] HTTP Server (worldlist.ws, jav_config.ws)
- [x] Binary header building
- [x] Binary blob reader/writer
- [x] Privacy concerns
  - [x] Bank Pin erasure
  - [x] Private message content erasure
  - [x] Login tokens in URL open packets
- [ ] Graphical User Interface (Proxy tool)
- [ ] Graphical User Interface (Live Transcriber)
- [ ] Live cache loading
- [ ] Historical cache loading
- [ ] Public archive
  - [ ] Automated binary blob uploading at the end of a session
  - [ ] Indexing of binary files
  - [ ] Ability to download any binary blobs
- [x] Decoders
  - [x] Plugin system for decoders, with each revision acting as its own plugin
  - [x] Revision 223 (client decoders)
  - [x] Revision 223 (server decoders)
- [ ] Transcriber
- [ ] Launchers
  - [ ] Proxy launcher/updater
  - [ ] RuneLite launcher (necessary to avoid detection)

[actions-badge]: https://github.com/blurite/rsprox/actions/workflows/ci.yml/badge.svg
[actions]: https://github.com/blurite/rsprox/actions
[mit-badge]: https://img.shields.io/badge/license-MIT-informational
[mit]: https://opensource.org/license/MIT
