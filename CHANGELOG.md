
# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased] - yyyy-mm-dd

Here we write upgrading notes for brands. It's a team effort to make them as
straightforward as possible.

### Added
- [LMR-ZZZZ]()
  Template line.

### Changed

### Fixed


## [2.0.5] - 2024-03-12

Land Manager Reloaded 2.0.5 is a patch release that includes the following changes:

### Fixed

- Fixed `maxAreasCanOwn` setting in the Server Configuration API. This setting was not being respected, player could still create areas, even if they owned the maximum amount and deletion of an area wouldn't properly decrease the number of areas owned by the player.

## [2.0.4] - 2024-03-06

Land Manager Reloaded 2.0.4 is a patch release that includes the following changes:

### Added

- Added `maxAreasCanOwn` to the Server Configuration API. This allows the server to limit the number of areas a player can own. You can find this setting in `world/serverconfig/lmr-server.toml`
- Added `LeftClickBlock` event to area protection. Mostly to prevent removing items from Drawers in the `Storage Drawers` mod.
- Added `AttackEntity` event for `HangingEntity` to area protection. Mostly to prevent breaking item frames and paintings in protected areas.
- Added the `disableAutoClaiming` setting define if a player needs to claim an area or not after creation. If `disableAutoClaiming` is set to `true`, the player will need to claim the area after creation with `/lmr claim <areaName>`.

### Changed

- Claim requests can't be validated if the player has reached the maximum number of areas they can own.

### Fixed

- Fixed an issue where the setting `maxAreaCapacity` in the Server Configuration API was not being respected. This setting is now respected and will limit the number of players that can join the same area.
- Fixed an issue where anyone could delete areas, now you need to modification rights to delete an area.


