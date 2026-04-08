---
phase: quick
plan: 260408-gsg
type: execute
wave: 1
depends_on: []
files_modified:
  - .planning/STATE.md
autonomous: true
requirements: []
must_haves:
  truths:
    - "STATE.md documents Planka board IDs so any workflow can reference them"
    - "STATE.md documents the Planka update convention so future phases/plans update cards"
    - "Current phase cards in Planka reflect accurate In Progress / Done status"
  artifacts:
    - path: ".planning/STATE.md"
      provides: "Planka IDs, list IDs, and update convention"
  key_links:
    - from: ".planning/STATE.md"
      to: "Planka board 1748558091659510815"
      via: "documented IDs and convention"
---

<objective>
Document Planka as a secondary tracking system in STATE.md by recording board IDs and defining a convention for when and how to move/create cards. Then sync the current phase progress to Planka so the board reflects reality.

Purpose: Future gsd workflows (execute-plan, fast, quick) can reference STATE.md to know how to keep Planka in sync without re-discovering IDs or conventions.
Output: Updated STATE.md with Planka section + Planka cards reflecting current Phase 9 progress.
</objective>

<execution_context>
@$HOME/.claude/get-shit-done/workflows/execute-plan.md
</execution_context>

<context>
@.planning/STATE.md
</context>

<tasks>

<task type="auto">
  <name>Task 1: Add Planka tracking section to STATE.md</name>
  <files>.planning/STATE.md</files>
  <action>
Add a new `## Planka Tracking` section to `.planning/STATE.md` immediately after the `## Overview` block. The section must contain:

1. **Board identifiers** (hardcoded, never change):
   ```
   ## Planka Tracking
   - **Project ID:** 1748557981382870045
   - **Board ID:** 1748558091659510815
   - **Lists:**
     - Todo: 1748582506266887205
     - In Progress: 1748582538210706470
     - Done: 1748582561136772135
   ```

2. **Update convention** — a clear rule block that future Claude instances follow:
   ```
   ### Convention
   Planka is a secondary tracking system. Keep it in sync whenever STATE.md is updated:
   - When a phase/plan is **started**: create a card in "In Progress" list titled "{Phase N} — {Phase Name}" (or move existing card from Todo).
   - When a phase/plan is **completed**: move its card to the "Done" list.
   - When a quick task is **completed**: add a comment to the relevant phase card, or create a short-lived card in Done titled "Quick: {task-id}".
   - Use the MCP `mcp__planka__*` tools (create_card, update_card, move_card) with the IDs above.
   - Do NOT block phase execution on Planka failures — Planka sync is best-effort.
   ```

Update `## Overview` → `**Last Updated:**` to `2026-04-08`.
  </action>
  <verify>grep -n "Planka Tracking" /Users/choxxy/Projects/tictactoe/.planning/STATE.md</verify>
  <done>STATE.md contains a "## Planka Tracking" section with all five IDs and the convention block</done>
</task>

<task type="auto">
  <name>Task 2: Sync current Phase 9 progress to Planka</name>
  <files></files>
  <action>
Use the Planka MCP tools to bring the board into sync with the current STATE.md reality.

Phase 9 is "Profile Picture" — Plans 1-3 are complete, Plan 4 is in progress.

Steps:
1. List existing cards on the board to see what's already there:
   - Call `mcp__planka__get_cards_in_list` for each of the three list IDs.

2. For each completed plan that doesn't have a Done card, create one:
   - "Phase 9 Plan 1 — Profile Picture: Dependencies & Manifest" → Done list (1748582561136772135)
   - "Phase 9 Plan 2 — Profile Picture: UI & Camera/Gallery" → Done list
   - "Phase 9 Plan 3 — Profile Picture: Storage & Persistence" → Done list

3. For the current in-progress plan:
   - "Phase 9 Plan 4 — Profile Picture: Display in MainMenu" → In Progress list (1748582538210706470)

4. For all earlier completed phases (1-7 and quick tasks), move/create cards in Done if they don't already exist:
   - Check what cards already exist first (step 1) — only create what's missing.
   - Completed phases to ensure are in Done: Phase 1, Phase 2, Phase 3, Phase 4, Phase 5, Phase 6, Phase 7.

Use card title format: "Phase {N} — {Phase Name}" for phase-level cards.
If the board already has cards from initial setup, move them to the correct list rather than creating duplicates.
  </action>
  <verify>Use mcp__planka__get_cards_in_list for the Done list ID and confirm Phase 1-7 and Phase 9 Plans 1-3 cards are present</verify>
  <done>Planka board shows: Phases 1-7 in Done, Phase 9 Plans 1-3 in Done, Phase 9 Plan 4 in In Progress</done>
</task>

</tasks>

<verification>
- STATE.md has `## Planka Tracking` with Project ID, Board ID, and all three list IDs
- STATE.md has the convention block describing when and how to update Planka
- Planka Done list contains cards for all completed phases/plans
- Planka In Progress list contains card for current Phase 9 Plan 4
</verification>

<success_criteria>
Any future Claude instance reading STATE.md can find Planka IDs and the update convention without asking. The Planka board visually reflects the current project state.
</success_criteria>

<output>
No SUMMARY file needed for quick tasks. Confirm completion in the chat.
</output>
