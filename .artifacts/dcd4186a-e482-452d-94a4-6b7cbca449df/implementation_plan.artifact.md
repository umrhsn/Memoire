# Implementation Plan - Refine Tablet Portrait 2-Player UI

This plan hides the redundant layout toggle in tablet portrait mode and forces a consistent mirrored "Opposite" view for that orientation, ensuring a clean and functional head-to-head experience on large screens.

## Proposed Changes

### [UI Components]
#### [MODIFY] [MainScreen.kt](file:///C:/Users/pc/umrhsn/Memoire/app/src/main/java/com/umrhsn/mmoire/ui/screens/MainScreen.kt)

**1. Update `MainHeader` Logic**
- Update the `MainHeader` function signature to accept `isTablet: Boolean` and `isLandscape: Boolean`.
- Wrap the "Change Layout" `AppHeaderIcon` in a condition to hide it specifically for tablet portrait: `if (uiState.isTwoPlayerMode && !(isTablet && !isLandscape))`.
- Update the `MainHeader` call site in `MainScreen` to pass these new parameters.

**2. Force Mirrored View in Tablet Portrait**
- In `TwoPlayerLayoutSwitcher`, modify the `rotationZ` assignment for the top slot (Player 1) in the `isHorizontalSplit` (portrait/stacked) branch.
- If `isTablet` is true, force `rotationZ = 180f` regardless of the stored `layout` value.
- Preserve the existing `when(layout)` logic for phones to maintain their flexible portrait behavior.

## Verification Plan

### Manual Verification
- **Pixel Tablet (Portrait)**:
    - Confirm the "Change Layout" icon is hidden.
    - Confirm **Player 1 (Top)** is always rotated 180°, even if `SIDE_BY_SIDE` was previously selected in landscape.
- **Pixel Tablet (Landscape)**:
    - Confirm the "Change Layout" icon is visible and functional.
- **Phone (Both Orientations)**:
    - Confirm the "Change Layout" icon remains visible and functional in all modes.
- **Single Player**:
    - Confirm no visual changes or regressions in the header.
