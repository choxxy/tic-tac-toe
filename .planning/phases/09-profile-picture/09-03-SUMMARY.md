---
phase: 09-profile-picture
plan: 3
type: execute
wave: 3
depends_on:
  - "09-02"
files_modified:
  - app/src/main/java/com/jna/tictactoe/screen/profile/ProfileViewModel.kt
  - app/src/main/java/com/jna/tictactoe/data/PreferenceRepository.kt
  - app/src/main/java/com/jna/tictactoe/data/UserPreferences.kt
autonomous: true
requirements:
  - PROFILE-03
must_haves:
  truths:
    - "A selected image is saved to the app's internal storage"
    - "The file path of the saved image is stored in DataStore"
  artifacts:
    - path: "app/src/main/java/com/jna/tictactoe/screen/profile/ProfileViewModel.kt"
      contains: "saveImageToInternalStorage"
    - path: "app/src/main/java/com/jna/tictactoe/data/PreferenceRepository.kt"
      contains: "updateProfilePicturePath"
---

# Phase 09-profile-picture, Plan 3: Summary

This plan implemented the logic to save a selected profile picture to the app's internal storage and persist its file path using Jetpack DataStore.

## Key Changes

1.  **DataStore & Repository Layer (`PreferenceRepository.kt`, `UserPreferences.kt`)**:
    -   The `UserPreferences` data class was updated to store a `profilePicturePath` string, replacing the previous `profilePictureUri`.
    -   `PreferenceRepository` was updated to include a `stringPreferencesKey` for `PROFILE_PICTURE_PATH`.
    -   A new function, `updateProfilePicturePath(path: String)`, was added to the repository to save the file path to DataStore.
    -   The `userPreferencesFlow` was updated to read and expose the new `profilePicturePath`.

2.  **ViewModel Layer (`ProfileViewModel.kt`)**:
    -   The `Application` context was injected into the `ProfileViewModel` to enable file system access.
    -   A new private suspend function, `saveImageToInternalStorage(uri: Uri)`, was created. This function:
        -   Takes a content `Uri` from the image picker.
        -   Copies the image data to a new file within the app's internal storage (`filesDir`).
        -   Returns the absolute path of the newly created file.
    -   The `onImageSelected(uri: Uri?)` function was updated to orchestrate the process: it calls `saveImageToInternalStorage` and then uses the returned path to update the DataStore via `preferenceRepository.updateProfilePicturePath`.

## Deviations from Plan

- **`UserPreferences.kt`**: The plan specified adding `profilePicturePath`. However, a `profilePictureUri` field already existed. Instead of adding a new field, `profilePictureUri` was renamed to `profilePicturePath` to avoid redundancy and clarify its purpose (storing a local file path, not a temporary content URI). This was a necessary correction for the feature to work as intended.
- **`PreferenceRepository.kt`**: Correspondingly, the existing `PROFILE_PICTURE_URI` key and `updateProfilePicture` function were renamed to `PROFILE_PICTURE_PATH` and `updateProfilePicturePath` to align with the changes in `UserPreferences`.

## Final Outcome

The application now has the core logic required to let a user select a profile picture, save it securely in the app's private storage, and remember the choice across app sessions. The `ProfileViewModel` handles the image processing, and the `PreferenceRepository` ensures the file path is persisted correctly.
