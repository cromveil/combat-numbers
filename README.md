# Combat Numbers

Fabric mod to show damage numbers, compatible with Minecraft 26.2.

Started off as part of a bigger project in an older version of MC and later abandoned, just like all my other projects. I like RPGs, but after trying most RPG modpacks, I came to the disappointing conclusion there's a lack of extravagrant Maplestory-style customizable damage numbers that nobody else other than me is going to ask for.

Has to be installed on the server side for accurate detection and numbers, without it client side can only work with consolidated health deltas. If you're looking for a purely client sided damage number mod, there are plenty of others that work just fine and you won't need something like this at all. But if you also like the idea of having damage skins, I do have plans to try and get a "client-only mode" going, it will fall back to using the not-so-accurate health deltas but hey at least you get to see pretty numbers.

## Features

- [x] Damage detection
- [x] Heal detection
- [ ] Absorption damage detection
- [ ] Shield block detection

## Datapack

A lot of things in this mod is data-driven and can be customized with a datapack. Documentation is still TODO but you can just look into `src/main/resources/data` to see how to customize it. I might rejig the schema a lot and really don't feel like rewriting this section every time something changes.

## TODO

- [ ] Actually use sprites instead of vanilla font to really show what this can do
- [ ] Animations could really use some more work 
- [ ] Backport to 26.1 - 26.1.2
- [ ] Try backporting to 1.20.1 - 1.21.11
- [ ] Support for other mod loaders (Forge, NeoForge, etc)
- [ ] Test out compatibility with other mods
- [ ] Test out compatibility with shaders
- [ ] Client-only mode (won't have accurate numbers though)
