# Implementation Plan - Device-Specific Orientation and 2-Player UI Refinement

This plan addresses orientation restrictions for phones/folded devices and refines the 2-player layout logic to ensure the best experience across phones, tablets, and foldables.

## Proposed Changes

### [Activities]
#### [MODIFY] [MainActivity.kt](file:///C:/Users/pc/umrhsn/Memoire/app/src/main/java/com/umrhsn/mmoire/activities/MainActivity.kt)
- **Force Portrait for Phones**: In `onCreate`, use `WindowSizeClass` to detect if the display is `Compact`. If so, set `requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT`.
- This ensures phones and folded foldables always stay in portrait mode, as requested.

### [UI Components]
#### [MODIFY] [MainScreen.kt](file:///C:/Users/pc/umrhsn/Memoire/app/src/main/java/com/umrhsn/mmoire/ui/screens/MainScreen.kt)

**1. MainHeader Toggle Visibility**
- Update the condition for showing the "Change Layout" icon.
- It should be **hidden** if:
    - Device is `Compact` (Phone/Folded).
    - Device is in **Portrait** (even if it's a tablet).
- It should be **visible** if:
    - Device is `Medium` or `Expanded` (Tablet/Opened Foldable) **AND** it's in **Landscape**.

**2. TwoPlayerLayoutSwitcher Logic**
- **Force Opposite for Phones**: If `isTablet` is false (Compact), force the `OPPOSITE` (mirrored top-bottom) rendering logic.
- **Opened Foldable Treatment**: For `Medium/Expanded` devices, treat the display as "Landscape" (vertical split) more aggressively if needed, but primarily follow the `isLandscape` flag for tablets.
- Ensure the `rotationZ` and `statsAtBottom` logic for the forced portrait mode is solid.

## Verification Plan

### Manual Verification
- **Phone Emulator (e.g., Pixel 7)**:
    - Verify the app cannot rotate to landscape.
    - Verify 2-player mode shows Player 1 at bottom, Player 2 at top (mirrored), and **no layout toggle icon**.
- **Tablet Emulator (e.g., Pixel Tablet)**:
    - **Portrait**: No toggle icon, forced Opposite-style mirrored view.
    - **Landscape**: Toggle icon visible, all 3 modes functional.
- **Foldable Emulator (e.g., Pixel Fold)**:
    - **Folded**: Behavior matches Phone (Portrait only, no toggle).
    - **Opened**: Behavior matches Tablet (Rotation allowed, toggle visible in landscape).
