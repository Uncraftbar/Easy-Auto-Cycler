# v3.0.0 - Faster, Seamless Trade Cycling

## Changes

- Cycling now automatically runs as fast as the server and connection permit.
- Removed the cycling speed setting; no manual delay tuning is needed anymore.
- Keeps one cycle request in flight and checks every returned set of trades before continuing.
- Cycling and configuration buttons now stay hidden whenever trade cycling is unavailable.
- Restored the scrollable filter list on Minecraft 1.21.11.
