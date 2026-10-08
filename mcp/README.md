# rsprox MCP server

This module runs rsprox without its GUI and serves it to a coding agent as an
[MCP](https://modelcontextprotocol.io) server. The agent launches a RuneLite client through the proxy,
reads the decoded packets in both directions, and looks at and drives the client. It is meant for
checking a private server end to end with a real client.

The same endpoint also runs inside the rsprox GUI. There it attaches to the clients that you launch
by hand, so an agent can read the packets of your own session while you play. See "Run it inside the
GUI".

The server has two halves. The proxy half runs in this process and records every packet. The client
half is a small RuneLite plugin that the server installs into the client's sideload directory before
each launch. The plugin connects back to the server over loopback. The proxy only observes, so it
cannot inject or change packets. All input goes through the client as mouse and key events, so the
server receives what it receives from a player.

## Run it

```
./gradlew :mcp:run
```

The server listens on `http://127.0.0.1:43580/mcp`. It uses the proxy targets that the rsprox GUI
uses, from `~/.rsprox/proxy-targets.yaml`. A session records to the usual folder of its target, as it
does when launched from the GUI.

The GUI serves the same endpoint on the same port, so only one of the two can hold it. To run this
server next to a GUI, turn the GUI's endpoint off first, as described under "Run it inside the GUI".
Giving this server another port with `--port` is not enough, because both would write the same
rendezvous file.

Each client opens a window, so the machine needs a display. A machine without one can use a virtual
display, as described under "Run it without a display".

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
not launch loads the plugin too, and the plugin then does nothing. The plugin finds the server through
the rendezvous file `~/.rsprox/mcp/bridge.json`. Every server of one user account writes that same
file, so run one MCP server per user account.

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
with `"kind":"attached"`. Sessions are numbered in the order they appear, `s1`, `s2` and so on,
together with the sessions that `session_start` launches. An agent picks the client you mean by the
`target`, the `proxyPort` and, once you are logged in, the `world`, the display `name` and the other
values under `login`. The proxy never sees a login name or a password, so neither is listed.
RuneScape 3 clients are not attached.

An attached session is read-only:

- `packets_read` works on it as on any session, with the same cursors and the same `L<login>` number
  for each login.
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

RuneLite always opens a window, so on a machine with no screen give it a virtual one. macOS and
Windows have no virtual display, so there the client needs a real screen. On Linux, run the server
under Xvfb and pass `--software-rendering`:

```
xvfb-run -s "-screen 0 1280x800x24" ./gradlew :mcp:run --args="--software-rendering"
```

Without `--software-rendering` everything works except `client_screenshot`, which returns a black
image. The GPU plugin renders with a software OpenGL driver on a virtual display, and the frames read
back from it are empty. The option stops the GPU plugin in each client the server launches. It does
not change the RuneLite profile, so clients started any other way keep their GPU setting.

Besides a JDK, the machine needs Xvfb and the X11 and font libraries that Java's AWT loads. On Debian
or Ubuntu:

```
apt-get install xvfb libxrender1 libxtst6 libxi6 libxext6 libfontconfig1 libfreetype6 fonts-dejavu-core
```

The machine needs no sound device. Without one the client reports that audio is unavailable and
carries on. It needs no loopback alias either. A custom target reaches each world through a loopback
address such as `127.0.1.3`, and Linux routes all of `127.0.0.0/8` to loopback.

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

The tools come in three groups. The `session_*` tools launch a client for a target, stop it and list
the sessions. `session_list` tells the sessions apart: each has its id, its `kind` (`launched` or
`attached`), its target, its state and its proxy port. Once the client has logged in, `login` holds
the login number, the revision, the world and its host, the index of the local player, the time of
the login and whether the client is still logged in. The capture file, the display name and the
newest tick are added when the proxy learns them. `session_start` gives the launcher of a client 180 seconds to complete its handshake.
When the launcher takes longer, the server abandons the launch and refuses further launches until it
is restarted. `packets_read` reads the decoded packets of a session after a cursor, and can wait for
the first packet that matches. The `client_*` tools look at the client and drive it: they read its
state, widgets and variables, take a screenshot, log in, click and type, list what is near the player
and act on it.

The server describes each tool itself. `tools/list` returns every tool with its description and the
schema of its arguments:

```
curl -s http://127.0.0.1:43580/mcp -d '{"jsonrpc":"2.0","id":1,"method":"tools/list"}'
```

Each packet is one line: `<seq> L<login> T<tick> <C|S|P> <PROT> <text>`. `C` is client to server, `S`
is server to client, and `P` is a marker that rsprox adds (`CLIENT_LAUNCHED`, `CLIENT_CONNECTED`,
`CLIENT_ATTACHED`, `CLIENT_EXITED`, `LOGIN`, `LOGOUT`). The key codes of typed text are zeroed in the recorded packets, as
in every rsprox recording. Read what was typed from the packet that carries the text, such as
`RESUME_P_STRINGDIALOG`.

## Act on the game world

Use `client_entities` and `client_interact` for anything in the game world: NPCs, objects, ground
items, other players, walking and dialogs. `client_entities` lists what is near the local player,
nearest first, with the options that each thing offers. Its coordinates are world coordinates, the
ones that the packets carry. An object that covers several tiles is listed once, at its south-west
tile.

`client_interact` performs one of the listed options, or `Examine` on an NPC, an object or a ground
item, the way a player does. It finds the target in the client's own lists, moves the real mouse onto
it, lets the client build the menu for what is under the mouse, and clicks: a left click when the
option looks like the default one, otherwise a right click and a click on the option's row. The
server receives exactly what a player's interaction sends, mouse packets included. The client decides
what a click means, so a target that moves after the mouse was aimed would get the wrong click. The
plugin sees the click the client is about to perform, cancels it before anything is sent when it is
not the intended one, and aims again, within the game tick. Only the client knows which option a left
click performs, because it sorts its menu late and a plugin such as the menu entry swapper can
reorder it. When a left click turns out to perform another option, the plugin uses the right-click
menu for the rest of the call and says so in `tried`. When no part of the target is on screen it
turns the camera toward the target and keeps trying for up to six seconds.

Only one call drives the mouse and the keyboard at a time. `client_interact`, `client_click`,
`client_type` and a `client_camera` call that turns the camera take turns, and the calls that only
read run beside them.

The call first checks that the client has the target and that the target offers the option, and
fails with `not_found` when it does not. It then waits for the packet that the client sends and
returns its line as `packet`, with `attempts`, the number of aims it took, `tick`, the client's tick
count when the click went through, `tried`, the last attempts in words, and `camera` when it turned
the camera. `proof` says how the packet was tied to this call. Packets reach the log up to a tick
late, so a packet that an earlier action caused can arrive after the call began. The server therefore
first finds the client's own `EVENT_MOUSE_CLICK` record of the press, by its canvas coordinates, and
takes the first matching packet after it. `proof` is then `click`. When no such record arrives within
three seconds, it takes the first matching packet after the cursor, and `proof` is `prefix`.

```
1. session_start {"target":"My Server"}
   -> {"session":"s1","kind":"launched","target":"My Server","state":"connected", ...}

2. client_login {"username":"mcptest","password":"any"}
   -> {"gameState":"LOGGED_IN","cursor":2}

3. client_entities {"kinds":["npc"],"name":"guide"}
   -> {"origin":{"x":3094,"y":3107,"plane":0},"npcs":[{"index":0,"id":3308,"name":"Gielinor Guide",
       "x":3094,"y":3109,"options":["Talk-to"]}],"objects":[],"ground_items":[],"players":[],
       "truncated":false,"cursor":612}

4. client_interact {"target":"npc","index":0,"option":"Talk-to"}
   -> {"target":"npc","index":0,"id":3308,"name":"Gielinor Guide","option":"Talk-to",
       "attempts":1,"tick":41,"tried":["left-clicked Talk-to Gielinor Guide at (312, 171); the client
       performed Talk-to Gielinor Guide"],"cursor":613,
       "packet":"617 L1 T41 C OPNPC1_V2 [opnpc1_v2] npc=(index=0, ...)","proof":"click"}
```

The `packet` in step 4 is the proof that the client sent the action. The two packets before it are
the mouse click and the mouse movement that the client sends for any click. When no such packet
arrives within three seconds for a target in the game world, the call fails, because the client
dropped the action without a word. The error names the likely cause, such as an NPC or a player that
left the client's view. A widget is different, because the client handles some widget options by
itself and sends nothing for them. The call then succeeds with `"packet":null` and a `note` that says
so. A login that the proxy has no decoder for has no packets in the log at all, so every interaction
of that login returns `"packet":null` with a `note` that it could not be confirmed. What the server
answered is in `packets_read {"after":613}`.

Walking is the same interaction with the option `Walk here` on a tile:
`client_interact {"target":"tile","x":3096,"y":3107}` clicks the tile and is proven by the
`MOVE_GAMECLICK` packet for it. A dialog is driven through its widgets:
`{"target":"dialog","option":"continue"}` clicks the widget that continues it, and
`{"target":"dialog","option":"2"}` or `{"target":"dialog","option":"Yes"}` clicks a choice by its
number or its text. Both are proven by `RESUME_PAUSEBUTTON`. A button of an interface is proven by its
`IF_BUTTON` packet, and a close button by `CLOSE_MODAL`.

When the client never offers the option within the deadline, about three ticks, the call fails with
`wrong_state` and says what the client offered at the last attempt instead, such as `the client
offered [Walk here, Examine Tree]`.

The client offers only Cancel at a point that an interface covers, such as the chatbox, the minimap
or the inventory. The tool never clicks there. After a few such points it turns the camera, which
moves the target to another part of the screen. If that does not help, the call fails with `the
client offers only Cancel; an interface covers the game view there`. An interface that blocks the
whole game view, such as the bank, gives the same message; close it first.

A target that is hidden from every angle, behind a wall for instance, cannot be clicked, and neither
can anything in a secondary world view such as a boat: both tools read only the top-level world
view, on the plane of the local player.

`client_camera` turns the camera on purpose: pass `yaw` and `pitch` in the client's units, or
`look_at` with the target fields of `client_interact`, and it returns the settled `yaw` and `pitch`
and whether the target is on screen. With no arguments it reads them.

`client_click` is the unguarded low-level fallback: it clicks a canvas position or the centre of a
widget, and the client performs whatever is under the mouse at that moment. Use it for positions on an
interface that `client_interact` cannot name, and `client_type` for typed input.

## Example: check that setting a display name works

The display-name interface is group 558. Its name field is widget `558:12`. `558:12` is the text
shown in the field, and it sits on top of `558:7`, the component that receives the click, so the
packet in step 5 reports `558:7`.

```
1. session_start {"target":"My Server"}
   -> {"session":"s1","kind":"launched","target":"My Server","state":"connected","generation":1,
       "proxyPort":43751,"httpPort":43650,"pid":40388,"cursor":2}

2. client_login {"username":"mcptest","password":"any"}
   -> {"gameState":"LOGGED_IN","cursor":2}

3. client_widgets {"group":558}
   -> {"roots":[...],"widgets":[..., {"id":"558:12","text":"*","bounds":[467,350,332,22],
       "click":[633,361], ...}, ...],"truncated":false,"cursor":470}

4. client_click {"widget":"558:12"}
   -> {"x":633,"y":361,"cursor":471}

5. packets_read {"after":471,"prots":["IF_BUTTONX"],"wait_ms":3000}
   -> {"next":473,"head":473,"dropped":0,"timedOut":false,"count":1}
      473 L1 T5 C IF_BUTTONX [if_buttonx] com=tutorial_displayname:name (558:7), op=1

6. client_type {"text":"McpProto","enter":true}
   -> {"typed":8,"cursor":484}

7. packets_read {"after":484,"prots":["IF_SETTEXT"],"contains":"McpProto","wait_ms":5000}
   -> {"next":519,"head":519,"dropped":0,"timedOut":false,"count":1}
      519 L1 T8 S IF_SETTEXT [if_settext] com=tutorial_displayname:status (558:13), text="Great! The
          display name <col=ffffff>McpProto</col> is <col=00ff00>available</col>!<br>You may set this
          name now, or enter another to look up."

8. client_widgets {"group":558,"text":"available"}
   -> {"roots":[...],"widgets":[{"id":"558:13","text":"Great! The display name <col=ffffff>McpProto</col>
       is <col=00ff00>available</col>!<br>You may set this name now, or enter another to look up.", ...}],
       "truncated":false,"cursor":520}

9. session_stop {}
   -> {"session":"s1","kind":"launched","target":"My Server","state":"stopped",
       "reason":"stopped by caller", ...}
```

Steps 5, 7 and 8 are three separate proofs. The client sent the click. The server answered the name
the client sent. The client rendered the answer.

A wait returns as soon as one packet matches. The name itself left the client two ticks earlier, as
`RESUME_P_STRINGDIALOG [resume_p_stringdialog] string="McpProto"`, and
`packets_read {"after":484,"origin":"client"}` shows it.

The same wait proves that nothing happened. Wait for the packet and expect a timeout:
`packets_read {"after":520,"prots":["IF_SETTEXT"],"wait_ms":1800}` returning `"timedOut":true` means
the server sent no such packet for three ticks.

After a server restart, call `session_start {"session":"s1"}`. The session keeps its id and its packet
log. The client comes back on new ports, and new packets are marked with the next login number.

## Check it against a live client

The automated tests cover the packet log, the endpoint's HTTP and JSON-RPC handling, the tool calls
as an MCP client makes them, the session failure paths, the attached sessions and the bridge handshake. They do not run a
game client, so what the client does is checked by hand. Run this after a RuneLite update, a game
revision change, or a change to the plugin. Each step names what proves it, so a step that fails
names what changed. A tool that reports a dropped action, or lists what the client offered instead,
is working as intended. Read its message before suspecting the tool.

Start the server and a session against a target you can log in to, with a new account:

1. `session_start` returns `"state":"connected"` with a `pid`, and that pid is the client process.
2. `client_login` returns `"gameState":"LOGGED_IN"`, and `packets_read {"origin":"proxy"}` shows a
   `LOGIN` row. `packets_read {"limit":20}` shows packets of tick 0, with named values.
3. `client_screenshot` returns an image whose width and height equal `canvas` in `client_state`.
4. `client_interact` on an interface button returns a `packet` line and `"proof":"click"`.
   `packets_read` from the returned cursor shows an `EVENT_MOUSE_CLICK` row before the button packet,
   so the click was a real one.
5. `client_type` into a prompt is followed by a packet that carries the typed text.
6. `client_entities` lists the NPCs and objects that the screenshot shows, with their options.
7. `client_interact` with the default option of an NPC that walks around returns its `OPNPC` packet.
   The `attempts` count stays low. Repeat it a few times.
8. `client_interact` with an option that is not the default, such as `Examine`, goes through the
   right-click menu: the last entry of `tried` starts with `clicked row`.
9. `client_interact` on an object and on a tile each return their packet, and after the walk
   `client_state` shows the player on that tile.
10. `client_interact {"target":"dialog","option":"continue"}` advances a conversation.
11. With an interface open that blocks the game view, `client_interact` on an NPC fails and says that
    the client offers only Cancel.
12. `client_camera {"look_at":"npc","index":0}` returns `"onScreen":true`, and the next screenshot
    shows the turned view.
13. Killing the client process makes `session_list` show `stopped` with the reason `client exited`,
    and `session_start` with the same `session` brings it back with the earlier packets still readable.

Then check the attached sessions. Stop the standalone server, start the GUI with `./gradlew proxy`
and launch a client from it by hand:

1. `session_list` shows a session with `"kind":"attached"`, the target and the proxy port of the
   client, and no `login`.
2. After you log in, `session_list` shows `login` with the world, the capture file and, a tick or two
   later, your display name and a `tick` that advances. `packets_read` on the session returns the
   packets that the GUI's own panel shows.
3. The GUI's panel shows the same packets as it does with `mcp.enabled=false`.
4. `session_stop` and `client_state` on the session fail with `not available for an attached
   session`, and the client keeps running.
5. Closing the client window makes `session_list` show `"state":"ended"`. So does closing the tab of
   a second client. Both sessions stay listed with their packets.
6. With the standalone server running first, the GUI starts as usual and logs that the endpoint is
   not served.
