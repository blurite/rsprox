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
  - [ ] C++
  - [ ] RuneLite
  - [ ] RuneLite Forks
  - [ ] HDOS
- [x] World identification via localhost address
- [ ] World-hop host address injection
- [x] Login re-encoding & obtaining ISAAC Seed
- [x] HTTP Server (worldlist.ws, jav_config.ws)
- [x] Binary header building
- [ ] Binary blob reader/writer
- [ ] Privacy concerns
  - [ ] Bank Pin erasure
  - [ ] Private message content erasure
- [ ] Graphical User Interface (Proxy tool)
- [ ] Graphical User Interface (Live Transcriber)
- [ ] Public archive
  - [ ] Automated binary blob uploading at the end of a session
  - [ ] Indexing of binary files
  - [ ] Ability to download any binary blobs
- [ ] Decoders
  - [ ] Plugin system for decoders, with each revision acting as its own plugin
  - [ ] Revision 223 (initial revision)
- [ ] Transcriber
- [ ] Launchers
  - [ ] Proxy launcher/updater
  - [ ] RuneLite launcher (necessary to avoid detection)

[actions-badge]: https://github.com/blurite/rsprox/actions/workflows/ci.yml/badge.svg
[actions]: https://github.com/blurite/rsprox/actions
[mit-badge]: https://img.shields.io/badge/license-MIT-informational
[mit]: https://opensource.org/license/MIT
