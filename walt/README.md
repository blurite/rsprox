# World Aliasing Loopback Tool (`WALT`, macOS Only)

This module contains a simple utility for macOS users that enables the RSProx loopback mechanism to function properly.  

If you are **not** a macOS user — thanks for stopping by. If you are, keep reading!

## Motivation

In the current release, macOS users have to manually manage and run a shell script to alias ranges of loopback addresses:

```bash
#!/bin/bash
set -euo pipefail

# === Config ====================================================
MIN_WORLD_ID=300      # Minimum world id to whitelist, inclusive.
MAX_WORLD_ID=650      # Maximum world id to whitelist, inclusive.
GROUP_ID=2            # Proxy target (2 is Oldschool, 3 is first custom, etc).
MODE=+                # "+" to whitelist, "-" to un-whitelist
# ===============================================================

# === some sudo ifconfig stuff ... ==============================
...

echo "Alias IPs added for worlds $MIN_WORLD_ID..$MAX_WORLD_ID (group $GROUP_ID)."
```

`WALT` puts formality around this script and exposes it through a well-documented CLI with
support for add/remove, status, sync, and clear operations. 

It also maintains a **registry file** which lives
at your `XDG_STATE_HOME` location if you have this environment variable set, or at `~/.local/state/walt/aliases.txt`, and tracks loopback aliases you want to have set. Upon reboot, you can sync your `lo0` network interface with the registry file (see [Usage section](#usage)).

## Requirements and Installation

Make sure you have [Xcode 16.4 or higher](https://xcodereleases.com/) and [Homebrew](https://brew.sh/) installed.

Since `WALT` is not part of the v1.0 release, you will need to tap this repo and install the development build.

```bash
brew tap blurite/rsprox https://github.com/blurite/rsprox.git
brew install --HEAD blurite/rsprox/walt
```

## Usage

**NOTE**: `walt` requires `sudo` to run - most of these commands will prompt you for your `sudo` password if you have one.

```bash
walt --help
```

#### Common commands:

```bash
# alias a single world loopback address...
walt add --min=300 --max=300 --group=2
# ... or a world range
walt add --min=255 --max=258 --group=3

# remove those aliases
walt remove --min=255 --max=258 --group=3

# sync your registry file with lo0 (useful after system reboots)
walt sync

# show all tracked loopbacks (in the registry and on lo0)
# those two should be equivalent! if they're not, use `walt sync`
walt status

# clear all aliases entirely
walt clear
```

## Reporting Issues

If you find issues or have ideas for improvement, open a PR or discussion. Bug reports
specific to Homebrew installation should include your `brew doctor` output.