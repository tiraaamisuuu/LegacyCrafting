# LegacyCrafting

LegacyCrafting is a client-side Fabric mod that replaces Minecraft's inventory
and crafting-table crafting screens with a fast, category-driven recipe menu.
Browse known recipes, see ingredient availability, and craft without manually
arranging the 2×2 or 3×3 grid.

The interface is inspired by the usability of Minecraft Legacy Console
Edition's crafting menu. It is an independent implementation and does not
bundle Legacy4J or assets extracted from Minecraft Legacy Console Edition. The
MIT-licensed navigation sound and controller prompts adapted from the Old UI
for Legacy4J resource pack are credited in
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

## Features

- Seven classic recipe categories: Structures, Tools & Weapons, Food, Armour,
  Mechanisms, Transportation, and Decorations
- Legacy Console-style horizontal recipe strip with mouse and keyboard navigation
- Original Legacy-style focus audio and Xbox 360 controller prompts
- Large recipe icons, output quantities, ingredient previews, and tooltips
- Clear visual distinction between craftable and unavailable recipes
- All Recipes and Craftable filtering
- Correct 2×2 and 3×3 recipe filtering
- Dynamic recipe discovery from the client's unlocked recipe book
- Server-authoritative Craft Once and Craft Maximum actions
- Persistent toggle for returning to the vanilla crafting screens

Recipe discovery is not hard-coded. Vanilla, datapack, and compatible modded
crafting recipe displays sent to the client can appear automatically.

## Requirements

| Dependency | Version |
| --- | --- |
| Minecraft Java Edition | 26.2 |
| Fabric Loader | 0.19.3 or newer |
| Fabric API | 0.157.0+26.2 or newer 26.2 build |
| Java | 25 |

Fabric API is required on the client. No mod, plugin, or Fabric installation is
required on the server.

## Installation

1. Install Fabric Loader for Minecraft 26.2.
2. Install Fabric API for Minecraft 26.2.
3. Copy the LegacyCrafting JAR and Fabric API JAR into the client's `mods`
   directory.
4. Start Minecraft with the Fabric profile.

## Controls

- Left click a recipe: craft once.
- Shift + left click a recipe: craft as many as possible.
- Mouse wheel: scroll recipes.
- Arrow keys: navigate a focused recipe grid or category bar.
- Enter or Space: craft the focused recipe.
- `A`: craft the selected recipe once.
- `Y`: craft the selected recipe as many times as possible.
- `X`: toggle Craftable filtering.
- `B`: close the crafting menu.
- `L`: toggle the Legacy crafting interface on or off.
- Right click: reserved for a future recipe action.

The toggle is stored in `config/legacycrafting.json`. Its key can be rebound in
Minecraft's Controls menu.

## Multiplayer and Paper compatibility

LegacyCrafting uses only interactions understood by an unmodified vanilla or
Paper server. Recipe selection sends Minecraft's standard recipe-placement
request. The mod waits for the server-populated crafting result and then uses a
normal result-slot quick-move interaction. It never creates items or directly
changes authoritative inventory state.

The current build has been smoke-tested by joining a local Paper 26.2 build 112
server with zero plugins and completing a 2×2 crafting action. Paper remained
authoritative over ingredient consumption and the crafted result.

## How crafting is planned

`RecipeCraftabilityService` uses Minecraft's recipe-book ingredient data and
`StackedItemContents` for the same tag and alternative matching semantics used
by vanilla. `IngredientAllocator` separately creates immutable plans for
repeated ingredients and shared alternatives. `CraftExecutor` does not execute
raw plan transfers; it delegates placement, crafting remainders, container
items, and result validation to the vanilla server workflow.

Craftability is refreshed when inventory/menu state changes or the client
receives a recipe update. Recipes are not re-evaluated every rendered frame.

## Current limitations

- Only shaped and shapeless crafting recipe displays are supported. Special
  recipes that do not provide client-side crafting requirements are shown as
  unavailable.
- Categories beyond Minecraft's broad recipe-book categories use conservative
  output-item heuristics and may need refinement for modded items.
- Controller input is not implemented yet, although tabs and recipe selection
  are separate focusable widgets for future controller navigation.
- The supplied old navigation pack contains only the focus cue. Craft success
  uses Minecraft's item-pickup sound; back and failure cues currently use
  pitched vanilla UI sounds until independently licensed replacements are
  available.
- Shift crafting is capped at 64 placement batches as a safety guard.

## Building from source

Clone the repository and run:

```sh
./gradlew build
```

The Gradle wrapper downloads the required build tooling. A Java 25 JDK must be
available. The remapped mod JAR is written to `build/libs/legacycrafting-0.1.0.jar`.

Run the logic tests with:

```sh
./gradlew test
```

For a development client:

```sh
./gradlew runClient
```

## Contributing

Issues and focused pull requests are welcome. Please run `./gradlew build`
before submitting changes and keep version-specific Minecraft integrations
small and isolated.

## License

LegacyCrafting is available under the MIT License. See [LICENSE](LICENSE).
