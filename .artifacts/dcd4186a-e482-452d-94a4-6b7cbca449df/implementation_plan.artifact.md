# Implementation Plan - Expose and Label Actions on Large Displays

This plan refactors the UI to expose and label game actions (Create, Load, Settings, Help) on tablets and opened foldables.

## Proposed Changes

### [UI Screens]
#### [MODIFY] [MainScreen.kt](file:///C:/Users/pc/umrhsn/Memoire/app/src/main/java/com/umrhsn/mmoire/ui/screens/MainScreen.kt)

**1. Create `SidebarActionButton`**
- A reusable component that matches the large action buttons (icon + label) but is styled with a light background to match the original icons.

**2. Update Sidebar (Tablet Landscape)**
- Place the four actions in a 2x2 grid at the bottom of the sidebar.
- Each button in the grid will use the new `SidebarActionButton` style with its respective label.

**3. Update Top Bar (Large Displays)**
- On tablets and opened foldables (portrait and 2-player modes), spread the action icons across the top bar instead of hiding them in a "More" menu.

## Verification Plan

### Manual Verification
- **Tablet Landscape**: Confirm sidebar has Reset/Size buttons followed by 4 labeled small buttons in 2x2.
- **Tablet Portrait / 2-Player**: Confirm top bar has all icons visible.
- **Phone**: Confirm "More" overflow menu still functions correctly.
