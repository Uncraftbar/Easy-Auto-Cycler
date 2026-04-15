# Fabric 26.1.2 Port — Status

**Status: WORK IN PROGRESS — does not yet compile.**

## What's done

- **Build system** updated to target MC 26.1.2 / Fabric 0.19.1 / Fabric API 0.146.0+26.1.2 / Java 25
  - Uses Loom `1.17.0-alpha.5` (plugin `fabric-loom` — matching the mod-generator 26.1 scaffold)
  - `gradle.properties`: new version coordinates, externalized CurseForge file ID
  - `settings.gradle`: Fabric Maven + mavenCentral + plugin portal
  - `fabric.mod.json`: MC range `~26.1`, fabricloader `>=0.18.6`, java `>=25`
  - `easyautocycler.mixins.json`: `compatibilityLevel` bumped `JAVA_21` → `JAVA_25`
  - Gradle wrapper bumped to 9.4.0
- **CurseForge dependency**: Trade Cycling Fabric 1.0.20+26.1.2 (file `7904305`) wired via cursemaven.

## What's still required — this is substantial

MC 26.1 is a **full rename + rendering-pipeline rewrite**, not just an incremental port.
The existing client-side GUI code needs meaningful rewriting, not just renames:

### 1. `ResourceLocation` → `Identifier` (package rename)
Every `net.minecraft.resources.ResourceLocation` reference must become
`net.minecraft.resources.Identifier`. Factory methods renamed accordingly
(`ResourceLocation.fromNamespaceAndPath` → `Identifier.fromNamespaceAndPath`,
`ResourceLocation.tryParse` → `Identifier.parse` etc).

### 2. GUI rendering pipeline — **full rewrite**
MC 26.1 replaced the immediate-mode `GuiGraphics`-based rendering with a deferred
render-state extraction pipeline:
- `AbstractWidget.renderWidget(GuiGraphics, ...)` → `extractWidgetRenderState(GuiGraphicsExtractor, ...)`
- `Screen.render(GuiGraphics, ...)` → `extractRenderState(GuiGraphicsExtractor, ...)`
- `guiGraphics.blit(texture, ...)` → `graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ...)` via `WidgetSprites`
- `RenderSystem.setShader(...)` / `setShaderTexture(...)` removed entirely

Files that need rewriting: `CustomImageButton.java`, `ScrollableContainer.java`,
`ConfigScreen.java`, `FilterListScreen.java`, `FilterEditorScreen.java`,
`SuggestingEditBox.java`, `ScreenAccessorMixin.java`.

### 3. Event object refactor
- `onPress()` → `onPress(InputWithModifiers input)`
- `onClick(double, double, int)` → `onClick(MouseButtonEvent, boolean)`
- `mouseClicked(double, double, int)` → `mouseClicked(MouseButtonEvent, boolean)`
- `keyPressed(int, int, int)` → `keyPressed(KeyEvent event)`
- Affects every custom widget and screen that overrides these.

### 4. Registry API
- `registryAccess().registryOrThrow(X)` → `registryAccess().lookupOrThrow(X)` (returns `HolderLookup.RegistryLookup<T>`)
- `Registry.getOptional(id)` → for items, prefer `BuiltInRegistries.ITEM.getOptional(id)` (items are static)
- Enchantment comparison should use `Holder.unwrapKey().map(k -> k.location())` to compare by
  `Identifier` rather than resolving an `Enchantment` value (works with data-driven enchantments).

### 5. Fabric API updates
- `Screens.getButtons(screen)` — signature may have changed in fabric-api 0.146.0.
- `ScreenEvents.AFTER_INIT` — verify callback signature.
- `ClientTickEvents.END_CLIENT_TICK` — still present, but verify.
- `ClientPlayNetworking.send(CustomPacketPayload)` — verify overload.

### 6. `ScreenAccessorMixin`
Accesses `children` and `renderables` fields on `Screen`. In MC 26.1 the `renderables`
field was renamed / restructured around the render-state extraction system. Either
remove the mixin and rework the consumers (`ScrollableContainer`) to track their own
children, or re-map the accessor targets.

## Suggested next steps

1. Pick `CustomImageButton` as the first widget to rewrite against the new
   extract-render-state pipeline, using `net.minecraft.client.gui.components.Button`
   in the 26.1.2 sources as a template.
2. Pattern-apply the migration to `ScrollableContainer` and the three Screen subclasses.
3. Bulk-rename `ResourceLocation` → `Identifier` across the tree.
4. Remove / re-scope `ScreenAccessorMixin` once the screens manage their own widgets
   explicitly.
5. Iterate `./gradlew build` to closure.
