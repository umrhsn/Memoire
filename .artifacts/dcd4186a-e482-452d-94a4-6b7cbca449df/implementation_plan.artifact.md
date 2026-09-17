# Implementation Plan - Fix 2-Player RTL Positioning (Manual Order Swap)

This plan fixes the physical positioning of players in 2-player mode on tablets/foldables in landscape. It ensures Player 1 is always on the left and Player 2 is on the right, regardless of whether the system language is LTR (like English) or RTL (like Arabic).

## Proposed Changes

### [UI Screens]
#### [MODIFY] [MainScreen.kt](file:///C:/Users/pc/umrhsn/Memoire/app/src/main/java/com/umrhsn/mmoire/ui/screens/MainScreen.kt)

**1. Detect Layout Direction**
- Inside `TwoPlayerLayoutSwitcher`, get the current `LayoutDirection` using `LocalLayoutDirection.current`.

**2. Manual Slot Ordering in Landscape `Row`**
- Replace the `CompositionLocalProvider` hack (which appears to be ineffective) with explicit logic.
- If `isRtl`:
    - Render the "Player 2" area first.
    - Render the "Player 1" area second.
    - Result in RTL: [P2 (Right)] [P1 (Left)]. Physically: [P1] [P2].
- If `!isRtl`:
    - Render the "Player 1" area first.
    - Render the "Player 2" area second.
    - Result in LTR: [P1 (Left)] [P2 (Right)]. Physically: [P1] [P2].

## Verification Plan

### Manual Verification
- **Tablet Landscape (Arabic)**: Confirm Player 1 is physically on the left and Player 2 is on the right.
- **Tablet Landscape (English)**: Confirm Player 1 is on the left and Player 2 is on the right.
- **Functionality**: Ensure rotations (Face-to-Face) remain correctly associated with the correct player slot.
