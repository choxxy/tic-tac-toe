---
task: 260405-difficulty-selection
title: Implement CPU Difficulty Selection
type: execute
---

<objective>
Implement a difficulty selection dialog for VS CPU mode, allowing players to choose between Easy, Medium, and Hard difficulty levels before starting a game.
</objective>

<tasks>

<task type="auto">
  <name>Task 1: Create Difficulty Selection Dialog</name>
  <files>app/src/main/java/com/jna/tictactoe/screen/menu/DifficultyDialog.kt</files>
  <action>
    - Create a new Composable `DifficultyDialog` following the Zenith Grid design principles (asymmetrical breathes, tonal depth).
    - Provide options for "Easy", "Medium", and "Hard".
    - Include a "Cancel" and "Start Game" action.
  </action>
</task>

<task type="auto">
  <name>Task 2: Integrate Dialog into MainMenuScreen</name>
  <files>app/src/main/java/com/jna/tictactoe/screen/menu/MainMenuScreen.kt</files>
  <action>
    - Add state to `MainMenuScreen` to control the visibility of the `DifficultyDialog`.
    - Show the dialog when the "Play vs CPU" card is clicked.
    - Update `onVsCpu` callback to accept the selected `Difficulty`.
  </action>
</task>

<task type="auto">
  <name>Task 3: Update Navigation and MainActivity</name>
  <files>app/src/main/java/com/jna/tictactoe/MainActivity.kt</files>
  <action>
    - Update the `onVsCpu` callback in `MainActivity`'s `NavHost` to pass the selected `Difficulty` to the `navController`.
  </action>
</task>

</tasks>

<verification>
- Verify that clicking "Play vs CPU" opens the difficulty dialog.
- Verify that selecting a difficulty and clicking "Start Game" navigates to the game screen with the correct difficulty.
- Verify that the difficulty is correctly applied in the game (e.g., "Hard" AI is unbeatable).
</verification>
