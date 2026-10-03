# rsprox MCP server

This module runs rsprox without its GUI and serves it to a coding agent as an
[MCP](https://modelcontextprotocol.io) server. The agent launches a RuneLite client through the proxy,
reads the decoded packets in both directions, and looks at and drives the client. It is meant for
checking a private server end to end with a real client.

The server has two halves. The proxy half runs in this process and records every packet. The client
half is a small RuneLite plugin that the server installs into the client's sideload directory before
each launch. The plugin connects back to the server over loopback.

## Run it

```
./gradlew :mcp:run
```

The server listens on `http://127.0.0.1:43580/mcp`. It uses the proxy targets that the rsprox GUI
uses, from `~/.rsprox/proxy-targets.yaml`. The GUI may run at the same time.

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

The plugin is installed as `rsprox-mcp-bridge.jar` in `~/.rlcustom/sideloaded-plugins` for a custom
target and in `~/.runelite/sideloaded-plugins` for the official one. A client that the MCP server did
not launch loads the plugin too, and the plugin then does nothing.

## Register it with an MCP client

The transport is plain HTTP. With Claude Code:

```
claude mcp add --transport http rsprox http://127.0.0.1:43580/mcp
```

Any other client takes the same URL in its configuration for an HTTP server:

```json
{ "mcpServers": { "rsprox": { "type": "http", "url": "http://127.0.0.1:43580/mcp" } } }
```

Start the server before the client connects. The server keeps its sessions and packets for as long as
it runs, whatever the clients do.

## Tools

`session` is optional on every tool while only one session exists.

| Tool | What it does |
|---|---|
| `session_start` | Launches a client for a target, or relaunches a stopped session. Waits until the client plugin has connected. |
| `session_stop` | Kills the client of a session. The session and its packets stay readable. |
| `session_list` | Lists the sessions with their state, ports, login and packet cursor, and the target names. |
| `packets_read` | Reads decoded packets after a cursor, unfiltered, in both directions. With `wait_ms` it waits for the first match. |
| `client_state` | Reads the game state, tick, canvas size, world, local player and right-click menu. |
| `client_screenshot` | Returns the game canvas as a PNG. Image pixels are click coordinates. |
| `client_widgets` | Lists the interface widgets that have text, a name or actions, each with its bounds and click point. |
| `client_vars` | Reads varps, varbits and client variables by id. |
| `client_login` | Logs in from the login screen and waits until the client is in the game. |
| `client_click` | Clicks at canvas coordinates or at the centre of a widget. |
| `client_type` | Types text as key presses, with an optional Enter. |

Every `client_*` result carries `cursor`, the packet cursor taken just before the call. Pass it as
`after` to `packets_read` to see only what the action caused.

Each packet is one line: `<seq> L<login> T<tick> <C|S|P> <PROT> <text>`. `C` is client to server, `S`
is server to client, and `P` is a marker that rsprox adds (`CLIENT_LAUNCHED`, `CLIENT_CONNECTED`,
`CLIENT_EXITED`, `LOGIN`, `LOGOUT`).

## Example: check that setting a display name works

The widget ids and packet contents below are illustrative. They depend on the server.

```
1. session_start {"target":"My Server"}
   -> {"session":"s1","target":"My Server","state":"connected","generation":1,"proxyPort":43751,
       "httpPort":43650,"pid":40388,"cursor":2}

2. client_login {"username":"mcptest","password":"any"}
   -> {"gameState":"LOGGED_IN","cursor":2}

3. client_widgets {"text":"look up name"}
   -> {"roots":[548],"widgets":[{"id":"558:7","text":"Look up name","name":"","actions":["Look up name"],
       "bounds":[315,200,130,30],"click":[380,215],"type":4,"hidden":false}],"truncated":false,"cursor":604}

4. client_click {"widget":"558:7"}
   -> {"x":380,"y":215,"cursor":640}

5. packets_read {"after":640,"prots":["IF_BUTTONX"],"wait_ms":3000}
   -> {"next":647,"head":647,"dropped":0,"timedOut":false,"count":1}
      645 L1 T46 C IF_BUTTONX [if_buttonx] com=558:7, sub=-1, obj=-1, button=1

6. client_type {"text":"McpProto","enter":true}
   -> {"typed":8,"cursor":655}

7. packets_read {"after":655,"prots":["RESUME_P_STRINGDIALOG","IF_SETTEXT"],"contains":"McpProto","wait_ms":5000}
   -> {"next":671,"head":671,"dropped":0,"timedOut":false,"count":2}
      662 L1 T48 C RESUME_P_STRINGDIALOG [resume_p_stringdialog] string="McpProto"
      669 L1 T49 S IF_SETTEXT [if_settext] com=558:13, text="<col=00ff00>McpProto</col> is available"

8. client_widgets {"group":558,"text":"is available"}
   -> {"roots":[548],"widgets":[{"id":"558:13","text":"<col=00ff00>McpProto</col> is available", ...}],
       "truncated":false,"cursor":671}

9. session_stop {}
   -> {"session":"s1","target":"My Server","state":"stopped","reason":"stopped by caller", ...}
```

Steps 5, 7 and 8 are three separate proofs. The client sent the click. The client sent the name and
the server answered. The client rendered the answer.

Two more patterns are useful:

- To assert that nothing happened, wait for the packet and expect a timeout.
  `packets_read {"after":655,"prots":["IF_SETTEXT"],"wait_ms":1800}` returning `"timedOut":true` means
  the server sent no such packet for three ticks.
- After a server restart, call `session_start {"session":"s1"}`. The session keeps its id and its
  packet log. The client comes back on new ports, and new packets are marked with the next login
  number.

## Known limits

- The client needs a display. The server has no GUI of its own, but RuneLite opens a window.
- The proxy only observes. It cannot inject or change packets. All input goes through the client as
  mouse and key events.
- A launch whose launcher never completes its handshake is abandoned after 180 seconds. After that,
  the server refuses further launches until it is restarted.
- Sessions record to the usual folder of the target, as they do when launched from the GUI.
- The rendezvous file `~/.rsprox/mcp/bridge.json` is shared, so run one MCP server per user account.
- Key codes of typed text are zeroed in the recorded packets, as in every rsprox recording. Read what
  was typed from the packet that carries the text, such as `RESUME_P_STRINGDIALOG`.
