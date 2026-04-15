# NeoForge 26.1.2 Port — Status

**Status: WORK IN PROGRESS — does not yet compile.**

## What's done

- **Build system** updated to target MC 26.1.2 / NeoForge 26.1.2.11-beta / Java 25
  - Uses NeoGradle 7.1.22 (plugin `net.neoforged.gradle.userdev`)
  - `gradle.properties`: new version coordinates, externalized CurseForge file IDs
  - `settings.gradle`: Foojay resolver 1.0.0
  - `neoforge.mods.toml`: dependency ranges bumped
- **Partial source migration** toward 1.21.2+ API:
  - `registryAccess().registryOrThrow(X)` → `registryAccess().lookupOrThrow(X)` (Enchantment lookups)
  - Item lookups switched to `BuiltInRegistries.ITEM` (no registry access needed)
  - `ItemEnchantments` comparison by `ResourceLocation` via `Holder.unwrapKey()` rather than resolving the Enchantment value (works with data-driven enchantments)
  - Legacy `Enchantment targetEnchantment` field removed; kept `targetEnchantmentId` only
- **CurseForge file IDs** bumped for Trade Cycling 26.1.2 NeoForge (file `7904299`). Easy Villagers 26.1.2 NeoForge file ID TBD — currently points at 26.1.1 build.

## What's still required — this is substantial

MC 26.1 is a **full rename + rendering-pipeline rewrite**, not just an incremental port.
The existing client-side GUI code needs meaningful rewriting, not just renames:

### 1. `ResourceLocation` → `Identifier` (package rename)
All `net.minecraft.resources.ResourceLocation` references must become `net.minecraft.resources.Identifier`. Affects almost every file that deals with assets/IDs.

### 2. GUI rendering pipeline — **full rewrite**
MC 26.1 replaced the immediate-mode `GuiGraphics`-based rendering with a deferred
render-state extraction pipeline:
- `AbstractWidget.renderWidget(GuiGraphics, ...)` → `extractWidgetRenderState(GuiGraphicsExtractor, ...)`
- `Screen.render(GuiGraphics, ...)` → `extractRenderState(GuiGraphicsExtractor, ...)`
- `guiGraphics.blit(texture, ...)` → `graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ...)` via `WidgetSprites`
- `RenderSystem.setShader(...)` / `setShaderTexture(...)` removed entirely

Files that need rewriting: `CustomImageButton.java`, `ScrollableContainer.java`,
`ConfigScreen.java`, `FilterListScreen.java`, `FilterEditorScreen.java`, `SuggestingEditBox.java`.

### 3. Event object refactor
- `onPress()` → `onPress(InputWithModifiers input)`
- `onClick(double, double, int)` → `onClick(MouseButtonEvent, boolean)`
- `mouseClicked(double, double, int)` → `mouseClicked(MouseButtonEvent, boolean)`
- `keyPressed(int, int, int)` → `keyPressed(KeyEvent event)`
- Affects every custom widget and screen that overrides these.

### 4. NeoForge event API
Likely signature changes in `ClientTickEvent`, `ScreenEvent.Init.Post`,
`InputEvent.Key`. Need to verify against the installed NeoForge 26.1.2 sources.

### 5. `ResourceKey.location()` — verify
One compile error reported `location()` missing on `ResourceKey`. May have been
renamed.

## Build errors at current state

Roughly 100 compile errors when running `./gradlew compileJava`, overwhelmingly
from the GUI/render pipeline rewrite outlined above, plus the
`ResourceLocation`→`Identifier` rename.

## Suggested next steps

1. Pick either `CustomImageButton` or one `Screen` as a reference case and fully
   migrate it to the new extract-render-state pipeline, studying the MC 26.1.2
   widgets (e.g. `net.minecraft.client.gui.components.Button`) as templates.
2. Pattern-apply that migration to remaining widgets/screens.
3. Do the bulk `ResourceLocation`→`Identifier` rename across the tree.
4. Update `ClientEventHandler.java` event subscriptions for any NeoForge event
   signature changes.
5. Iterate `./gradlew compileJava` to closure.
