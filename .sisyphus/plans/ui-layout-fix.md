# Fix UI Layout in GameRenderer.java

## TL;DR

> **Quick Summary**: Refactor the terminal UI rendering from a row-by-row paired layout to a strictly column-based layout matching the design PNG. Add "Needs:" labels to Nobles to match regular card styling.
>
> **Deliverables**:
> - Updated `GameRenderer.java` with independent Left and Right column generation.
> - Left Column: Level 3 Deck -> Level 2 Deck -> Level 1 Deck -> Menu & Bank (side-by-side).
> - Right Column: Nobles (side-by-side array) -> Move History -> Players (1 & 2 side-by-side) -> Players (3 & 4 side-by-side).
> - Updated Noble card rendering matching standard Deck card height and including the requirement label.
>
> **Estimated Effort**: Short
> **Parallel Execution**: NO - sequential
> **Critical Path**: Task 1 -> Task 2

---

## Context

### Original Request
The current layout draws the Board row-by-row, coupling unrelated elements (like Level 3 tier and Nobles) leading to huge whitespace gaps when elements have different heights. 
- Left Column needs to be: Level 3 deck -> Level 2 deck -> Level 1 deck -> Menu and Bank side-by-side at the bottom.
- Right Column needs to be: Nobles (array of smaller cards) -> Move History -> Players (1 & 2 side-by-side) -> Players (3 & 4 side-by-side, if present).
- Nobles should use ASCII card frames exactly like regular cards, displaying cost, id, and point values, but in a slightly smaller rectangle.

### Interface Summary
**Key Discussions**:
- **Layout Approach**: Swapping from horizontal row-by-row pairs to vertical Column stacks, combining only once at the end.
- **Noble Dimension**: Reusing the same ASCII frame format as regular cards, but displaying requirements instead of cost.
- **Menu/Bank Orientation**: The Menu and Bank must be side-by-side at the bottom of the Left Column, directly under the deck.

### Metis Review
**Identified Gaps** (addressed):
- **Missing automated tests**: The project relies on `compile.bat` and manual JS expect scripts. -> *Auto-Resolved*: Adding a console output generation test via the existing node driver isn't strictly necessary since `generate_renderer.js` already successfully outputs the file and can be tested visually or via the existing `test_game.exp`. I will add a simple node smoke test to QA.
- **Noble "Needs" Label**: PNG needs a label above the requirements. -> *Auto-Resolved*: We will add the label to the Noble ASCII generation loop.
- **Missing Player 3/4 placeholding**: -> *Auto-Resolved*: We will render only the active players as per current behavior, stacking them 2x2. `combineHorizontal` already successfully batches them by twos.

---

## Work Objectives

### Core Objective
Update `GameRenderer.java` via the code generator script to decouple left and right columns, fixing the excessive vertical padding and matching the reference PNG layout.

### Concrete Deliverables
- Modified `generate_renderer.js` to output the new logical layout in `GameRenderer.java`.

### Definition of Done
- [ ] Running `node generate_renderer.js` generates the java file successfully.
- [ ] The Left column natively concatenates Deck 3, Deck 2, Deck 1, and the Side-by-Side Menu/Bank.
- [ ] The Right column natively concatenates Nobles, Move History, and grouped Players.

### Must Have
- Nobles must display inside standard ASCII card framing.

### Must NOT Have (Guardrails)
- Do NOT change parsing mechanisms or model classes. Confine all edits to `generate_renderer.js`.

---

## Verification Strategy

> **ZERO HUMAN INTERVENTION** — ALL verification is agent-executed. No exceptions.

### Test Decision
- **Infrastructure exists**: Node script driver + Bash scripts.
- **Automated tests**: Tests-after.
- **Framework**: `node test_game.js` runs a headless game.

### QA Policy
Every task MUST include agent-executed QA scenarios using `bash` to run the game and verify standard output. 

---

## Execution Strategy

### Parallel Execution Waves

Wave 1 (Start Immediately):
├── Task 1: Update Noble card rendering block [quick]
└── Task 2: Implement Column-based generation [quick]

Critical Path: Task 1 -> Task 2
Parallel Speedup: NO

---

## TODOs

- [x] 1. Update Noble Rendering to Card Style

  **What to do**:
  - In `generate_renderer.js`, update `renderNoblesHorizontal` to draw Nobles using full 8-line card frames. 
  - Ensure the string `Needs:` or similar cost identifier is placed above the requirement gems.

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Confined syntax string manipulation in a single JS file.
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: Sequential
  - **Parallel Group**: Sequential
  - **Blocks**: 2
  - **Blocked By**: None

  **References**:
  - `generate_renderer.js:174-192` - Current noble rendering loop

  **Acceptance Criteria**:
  - [ ] Node script executes without errors.

  **QA Scenarios**:

  ```
  Scenario: Node script runs cleanly
    Tool: Bash
    Preconditions: Project root
    Steps:
      1. node generate_renderer.js
    Expected Result: Output is "GameRenderer.java generated successfully!"
    Failure Indicators: Stack trace or syntax error.
    Evidence: .sisyphus/evidence/task-1-node-build.txt
  ```

  **Commit**: NO

- [x] 2. Implement Columnar Layout

  **What to do**:
  - In `generate_renderer.js`, rewrite `renderGameStateInternal` to compile all Left elements into a single `List<String> listLeft` array and Right elements into `List<String> listRight`.
  - Compile the bottom of `listLeft` using `combineSideBySide(leftMenu, rightBank)` (rename variables appropriately as Bank is now on the left).
  - Return `combineSideBySideRaw(listLeft, listRight)`.

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: Core UI topological change requiring grid/flex-like array comprehension.
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: Sequential
  - **Blocked By**: 1

  **References**:
  - `generate_renderer.js:68-98` - The layout mapping target

  **Acceptance Criteria**:
  - [ ] Node script generates valid Java layout.
  - [ ] compilation succeeds.

  **QA Scenarios**:

  ```
  Scenario: Compile and Test Game Output Structure
    Tool: Bash
    Preconditions: generate_renderer.js updated
    Steps:
      1. node generate_renderer.js
      2. ./compile.sh OR compile.bat (depending on win/unix)
      3. node test_game.js > output.txt
      4. grep -n "Menu" output.txt && grep -n "Move History" output.txt
    Expected Result: Compile completes, grep shows successful component rendering.
    Failure Indicators: Java compilation errors, or missing UI sections.
    Evidence: .sisyphus/evidence/task-2-compile-test.txt
  ```

  **Commit**: YES
  - Message: `refactor(ui): update GameRenderer to strictly columnar layout per PNG`
  - Files: `generate_renderer.js`

---

## Final Verification Wave

- [x] F1. **Plan Compliance Audit** — `oracle`
  Output: `Must Have [5/5] | Must NOT Have [4/4] | Tasks [2/2] | VERDICT: APPROVE`

- [x] F2. **Scope Fidelity Check** — `deep`
  Output: `Tasks [2/2 compliant] | Contamination [CLEAN] | Unaccounted [CLEAN] | VERDICT: APPROVE`

---

## Success Criteria

### Final Checklist
- [ ] All "Must Have" present
- [ ] All "Must NOT Have" absent
- [ ] Java cleanly compiles
