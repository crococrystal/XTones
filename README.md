# XTones

Decorative building blocks with **in-hand scrolling**, for **Minecraft 1.21.1 / NeoForge**.

Development fork of [XTones Reworked](https://github.com/lays24mc/XTones-Reworked) that restores variant scrolling and adds switching between block families. No release of this fork has been published yet.

## Controls

Hold an XTones block in your main hand:

| Shortcut | Action |
| --- | --- |
| **Alt/Option or Command + mouse wheel** | Cycle variants of the held block |
| **Shift + Alt/Option or Command + mouse wheel** | Cycle block families |

Both left and right modifier keys work. Scroll up moves forward; scroll down moves backward. Lists wrap. Changing families keeps the variant number when available, otherwise it uses the first variant. The base tile and flat lamp are included as single-variant families.

Conversions are **free in survival and creative**. The whole held stack changes while keeping its count and item components, including custom names. Recipes and the stonecutter remain available. Scrolling without a modifier, or while holding another mod's item, keeps normal Minecraft behavior. Menus and spectator mode do not convert blocks.

## Compatibility

Minecraft **1.21.1**, Java **21**, NeoForge **21.1.209 or newer within 21.1**. Multiplayer scrolling requires this fork on both client and server.

**Replace XTones Reworked; do not install both.** The registry ID remains `xtonesreworked` to preserve existing block IDs and worlds.

Scrolling targets **1.21.1 only**. Other branches inherited from upstream retain their original code and do not include this feature.

## Build and check

```sh
./gradlew build
./gradlew runGameTestServer
```

The JAR is written to `build/libs`. Server GameTests check wrapping, count and component preservation, and rejected invalid requests. Test code is excluded from the mod JAR.

For an in-game check, use `./gradlew runClient`, hold an XTones block and try both shortcuts in both directions. Also check an ordinary item, an inventory menu, and a multiplayer server with the fork installed.

## Credits and license

- [TobsenD / lays24mc](https://github.com/lays24mc): XTones Reworked, the base for this fork.
- [TehNut](https://github.com/TehNut/Xtones): original XTones. Variant scrolling existed in the original; Reworked replaced it with recipes.
- **riciJak**: original textures, retained from upstream. The original project's [artwork attribution](https://github.com/TehNut/Xtones#readme) states that its art is owned by riciJak and used with permission.
- [Wheelz0](https://github.com/Wheelz0): earlier 1.16.5 port.
- **crococrystal**: this fork and its block-family scrolling feature.

The upstream [MIT license](LICENSE) and copyright notice are preserved and included in the JAR. This is an independent continuation, not an official release by the original authors.
