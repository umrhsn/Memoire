# Implementation Plan - Fix 2-Player Tablet Landscape Layouts (Face-to-Face & Opposite)

This plan fixes the orientation and layout issues for Face-to-Face and Opposite 2-player modes on tablets. It ensures headers stay intact during rotation and reuses the side-by-side vertical split structure for the Opposite mode.

## Proposed Changes

### [UI Components]
#### [MODIFY] [MainScreen.kt](file:///C:/Users/pc/umrhsn/Memoire/app/src/main/java/com/umrhsn/mmoire/ui/screens/MainScreen.kt)

**1. Fix `isHorizontalSplit` Logic**
- Update the condition to be `false` for all three modes when in landscape. This forces `OPPOSITE`, `FACE_TO_FACE`, and `SIDE_BY_SIDE` to use the `Row` (vertical split) structure.
- Portrait mode will still default to `isHorizontalSplit = true` (Top-Bottom Column).

**2. Implement Compact Stats Header**
- Create `PlayerStatsCompactHeader` (or similar) that uses `Arrangement.spacedBy()` instead of `fillMaxWidth()` + `Arrangement.SpaceBetween`. This prevents the name and badges from drifting apart when the header is rotated 90°/-90°.
- Update `PlayerRaceHalf` to accept a `useCompactHeader` boolean.

**3. Fix Face-to-Face Mode (Landscape)**
- **Slot Mapping**: Player 1 (Left), Player 2 (Right).
- **Player 1 (Left)**: Rotated 90°. Use compact header positioned at the center divider (`statsAtBottom = true`).
- **Player 2 (Right)**: Rotated -90°. Use compact header positioned at the center divider (`statsAtBottom = false`).
- **End State**: `[P1 cards (outward)] [P1 stats][divider][P2 stats] [P2 cards (outward)]`.

**4. Fix Opposite Mode (Landscape)**
- **Slot Mapping**: Player 1 (Left), Player 2 (Right).
- **Player 1 (Left)**: 0° rotation (Standard Side-by-Side slot).
- **Player 2 (Right)**: Wrap the entire `PlayerRaceHalf` (Header + Board) in a `Box` with `graphicsLayer { rotationZ = 180f }`.
- **Logic**: No dimension swap trick needed for 180° rotation.

**5. Portrait Mode Intentionality**
- Portrait mode will continue to use the mirrored Top-Bottom split (`Column`) regardless of the selected layout. This ensures ergonomics on tall screens. I will ensure the rotation logic `when(layout) { SIDE_BY_SIDE -> 0f, else -> 180f }` is correctly applied to the top slot.

## Verification Plan

### Manual Verification (Pixel Tablet Landscape)
- [ ] **Face-to-Face**: Confirm Player 1 (Left) and Player 2 (Right) stats are grouped together in the center vertical spine, facing their players. No drifting name labels.
- [ ] **Opposite**: Confirm Player 1 is upright (Left) and Player 2's entire half (Header + Cards) is upside-down (Right).
- [ ] **Side-by-Side**: Confirm it remains a vertical split with both players upright (0°).
- [ ] **Ordering**: Confirm Player 1 is always in the start/left slot.
- [ ] **Screenshots**: Capture and attach screenshots of Face-to-Face and Opposite modes to confirm stats meet at the divider.
