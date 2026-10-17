# Experience and accessibility

Status: proposed · Owner: UX

## Journeys

1. **Try:** launch → see a synthetic island inside the app → learn limitations → adjust placement.
2. **Enable:** explain overlay access → open the relevant Android settings → recheck on return → optionally enable notification access → choose sources → tap Start.
3. **Use:** compact event → tap to expand → choose an explicit action or collapse → return to current activity.
4. **Pause:** use the app or service notification → window disappears → state clearly reads Paused.
5. **Recover:** unavailable source or stopped service → open diagnostics → explain the exact missing capability → user chooses recovery.

Never request all permissions at launch. Access to post this app's notifications is separate from access to read notifications from selected sources. The in-app demo remains available without either.

## Surface specification

| State | Presentation | Interaction |
| --- | --- | --- |
| Hidden | No overlay window | Start from app |
| Compact | Source icon and safe summary; text hidden by default | Tap expands; accessible label describes state |
| Expanded | Bounded card with permitted metadata and available actions | Explicit collapse and action controls |
| Suspended | No window while locked, screen off, paused, or access lost | Resume only when guards permit |
| Unavailable | Explanation in app, never a blocking overlay | Open settings or continue with reduced capability |

Starting design tokens: 12 dp outer safe spacing, 48 dp minimum interactive targets, 20 dp rounded expanded corners, 180–260 ms transitions, system typography and contrast-aware surfaces. These are design proposals to tune on device. A compact visual pill can be smaller than its interactive target only if the surrounding touch area does not obstruct unrelated controls; otherwise enlarge or move it.

Placement uses safe insets, current display geometry, and user calibration. Prefer a position below system UI; camera cutout alignment is best effort. Landscape defaults to hidden until validated. A changed display/fold configuration invalidates previous geometry before reattachment. Expanded content must fit usable bounds and scroll internally at large font sizes.

## Gestures and focus

Tap expands; explicit Collapse closes the card. Swipe-to-dismiss is optional and always has an accessible equivalent. MVP dismissal suppresses the local island item, not the source notification. Long press may open a small menu only after its gesture behavior is tested. Avoid gesture-only actions and transparent full-screen touch capture.

An expanded island never automatically steals keyboard focus. TalkBack navigation and back/collapse behavior must be proven with the selected window flags; if the overlay cannot provide an accessible expanded interaction, offer an in-app detail screen instead. Touch exploration must not expose hidden content.

## Content and interruption rules

Default preview is source icon plus generic event category; title/body require an explicit setting. Never reconstruct OS-redacted content. Locking hides the entire overlay. Honor ranking suppression signals when available and offer a manual quiet mode; complete DND state may not be observable without additional access. No separate sound or vibration in MVP, avoiding duplicate notification alerts.

Media remains stable while playing; a transient event may briefly replace the compact surface and then return to media. User interaction holds the expanded item until collapse, removal, invalidation, or a privacy guard interrupts it. A second event is indicated by a bounded count, not another overlay window.

## Accessibility acceptance

- TalkBack can identify source, expand/collapse, execute supported actions, and leave the surface without a focus trap.
- At 200% font scale, text and actions remain reachable within display bounds.
- RTL mirrors layout and directional icons where appropriate; event order remains deterministic.
- Reduced-motion/system animation settings replace movement with immediate or short fade transitions.
- Contrast target is at least 4.5:1 for normal text and 3:1 for large text and essential controls.
- Pause and recovery instructions work without relying on color, animation, or gestures alone.

See [state machine](../system-architecture/04-event-state-machine.md) for authoritative transitions and [quality strategy](../system-architecture/08-quality-validation.md) for evidence requirements.

## Command palette and new live activities

The [feature map](raycast-feature-map.md) extends these island journeys. Use a focused, keyboard-aware palette with app/command results, typed calculator/graph/conversion cards, emoji categories, imported clipboard items and explicit AI entry points. Search clear/back, result count, action menus and date footer adapt the screenshot to touch rather than requiring desktop modifier keys.

Task progress shows actual agent state when a runner is connected; design demos are explicitly labeled simulation. Torch controls show hardware callback state and unavailable/permission-denied paths. Music expands to supported playback actions; artwork and timed lyrics remain source/provider dependent. A running-agent count must not imply that the coding-role handbooks are executing.

Per-app management exposes visibility, favorite, aliases and eventually priority/shortcut capability. Hidden items remain recoverable from management. No conversion card presents fabricated current rates and no AI card presents static sample text as a model answer.
