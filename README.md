# Functional Stoves — Six-Slot Cooking Grate (experimental)

This **feature branch** produces a separate Forge 1.20.1 addon for the user-supplied
**Functional Stoves 1.0-SNAPSHOT** JAR. It does not reproduce, redistribute, or modify
the original mod's assets or classes. Keep the original JAR installed; install the JAR
built from this branch alongside it.

## Feature

- Place a thin cast-iron grate in the air block directly **above** an original
  `functional_stoves:large_stove`. The original stove remains unchanged.
- Six separate slots visibly render the food on the grate.
- Right-click with a campfire-cookable ingredient to place one item in the next available slot.
- Items cook only while the underlying stove's LIT property is true.
- When a slot finishes, the rendered stack becomes the recipe output and remains there.
- Right-click with an **empty hand** to take the first cooked slot.
- Cooked results are tagged with `CozyGrillBonus`; eating them gives 3 additional
  hunger points and 3 additional saturation points.
- Inputs and outputs are persisted in the block entity; breaking the grate drops its contents.
- Crafting recipe: 6 iron ingots + 2 iron bars.

## Build

Minecraft 1.20.1 / Forge 47.4.0 / Java 17. GitHub Actions builds the branch from a pull request and uploads the resulting JAR under workflow artifacts.

## Known limits / testing

- This is an addon, **not a replacement for Functional Stoves**. Do not remove the
  original Functional Stoves JAR.
- The original mod's Large Stove is a multi-part block; check the position of the grate
  and rendering against both large-stove halves in-game.
- This feature is work in progress until tested on a client and dedicated server.
- The original repository's unused placeholder stove assets remain in the source tree,
  but the addon does not register the placeholder blocks.
