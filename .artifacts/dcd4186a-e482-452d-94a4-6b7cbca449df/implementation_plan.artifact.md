# Implementation Plan - Fix Create Board UI Responsiveness

This plan fixes the issue where the "Open Gallery" button and Step 2 header are squashed on smaller screens in the "New Board" page.

## Proposed Changes

### [UI Screens]
#### [MODIFY] `CreateScreen.kt`

**1. Make Mobile Layout Scrollable**
- Wrap the main content area (Step 1 and Step 2) in a `Column` with `verticalScroll`.
- Move the Step 2 Header from the Step 1 `Column` to the bottom section or ensure it's part of the global scroll.
- Ensure the bottom section (Step 2 Controls) is also part of the scroll or correctly positioned if it needs to be sticky. (I will make it part of the scroll for better space management on small screens).

**2. Compact `EmptySelectionState`**
- Reduce the icon size from 80dp to 64dp.
- Reduce vertical padding and spacers within the card.
- Ensure the card uses `wrapContentHeight` and does not force a large minimum height.

**3. Fix Image Grid height in Scrollable Column**
- Since `LazyVerticalGrid` cannot be directly placed in a `verticalScroll` with `fillMaxSize`, I will either:
    - Use `Modifier.heightIn(max = ...)` for the grid.
    - Or replace it with a non-lazy grid for mobile if the number of items is small (it's max 20).
    - Or use `LazyColumn` for the whole screen and use `Grid` items.
- Choice: I will use a `Column` with `verticalScroll` for the whole screen and implement the grid using `Row`s or a custom flow layout to allow it to expand naturally within the scroll.

## Verification Plan
- **Mobile Layout**: Open "New Board" on a phone.
- **Empty State**: Confirm "No Photos Yet" card is fully visible and the "Open Gallery" button is correctly sized and has text.
- **Scroll Check**: Verify the whole page can be scrolled if content exceeds the screen height.
- **Tablet Check**: Ensure tablet layout (horizontal split) remains unaffected.
