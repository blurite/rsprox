# rsprox MCP server

This module runs rsprox without its GUI and serves it to a coding agent as an
[MCP](https://modelcontextprotocol.io) server. The agent launches a RuneLite client through the proxy,
reads the decoded packets in both directions, and looks at and drives the client. It is meant for
checking a private server end to end with a real client. The same endpoint also runs inside the rsprox
GUI, where it attaches to the clients that you launch by hand.

The proxy only observes, so it cannot inject or change packets. All input goes through the client as
mouse and key events, from a small RuneLite plugin that the server installs before each launch.

## Run it

```
./gradlew :mcp:run
```

The server listens on `http://127.0.0.1:43580/mcp`. It uses the proxy targets that the rsprox GUI
uses, from `~/.rsprox/proxy-targets.yaml`. A session records to the usual folder of its target, as it
does when launched from the GUI. Each client opens a window, so the machine needs a display or a
virtual one, as described under "Run it without a display".

Options go in `--args`:

```
./gradlew :mcp:run --args="--start 'My Server'"
```

| Option | Meaning |
|---|---|
| `--port <n>` | Loopback port of the MCP endpoint. Default 43580. |
| `--start <target>` | Launch a client for this target at startup. |
| `--sideload-dir <dir>` | Where to install the client plugin, when the client does not use the default directory. |
| `--port-skip <n>` | Proxy ports to leave free at the start of the range for a GUI. Default 50. |
| `--software-rendering` | Stop the GPU plugin in launched clients. Needed on a virtual display. |

The plugin is installed as `rsprox-mcp-bridge.jar` in `~/.rlcustom/sideloaded-plugins` for a custom
target and in `~/.runelite/sideloaded-plugins` for the official one. A client that the MCP server did
not launch loads the plugin too, and the plugin then does nothing.

Run one MCP endpoint per user account. The plugin finds its server through the file
`~/.rsprox/mcp/bridge.json`, which every endpoint of the account writes, so another `--port` is not
enough. To run this server next to a GUI, turn the GUI's endpoint off first.

## Run it inside the GUI

```
./gradlew proxy
```

The GUI serves the endpoint on `http://127.0.0.1:43580/mcp` from the moment the proxy has started. Two
lines in `~/.rsprox/proxy.properties` change that. Edit the file while the GUI is closed, because the
GUI writes the file again when it saves its own settings.

| Property | Meaning |
|---|---|
| `mcp.enabled` | Whether the GUI serves the endpoint. Default `true`. Set it to `false` to turn the endpoint off. |
| `mcp.port` | Loopback port of the endpoint. Default 43580. |

When the port is taken, by a second rsprox or by the standalone server, the GUI logs one line that
says so and runs without the endpoint.

Every RuneLite or native client that you launch from the GUI appears in `session_list` as a session
with `"kind":"attached"`, numbered together with the sessions that `session_start` launches. An agent
picks the client you mean by the `target`, the `proxyPort` and, once you are logged in, the values
under `login`, such as the `world` and the display `name`. The proxy never sees a login name or a
password, so neither is listed. RuneScape 3 clients are not attached.

An attached session is read-only:

- `packets_read` works on it as on any session.
- `session_stop`, `session_start` and every `client_*` tool refuse it with `not available for an
  attached session`. The client is yours, so the server never stops it, clicks in it or reads its
  screen.
- When you close the session's tab in the GUI, or the client exits, the session becomes
  `"state":"ended"`. It stays listed and its packets stay readable.

Inside the GUI the packet log holds the GUI's view, for attached sessions and for sessions that
`session_start` launches alike. The GUI's filters and settings decide which packets are transcribed
at all, so a packet that they hide is missing from `packets_read` too. Enable a filter in the GUI to
see its packets. The standalone server has no such filters and logs every packet.

The endpoint has no password. Any program that runs under your user account, and any other user
of the machine, can reach it on loopback and read everything the log holds, including public and
private chat, for as long as the GUI runs. Turn `mcp.enabled` off when that is not acceptable.

## Run it without a display

On Linux, run the server under Xvfb and pass `--software-rendering`:

```
xvfb-run -s "-screen 0 1280x800x24" ./gradlew :mcp:run --args="--software-rendering"
```

Without `--software-rendering` everything works except `client_screenshot`, which returns a black
image. Besides a JDK, the machine needs Xvfb and the X11 and font libraries that Java's AWT loads. On
Debian or Ubuntu:

```
apt-get install xvfb libxrender1 libxtst6 libxi6 libxext6 libfontconfig1 libfreetype6 fonts-dejavu-core
```

## Register it with an MCP client

The transport is plain HTTP. With Claude Code:

```
claude mcp add --transport http rsprox http://127.0.0.1:43580/mcp
```

Any other client takes the same URL in its configuration for an HTTP server. Start the server before
the client connects. The server keeps its sessions and packets for as long as it runs, whatever the
clients do.

## Tools

The server describes each tool itself. `tools/list` returns every tool with its description and the
schema of its arguments:

```
curl -s http://127.0.0.1:43580/mcp -d '{"jsonrpc":"2.0","id":1,"method":"tools/list"}'
```

The descriptions leave out the following:

- `session_start` gives the launcher of a client 180 seconds to complete its handshake. When the
  launcher takes longer, the server abandons the launch and refuses further launches until it is
  restarted.
- The key codes of typed text are zeroed in the recorded packets, as in every rsprox recording. Read
  what was typed from the packet that carries the text, such as `RESUME_P_STRINGDIALOG`.
- Only one call drives the mouse and the keyboard of a client at a time. `client_interact`,
  `client_click`, `client_type` and a `client_camera` call that turns the camera take turns. The
  calls that only read run beside them.
- The client offers only Cancel at a point that an interface covers, such as the chatbox or the
  minimap. `client_interact` never clicks there. It turns the camera after a few such points, and
  then fails with `the client offers only Cancel; an interface covers the game view there`. An
  interface that blocks the whole game view, such as the bank, gives the same message. Close it first.
- A target that is hidden from every angle, behind a wall for instance, cannot be clicked. Neither
  can anything in a secondary world view such as a boat: `client_entities` and `client_interact`
  read only the top-level world view.

## Example: check that setting a display name works

The display-name interface is group 558, and its name field is widget `558:12`.

```
1. session_start {"target":"My Server"}
   -> {"session":"s1","kind":"launched","target":"My Server","state":"connected", ...,"cursor":2}
2. client_login {"username":"alice","password":"any"}
   -> {"gameState":"LOGGED_IN","cursor":2}
3. client_click {"widget":"558:12"}
   -> {"x":633,"y":361,"cursor":471}
4. client_type {"text":"Alice","enter":true}
   -> {"typed":5,"cursor":484}
5. packets_read {"after":484,"prots":["IF_SETTEXT"],"contains":"Alice","wait_ms":5000}
   -> {"next":519,"head":519,"dropped":0,"timedOut":false,"count":1}
      519 L1 T8 S IF_SETTEXT [if_settext] com=tutorial_displayname:status (558:13), text="Great! The
          display name <col=ffffff>Alice</col> is <col=00ff00>available</col>! ..."
```

Step 5 proves that the server answered the name the client sent. The same wait proves that nothing
happened. Wait for the packet and expect a timeout:
`packets_read {"after":519,"prots":["IF_SETTEXT"],"wait_ms":1800}` returning `"timedOut":true` means
the server sent no such packet for three ticks.

When the client of a session has exited or was stopped, call `session_start {"session":"s1"}`. The
session keeps its id and its packet log. The client comes back on new ports, and the packets of its
next login are marked with the next login number.

## What the automated tests do not cover

The tests run no game client. Check what the plugin does inside RuneLite by hand against a live
client, after a RuneLite update, a game revision change or a change to the plugin.
