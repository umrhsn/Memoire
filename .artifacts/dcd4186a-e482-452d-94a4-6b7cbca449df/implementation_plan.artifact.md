# Implementation Plan - Final 2-Player Tablet Refinements

This plan implements the final requested refinements for the 2-player layouts, specifically focusing on the "Opposite" mode structure and shared vertical stats console.

## Proposed Changes

### [UI Components]
#### [MODIFY] [MainScreen.kt](file:///C:/Users/pc/umrhsn/Memoire/app/src/main/java/com/umrhsn/mmoire/ui/screens/MainScreen.kt)

**1. Unified Vertical Split for Landscape**
- Update `isHorizontalSplit` to be `false` for ALL modes when in landscape. This ensures `OPPOSITE`, `FACE_TO_FACE`, and `SIDE_BY_SIDE` all use the `Row` (side-by-side) structure.
- Portrait mode will continue to use the `Column` (top-bottom) split for best reach.

**2. Mode-Specific Structure in Row Split**
- **Mode: Side-by-Side**:
    - P1 (Left): 0° rotation, header at top.
    - P2 (Right): 0° rotation, header at top.
- **Mode: Face-to-Face**:
    - P1 (Left): 90° rotation, header at center divider.
    - P2 (Right): -90° rotation, header at center divider.
- **Mode: Opposite** (New Shared Spine):
    - Layout: `[P1 Board] [Central Stats Spine] [P2 Board]`.
    - Central Spine: A dedicated vertical column containing both players' names and stats.
    - Orientation in Spine: P1 stats upright (0°), P2 stats upside-down (180°).
    - Boards: P1 Board (0°), P2 Board (180°).

**3. Correct Player Ordering**
- Ensure Player 1 is always the primary (Left/Top) slot in all modes.

## Verification Plan

### Manual Verification
- Deploy to Pixel Tablet.
- Verify **Side-by-Side**: P1 Left, P2 Right, both upright.
- Verify **Face-to-Face**: P1 Left (90°), P2 Right (-90°), stats meeting at center.
- Verify **Opposite**: P1 Board Left (0°), P2 Board Right (180°), stats grouped in the middle with opposite rotations.
- Confirm Player 1 is consistently on the start side.
