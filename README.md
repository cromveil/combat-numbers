# Combat Numbers

A mod dedicated to displaying fancy damage numbers.

## Features

**Built-in themes** - Opinionated default styling. Currently only the "basic" and "maple" themes are available.

**Damage Types** - Supports showing different styles based on damage type.

**Heal Types** - Supports different healing sources.

**Critical Hits** - Support for vanilla critical hits.

**Fully customizable** - Styling, skins and animations can be modified via datapacks and resource packs.

## Installation

This is a client + server mod, meaning it has to be installed on both client and server side for dedicated servers. Works without any additional setup on single player.

This mod relies on server hooks to figure out the actual damage amount and type. Without it, a vanilla client only receives health deltas from the server and there's a limit to how much we can do with that info.

### Compatibility

Combat Numbers should be compatible with most mods.

Works with Iris shaders, just make sure the mod's Render Mode config is set to "HUD" (default).

# Motivation

Started off as a toy project in an older version of MC and later abandoned like 90% of my other projects. I like RPG modpacks, but sadly I came to the disappointing conclusion there's a lack of extravagrant Maplestory-style customizable damage numbers that nobody else other than me is going to ask for.

## Future Plans

- Backports are planned because, well, there's not really much RPG packs on 26.2 right now.
- I do have plans to try and get a "client-only mode" going, it will fall back to using the not-so-accurate health deltas but hey at least you get to see pretty numbers.
- Contributions are welcome, especially additional themes. I am no artist, just a guy whose neuron activates whenever he sees pretty damage numbers.

# Customizing

Combat Numbers is largely data-driven.

- To change damage skins and animations, pick a built-in theme or a custom resource pack (guide below). This only affects your own game client.
- To control what style to show for each damage type, create or use a datapack.

## Default Themes

Default themes are bundled into the mod. You can switch themes via the mod config (directly in NeoForge, or through Mod Menu in Fabric).

Available options:

- `basic`, damage indicators using the default Minecraft font
- `maple`, MapleStory aesthetic

## Resource Packs

You can override skins and animations with a resource pack. Make a normal resource pack and add files under `assets/combatnumbers/`:

```
assets/combatnumbers/skins/<id>.json
assets/combatnumbers/animations/<id>.json
assets/combatnumbers/textures/<file>.png     <-- put images for sprite-based skins here
```

For examples, look at GitHub sources for themes.

Resource packs are prioritized over the built-in themes and layered on top.

### Skins

A skin is either recolored text or your own sprites.

#### Regular text

```json
{
  "type": "text",
  "settings": {
    "fill_color": "0xFFFFAA00",
    "outline_color": "0xFF000000",
    "scale": 1.0
  }
}
```

Colors are ARGB hex. `outline_color` and `scale` are optional.

#### Sprites

```json
{
  "type": "sprite",
  "settings": {
    "texture": "combatnumbers:my_numbers",
    "columns": 10,
    "cell_width": 16,
    "cell_height": 20,
    "char_order": "0123456789",
    "scale": 1.0
  }
}
```

For more info, look at GitHub sources.

### Animations

An animation is a list of steps. Each step nudges one or more channels (`opacity`, `scale`, `x`, `y`, `rotation`) over a time window.

```json
[
  {
    "start": 0,
    "duration": 200,
    "channels": {
      "opacity": { "type": "keyframe", "values": [0, 1], "easing": "ease_out" }
    }
  },
  {
    "start": 700,
    "duration": 300,
    "channels": {
      "opacity": { "type": "tween", "to": 0, "easing": "ease_out_circ" }
    }
  }
]
```

For more info, look at GitHub sources.

## Datapacks

A datapack can also double as a resource pack. They take priority over any built-in theme or resource pack, so a server can force a theme on all clients.

The main purpose of a datapack though, is to control styling rules.

A rule has a `when` (what to match) and a `then` (which skin and animation to use):

```json
{
  "rules": [
    {
      "when": {},
      "then": {
        "skin": "combatnumbers:physical",
        "animation": "combatnumbers:generic"
      }
    },
    {
      "when": { "tags": ["minecraft:is_fire"] },
      "then": { "skin": "combatnumbers:fire" }
    },
    {
      "when": { "flags": ["combatnumbers:crit"] },
      "then": {
        "skin": "combatnumbers:physical_crit",
        "animation": "combatnumbers:wave"
      }
    }
  ]
}
```

### Example: Damage Rules

Match by damage tag, which groups many sources at once (all fire, all projectiles, and
so on):

```json
{
  "when": { "tags": ["minecraft:is_projectile"] },
  "then": { "skin": "combatnumbers:physical" }
}
```

Match by exact damage type, for one specific cause:

```json
{
  "when": { "type": "minecraft:lightning_bolt" },
  "then": { "skin": "combatnumbers:lightning" }
}
```

For the full list of damage types and tags, see the Minecraft Wiki pages on [damage types](https://minecraft.wiki/w/Damage_type) and [tags](https://minecraft.wiki/w/Damage_type_tag_%28Java_Edition%29).

To match by who or what was involved, using vanilla [predicates](https://minecraft.wiki/w/Predicate) for `target`, `attacker`, or `weapon`:

```json
{
  "when": { "target": { "type": "minecraft:ender_dragon" } },
  "then": { "skin": "mypack:boss_numbers" }
}
```

### Example: Heal Rules

Minecraft does not label heals the way it labels damage. The mod works out the cause itself and lets you match it in `type` on a heal rule:

- `combatnumbers:natural_regen`, the slow regeneration from food.
- `combatnumbers:regen_effect`, the Regeneration status effect.
- `combatnumbers:instant_health`, Instant Health potions and similar.
- `combatnumbers:generic_heal`, anything else (commands, other mods, and so on).

### Example: Filter Rules

Filters drop events before they show. They live at `data/combatnumbers/filters/<file>.json`, each a list of conditions that use the same fields as a rule's `when`. Anything matching any condition is hidden. The shipped default hides passive regeneration:

```json
[
  { "type": "combatnumbers:natural_regen" },
  { "type": "combatnumbers:regen_effect" }
]
```

To also hide all fall damage, for example:

```json
[
    { "tags": ["minecraft:is_fall"] }
]
```
