# Design System Strategy: The Elevated Playfield

This design system reimagines the classic Tic Tac Toe experience as a high-end, editorial digital product. By moving away from "game-like" tropes—heavy borders, neon glow, and chaotic animations—we embrace a philosophy of **Architectural Serenity**. The goal is to make the act of placing an 'X' or an 'O' feel as intentional and satisfying as placing a physical stone on a marble board.

---

## 1. Creative North Star: "The Kinetic Gallery"
The North Star for this system is the concept of a **Kinetic Gallery**. We treat the game board not as a grid of buttons, but as a curated space where form and void interact. 

To break the "template" look:
- **Intentional Asymmetry:** Avoid centering every element perfectly in the viewport. Use the `20` (7rem) or `24` (8.5rem) spacing tokens to create large, asymmetrical "breathing zones" at the top of the layout.
- **Tonal Depth:** Instead of lines, we use the shifting light of `surface-container` tiers to define the board.
- **Typography as Architecture:** We use extreme contrast between `display-lg` and `label-sm` to create a sophisticated, editorial hierarchy.

---

## 2. Color & Surface Philosophy
The palette is rooted in soft, ethereal neutrals (`surface` #f9f9fe) contrasted with high-precision accents for gameplay.

### The "No-Line" Rule
**Explicit Instruction:** Do not use 1px solid borders to define the 3x3 grid. Sectioning must be achieved through background shifts.
- The game board should be a `surface-container-high` (#e4e8f3) area.
- Individual cells should be `surface-container-lowest` (#ffffff).
- The transition between these two tones creates the "grid" naturally without a single line being drawn.

### Surface Hierarchy & Nesting
Use the Material-inspired tiers to create a "nested" physical reality:
1.  **Level 0 (Base):** `surface` (#f9f9fe) — The infinite gallery floor.
2.  **Level 1 (Section):** `surface-container-low` (#f2f3fa) — Used for the game stats area.
3.  **Level 2 (Active Board):** `surface-container-highest` (#dce3f0) — The main board background.
4.  **Level 3 (Interactive Cell):** `surface-container-lowest` (#ffffff) — The individual 3x3 squares.

### Signature Textures & Glass
- **The "X" Factor:** Use a subtle vertical gradient for the 'X' player, transitioning from `primary` (#005bc1) to `primary_dim` (#004faa).
- **The "O" Factor:** Use a gradient from `secondary` (#c1000a) to `secondary_dim` (#ab0008).
- **Glassmorphism:** For overlays (like a "Winner" announcement), use `surface_container_lowest` at 70% opacity with a `20px` backdrop-blur. This keeps the game board visible but softened.

---

## 3. Typography: The Editorial Voice
We utilize **Manrope** for its geometric clarity and modern rhythm.

- **The Scoreboard (`display-lg`):** Use this for the primary score numbers. The large scale (3.5rem) transforms a simple number into a graphic element.
- **The Call to Action (`title-lg`):** Use for "Your Turn" or "Play Again." It provides enough weight without feeling "loud."
- **Meta Data (`label-sm`):** Use for "Total Games Played" or "Difficulty Level." Keep these in `on_surface_variant` (#595f6a) to ensure they recede into the background.

**Identity Tip:** Set `letter-spacing: -0.02em` on all `display` and `headline` tokens to give the typeface a tighter, premium "custom-set" feel.

---

## 4. Elevation & Depth
In this system, depth is "baked in" rather than "applied."

- **The Layering Principle:** Place `surface-container-lowest` cards on top of a `surface-container-low` background. This creates a soft, natural lift.
- **Ambient Shadows:** For the active player's indicator, use a shadow with a blur of `40px` and an opacity of `6%`, using the `surface_tint` (#005bc1) color. This mimics the way colored light reflects off a surface.
- **The "Ghost Border" Fallback:** If you must define a boundary (e.g., in a settings menu), use `outline-variant` (#acb2bf) at **15% opacity**. It should be felt, not seen.

---

## 5. Components & Gameplay Elements

### The Game Cell (The Hero Component)
- **Container:** `surface-container-lowest`, `rounded-xl` (0.75rem).
- **Hover State:** Transition background to `surface-container-high` smoothly (200ms).
- **Empty State:** No content, no border.
- **Active State (X/O):** Content enters with a 10% scale-up and fade-in.

### Buttons (Actionable Logic)
- **Primary (e.g., "New Game"):** No solid background. Use a `surface-container-highest` fill with `primary` text. This feels more modern and less heavy than a solid blue block.
- **Secondary:** Use `title-sm` typography with a `3` (1rem) padding and no container.

### Status Chips
- **Turn Indicator:** A pill-shaped (`rounded-full`) chip using `surface-container-lowest`. Use a `0.5` (0.175rem) dot of the active player's color (`primary` or `secondary`) next to the label.

### Lists & Settings
- **Forbid Dividers:** To separate "Sound Effects" from "Haptics" in settings, use a `4` (1.4rem) spacing gap. The white space is your divider.

---

## 6. Do’s and Don’ts

### Do
- **Do** use `20` and `24` spacing for top and bottom margins to embrace "The Digital Curator" look.
- **Do** use `primary-container` (#d8e2ff) as a very subtle background for the 'X' player's turn indicator to provide a "glow" without using a shadow.
- **Do** ensure the "O" (`secondary`) feels equally vibrant but distinct in temperature (Warm vs. Cool).

### Don't
- **Don't** use black (#000000). Use `on_background` (#2c333d) for all "black" text to maintain the soft neutral aesthetic.
- **Don't** use standard `rounded-md`. Use `rounded-xl` for large containers and `none` for decorative background accents to create a "custom" architectural feel.
- **Don't** crowd the board. If the screen is small, shrink the board before shrinking the white space around it.