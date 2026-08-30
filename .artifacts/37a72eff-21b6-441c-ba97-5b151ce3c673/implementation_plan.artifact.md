# Implementation Plan - Modernization Sprint 1

This plan covers the "First-Sprint" checklist from the modernization roadmap, focusing on dependency cleanup, Hilt integration, and migrating from Picasso to Coil.

## User Review Required

> [!IMPORTANT]
> The migration from Picasso to Coil will change how images are loaded. I will verify that all existing image loading logic in the board and custom game flow remains functional.

## Proposed Changes

### Build System & Dependencies

#### [MODIFY] [build.gradle](file:///C:/Users/pc/umrhsn/Memoire/app/build.gradle)
- Remove `com.squareup.picasso:picasso`.
- Add `io.coil-kt:coil:2.7.0`.
- (Optional) Clean up Firebase dependencies to use BoM again if resolution issues are resolved, but I'll stick to the current working explicit versions for now to avoid breaking the build.

---

### UI & Image Loading

#### [MODIFY] [MainActivity.kt](file:///C:/Users/pc/umrhsn/Memoire/app/src/main/java/com/umrhsn/mmoire/activities/MainActivity.kt)
- Replace `Picasso.get().load(imageUrl).fetch()` with Coil's `ImageLoader` or `load` extension.

#### [MODIFY] [MemoryBoardAdapter.kt](file:///C:/Users/pc/umrhsn/Memoire/app/src/main/java/com/umrhsn/mmoire/adapters/MemoryBoardAdapter.kt)
- Replace `Picasso.get().load(...)` with Coil's `load` extension function for `ImageView`.

---

### Architecture & DI (Hilt)

#### [MODIFY] [CreateActivity.kt](file:///C:/Users/pc/umrhsn/Memoire/app/src/main/java/com/umrhsn/mmoire/activities/CreateActivity.kt)
- Replace deprecated `MediaStore.Images.Media.getBitmap` with `ImageDecoder` for all API levels (it's already partially used, I'll clean it up).
- Ensure `@AndroidEntryPoint` is present and correctly handled.

#### [NEW] [NetworkModule.kt](file:///C:/Users/pc/umrhsn/Memoire/app/src/main/java/com/umrhsn/mmoire/di/NetworkModule.kt)
- Provide `FirebaseFirestore` and `FirebaseStorage` instances via Hilt.

## Verification Plan

### Automated Tests
- Run `:app:assembleDebug` to ensure the project still builds.

### Manual Verification
- Deploy the app.
- Start a game to verify images load correctly.
- Create a custom game to verify image selection and upload still work.
