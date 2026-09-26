# Autocomplete input fix — 2026-09-26 (local only)

User testing confirmed cycling works, but popup clicks failed on both loaders
and NeoForge Tab moved focus instead of completing.

## Cause and change

- Minecraft 26.3 uses SDL input codes. `InputConstants.MOUSE_BUTTON_LEFT` is
  **1**, but both widgets still required the old GLFW value **0**.
- NeoForge's widget also still compared key events against GLFW constants.
  Switched Tab, Up/Down, Return and keypad Enter to `InputConstants.KEY_*`.
- Retained the editor's existing popup-first click routing (all three ID
  fields); no new focus-navigation override is needed. Vanilla Screen already
  offers keyboard events to the focused widget before navigating.
- Ignore stale popup bounds when the current match list is empty.
- Cycling, filtering, and GUI layout are unchanged.

## Verification

`./gradlew test build` passed on Java 25. Eight regression tests passed:
Tab acceptance; Screen's focused-widget Tab dispatch; Up/Down with both Enter
keys; normal Tab fall-through without matches; all three popup rows; popup
above the input; wrong buttons/outside/unfocused/stale popup rejection; and
popup-first click dispatch for enchantment, item and payment fields.

Tests execute the real widget and Screen event code against 26.3 classes, with
font/rendering mocked and native focus/IME isolated. **These are not a fresh
interactive or rendered-client validation.** Existing running clients must be
relaunched to load the change; no windows were restarted by this fix task.

Updated JAR: `build/libs/easyautocycler-neoforge-26.3-3.1.2-local.26.3.jar`.
Nothing pushed or published; previously exported test bundles are unchanged.
