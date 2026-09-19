# Fix Blank Screen When Editing Games

The issue is that `CreateActivity` does not trigger a recomposition when `boardSize` is asynchronously loaded during the game edit flow. `boardSize` is a regular `var`, so its updates are not tracked by Jetpack Compose.

## Proposed Changes

### [Create Activity]

#### [MODIFY] [CreateActivity.kt](file:///C:/Users/pc/umrhsn/Memoire/app/src/main/java/com/umrhsn/mmoire/activities/CreateActivity.kt)
- Convert `boardSize` to a `MutableState` so that updates trigger recomposition.
- Derive `numImagesRequired` from `boardSize` or make it a `MutableState` as well.
- Add a loading state in the UI for when an existing game is being loaded but `boardSize` is not yet available.

## Verification Plan

### Automated Tests
- I'll check `CreateViewModelTest.kt` to see if I can add a test for the edit flow, although the bug is primarily in the Activity's UI synchronization logic which is harder to unit test with Robolectric without full Activity lifecycle. I will focus on ensuring the `CreateViewModel` correctly handles the `loadGame` state.

### Manual Verification
1. Launch the app.
2. Create a new game with some images.
3. Go to the "Browse Boards" screen.
4. Tap the "Edit" icon on the newly created game.
5. Verify that the `CreateScreen` opens and is correctly populated with the game's images and name.
