# Jauml v2.1.1

### New Features
* None

### Improvements
* MultiLoader publish workflow now builds every workspace (1.20.1 / 1.21.1 / 1.21.11 / 26.1.2 / 26.2) and uploads all loader jars.

### Bug Fixes
* Fixed Fabric and NeoForge **26.1.2** crash: `MixinMinecraft` was a stub class listed in mixin configs but missing `@Mixin` (`InvalidMixinException`). Affects users still on **1.3.0** (issues [#3](https://github.com/MeherBenSalem/jauml/issues/3), [#4](https://github.com/MeherBenSalem/jauml/issues/4)). The broken stub and mixin JSON entries are removed; `"mixins": []`.

### Configuration
* None

### Compatibility
* Shared version **2.1.1** across all MultiLoader workspaces.
* Loaders: Fabric, Forge (1.20.1), NeoForge (1.21.1+).
* Drop-in update from 2.1.0 / 2.0.0 / 1.3.0.

### Upgrade Notes
1. Replace any `jauml-*-1.3.0.jar` (or older) with the matching `jauml-*-2.1.1.jar` for your loader and Minecraft version.
2. Restart the client or server.
3. Confirm the game loads without `missing an @Mixin annotation` for `tn.naizo.jauml.mixin.MixinMinecraft`.
