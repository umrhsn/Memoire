# Implementation Plan - Universal Device Orientation and 2-Player Logic

This plan unifies the orientation and 2-player layout logic to handle phones, tablets, and opened foldables correctly. It ensures phones stay in portrait and that square-ish foldables are treated as large displays regardless of orientation.

## Proposed Changes

### [Activities]
#### [MODIFY] [MainActivity.kt](file:///C:/Users/pc/umrhsn/Memoire/app/src/main/java/com/umrhsn/mmoire/activities/MainActivity.kt)
- **Lock Phones to Portrait**: Use a threshold of `500dp` for `smallestScreenWidthDp` to lock mobile phones and folded foldables to portrait mode. This ensures they never accidentally rotate to a layout that doesn't benefit them.

### [UI Screens]
#### [MODIFY] [MainScreen.kt](file:///C:/Users/pc/umrhsn/Memoire/app/src/main/java/com/umrhsn/mmoire/ui/screens/MainScreen.kt)

**1. Reliable Device Category Detection**
- Use `config.smallestScreenWidthDp >= 500` as the definition for `isLargeDisplay`. This correctly includes tablets and opened foldables (like Pixel Fold).
- Detect "tall screens" using `windowSizeClass.heightSizeClass == WindowHeightSizeClass.Expanded`.

**2. Intelligent Layout Decision**
- Define `treatAsLandscape` (vertical split) as true if the device is in landscape **OR** if it's a large display that isn't tall (opened foldable held vertically).
- Only show the "Change Layout" toggle on `isLargeDisplay` when `treatAsLandscape` is true. This hides it in tablet portrait but keeps it on foldables.

**3. Portrait Mirroring Fix**
- Ensure the top-bottom split (used in portrait) always forces the 180° mirrored "Opposite" rotation for Player 1, providing a consistent head-to-head experience on narrow screens.

## Verification Plan

### Manual Verification
- **Pixel 7 (Phone)**: Confirm locked portrait, no toggle, forced mirrored view.
- **Pixel Tablet (Large Tablet)**:
    - **Portrait**: No toggle, forced mirrored view.
    - **Landscape**: Toggle visible, 3 modes functional.
- **Pixel 10 Pro Fold (Foldable)**:
    - **Folded**: Matches phone behavior.
    - **Opened**: Toggle visible in both directions, allowing side-by-side splits on the square screen.
