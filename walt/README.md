# World Aliasing Loopback Tool (`WALT`, macOS Only)

This module contains a simple utility for macOS users that enables the RSProx loopback mechanism to function properly. If you are **not** a macOS user - thanks for stopping by. If you are, keep reading!


## Motivation
In legacy releases, macOS users would have to manually manage and run this script:

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

`WALT` puts some formality around this script and exposes it through an easy-to-use and well-documented
CLI.

## Installation

`WALT` can be installed via homebrew.

> homebrew instructions later.