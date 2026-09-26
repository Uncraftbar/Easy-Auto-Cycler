# 3.1.2 release scope audit — 2026-09-26

Previous publications were verified against GitHub Actions checkout logs and the
public Modrinth version listing, not inferred from branch version strings.

- 3.1.0 successful publishing run: `30704468308` (2026-08-01).
- Fabric 1.20.1-only 3.1.1 successful publishing run: `32033918560` (2026-08-17).
- Latest public release before this operation: `v3.1.1`.

## Changed targets: publish 3.1.2

| Branch | Last published source | New change |
|---|---|---|
| fabric-1.21.11 | aa257e728c6bf0c4d1ce1987296525f65948df4a | PR #34 category translation |
| neoforge-1.21.11 | 2ec94013c5188c534da468920de63c57c4c709a8 | PR #35 category translation |
| fabric-26.1.2 | 968ec9cdd76e1f2ebfe71a2c362ad10b4e1ccc82 | PR #36 category translation |
| neoforge-26.1.2 | da0f94b294ca9952f2423df4711385822ee96d52 | PR #37 category translation |
| fabric-26.2 | 08d3ac1d5881e5f97e88ef36b12b1c73fd43a733 | PR #38 category translation |
| neoforge-26.2 | b471419dc28e1c1c51b6672c1d8ecbe47ff82ba0 | PR #39 category translation |
| fabric-26.3 | never published | New tested port |
| neoforge-26.3 | never published | New tested port |

The six existing targets have only the category-fix PRs since their last
publication, plus the release version bump. The maintainer approved the 26.3
ports after testing cycling and the corrected autocomplete in desktop clients.

## Unchanged targets: no commits, pushes, or new files

| Branch | Current = last published source | Keep version |
|---|---|---|
| fabric-1.20.1 | 2858a65 | 3.1.1 |
| forge-1.20.1 | f2056bfff83c6039ab17bb6d528bc1a13e94aced | 3.1.0 |
| fabric-1.21.1 | 9af0bf6a47bb639e6ed447c9f3c4e0478dbe5f41 | 3.1.0 |
| neoforge-1.21.1 | 9d1119267e28a60346c8a1dd2f432d0cd1b2c95a | 3.1.0 |

Use workflow target `release-3.1.2`, version `3.1.2`, destination `all`.
Dry-run first, then publish the same eight targets to CurseForge, Modrinth,
and a shared GitHub `v3.1.2` release. Never use `all` for this selective update.
