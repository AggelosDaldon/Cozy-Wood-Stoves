# Functional Stoves — Unified Cooking Grate

**Forge 1.20.1 / Java 17 · source integration branch**

This branch integrates a six-slot cooking grate into the **original** Functional Stoves mod.
Only one Forge mod ID is used: `functional_stoves`. It preserves the original
`io.github.samispoggers.functional_stoves` classes, textures, animations, models,
recipes, and original `META-INF/mods.toml`. It is NOT the earlier two-mod addon.

## Six-slot grate
- Place a metal grate in the block immediately above either part of the Large Stove.
- Insert up to six foods by right-clicking with campfire-cookable ingredients.
- All six items appear on the grate and cook when the corresponding stove part is lit.
- Cooked foods remain visibly on the grate until taken with an **empty-hand right click**.
- Outputs receive a three-point bonus to hunger and saturation when eaten.
- Ingredients, output state and cooking progress persist in block entity NBT.
- Iron-ingot and iron-bar crafting recipe, with a ridged grate block model.

## Single-JAR assembly

The original `functional_stoves-1.0-SNAPSHOT.jar` binary is **not checked into this
public repository** because it was authored by another creator and carries an
All Rights Reserved license. It is available only from the user's original upload.

To build one combined mod locally:
1. Put the original JAR at `original/functional_stoves-1.0-SNAPSHOT.jar`.
2. Run `gradle jar` with Java 17 and Gradle 8.8.
3. Install the output `build/libs/functional_stoves-1.0-SNAPSHOT-with-grill.jar`
   **instead of** the original mod, not alongside it.

The equivalent locally merged JAR can be produced using the helper provided
alongside the original chat attachment. The compiled patch is pinned at
`dist/grill-compiled-patch.b64`.

The GitHub Actions job checks source compilation. It **does not** claim a
functional single-mod artifact when the original JAR isn't present.

## Status

Forge source compiles successfully; **client, dedicated-server and gameplay
runtime testing are still pending**. Keep this PR draft until tested. In particular,
the original Large Stove is two-part, and heat/range should be verified in game.
