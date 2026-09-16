# Implementation Plan - Fixed 2-Player Stats Layout and Visibility

This plan fixes the visibility of player names in dark mode and implements a non-scrollable, two-row statistics layout for 2-player mode.

## Proposed Changes

### [UI Components]
#### [MODIFY] `SharedComponents.kt` - `StatBadge`
- **Icon Background**: Maintain the circular background for icons.
- **Constraints**: Ensure text does not wrap or truncate (`maxLines = 1`).

### [UI Screens]
#### [MODIFY] `MainScreen.kt` - `PlayerStatsRow`
- **Layout**: Use a `Column` to create a two-row structure:
    - **Row 1**: Player name and icon, aligned to the **start**.
    - **Row 2**: Statistics badges, aligned to the **end**.
- **Visibility**: Set the color of the "Player X" text to `MaterialTheme.colorScheme.onSurface` to ensure it is visible in both light (black) and dark (white) modes.
- **Responsiveness**: Ensure the layout is not scrollable and fits all content by using the two-row stack.

## Verification Plan
- **2-Player Dark Mode**: Confirm "Player 1" and "Player 2" text is clearly visible (white/light).
- **2-Player Light Mode**: Confirm "Player 1" and "Player 2" text is clearly visible (black/dark).
- **Alignment Check**: Confirm Player info is at the start and Stats are at the end, stacked vertically.
- **No Scroll Check**: Confirm the row does not scroll horizontally.
