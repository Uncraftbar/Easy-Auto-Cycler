# Autocomplete input fix — 2026-09-26 (local only)

User testing confirmed cycling and Tab completion work, but clicking popup
suggestions does not.

## Cause and change

Minecraft 26.3 uses SDL input codes. `InputConstants.MOUSE_BUTTON_LEFT` is **1**,
but the widget required the old GLFW value **0**. Use the named Minecraft
constant and ignore stale popup bounds when the current match list is empty.
The existing popup-first event routing for enchantment, item and payment
fields remains intact. Keyboard handling already used the correct constants.
Cycling, filtering, and GUI layout are unchanged.

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

Updated JAR: `build/libs/easyautocycler-fabric-26.3-3.1.2-local.26.3.jar`.
Nothing pushed or published; previously exported test bundles are unchanged.
