# SOF Menu (Minecraft 1.20.1 Forge)

Client-side menu overhaul for the **SOF** (Survival - Origin & Future) modpack, ported
from the Minecraft 1.12.2 build to **Minecraft 1.20.1 / Forge 47.x**.

## Features

- **Custom main menu** - slideshow background, SOF logo, textured fade-in buttons
  (Single Player / Multiplayer / Options / Quit / Info).
- **Info book screen** - book layout with decorative stickers, links to the
  official website & GitHub, and the developer credits.
- **Credits screen** - glass panel with active / former developer lists.
- **Loading screens** - the bundled 34-frame artwork with blurred cross-fades is
  shown during resource loading, world loading, terrain download and connecting.
- **Window customization** - custom window title and icon.
- **Texture preloading** - menu / loading textures are decoded ahead of time so
  screens never hitch.
- **Last session store** - remembers the last server joined.

## Building

Requirements: JDK 17 and Gradle 8.1.1 (the wrapper is included).

```bat
gradlew.bat build
```

The built jar lands in `build/libs/`.

## Running

```bat
gradlew.bat runClient
```

## Project layout

- `src/main/java/com/canoestudios/sofmenu/` - the mod source.
  - `client/gui/` - main menu, info book, credits, textured buttons.
  - `client/loading/` - loading artwork, loading overlay and screen wrappers.
  - `client/resources/` - texture preload cache.
  - `client/session/` - last session store.
  - `client/window/` - window title / icon.
- `src/main/resources/` - `mods.toml`, language files, textures and sounds.

## Notes

- The loading overlay and connection/world-load screens are replaced through
  Forge events (`ScreenEvent.Opening` + the client tick) instead of a coremod,
  which is how the 1.12.2 build implemented them.
- Private vanilla fields (e.g. `ConnectScreen.connection`) are read via Forge's
  SRG-aware `ObfuscationReflectionHelper`, so the mod keeps working in the
  reobfuscated production environment.

## License

MIT - see [LICENSE](LICENSE).
