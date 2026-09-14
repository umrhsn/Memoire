# Implementation Plan - Minimalist Flanking Icon Pills with Icon Backgrounds

This plan finalizes the flanking panels for tablets and foldables, moving to a minimalist icon-only design in a single vertical column, and adds circular backgrounds to the statistics icons to match the action icons' visual style.

## Proposed Changes

### [UI Components]
#### [MODIFY] `SharedComponents.kt`
- **Update `StatBadge`**:
    - Wrap the `Icon` inside the `StatBadge` row with a circular container (`Box` with `CircleShape` and `onSurface.copy(alpha = 0.06f)`).
    - This ensures portrait mode stats have the "circular faint backgrounds" implemented as requested.
    - Set the icon size within this container to `18.dp` (matching `stat_icon_size`).

### [UI Screens]
#### [MODIFY] `MainScreen.kt`
- **Refactor `LeftIconActionPanel`**: Remove all text labels and grouping. Use a single `Column` of 6 icon buttons inside a `FloatingVerticalPill`. Use `AppHeaderIcon` for consistency.
- **Refactor `RightStatsIconPanel`**: Use `FloatingVerticalPill` for consistency.
- **Update `StatIconValue`**:
    - Ensure the circular background style matches `StatBadge` exactly (shape, color, and icon tint).
    - Align the text value underneath the icon container.
- **Update Layout Logic**: Ensure these pills flank the centered board on tablet landscape and opened foldables.

## Verification Plan
- **Tablet Landscape**: Confirm slim icon-only pills appear.
- **Stats Panel Check**: Verify icons in the right panel have circular backgrounds and text values are correctly positioned underneath.
- **Portrait Stats Check**: Confirm icons in the bottom stat pill (portrait) also have circular faint backgrounds.
- **Upright Check**: Confirm every icon and text value is upright and horizontal.
