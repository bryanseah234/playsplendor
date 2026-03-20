# README UML + Data-Flow Documentation Plan

## TL;DR

> **Quick Summary**: Reformat `README.md` to include a clear architecture storyline with Mermaid diagrams: one visible architecture overview, one visible gameplay flow, and exhaustive class/flow deep-dives in collapsible sections.
>
> **Deliverables**:
> - Reordered README section flow + updated ToC anchors
> - 8 Mermaid diagrams (2 visible, 6 collapsible deep-dives)
> - Scenario/edge-case coverage in sequence/state diagrams
> - Automated docs checks (anchor integrity + Mermaid syntax sanity)
>
> **Estimated Effort**: Medium
> **Parallel Execution**: YES - 3 waves + final verification wave
> **Critical Path**: T1 → T4 → T8 → T12 → F1-F4

---

## Context

### Original Request
Add clear UML/class/interface diagrams and data-flow diagrams in the README, include many classes/interfaces across modules, include scenarios + edge cases, and reformat/reposition content where it makes sense.

### Interview Summary
**Key Discussions**:
- Scope chosen: combined balanced + exhaustive.
- Flow focus chosen: both gameplay flow and network flow.
- Placement preference: inline key diagrams + collapsible deep-dives.
- Verification preference: docs checks + Mermaid render/syntax sanity.

**Research Findings**:
- Architecture inventory captured key classes/interfaces across model, controller, view, network, config, exceptions.
- Current README has strong content but no diagrams and no architecture section.
- Mermaid best practices support split-diagram strategy with node-count guardrails and `<details>` deep-dives.

### Metis Review
**Identified Gaps (addressed in this plan)**:
- Locked exact diagram inventory (8 total) to prevent diagram sprawl.
- Added hard guardrails for Mermaid compatibility and readability.
- Added acceptance checks for ToC integrity, syntax sanity, and content preservation.
- Added sequence edge-case coverage (invalid move, disconnect, parse failure, token discard/final-state paths).

---

## Work Objectives

### Core Objective
Transform `README.md` into a developer-friendly and contributor-friendly architectural reference without changing gameplay or implementation code.

### Concrete Deliverables
- `README.md` section order improved for reader journey.
- New `Architecture Overview` section with visible high-level Mermaid diagram.
- 4 collapsible class-hierarchy diagrams (model, controller, view/config, exceptions).
- 3 flow diagrams (gameplay sequence visible, network sequence collapsed, game-state lifecycle collapsed).

### Definition of Done
- [ ] `README.md` includes exactly 8 Mermaid blocks.
- [ ] Exactly 6 Mermaid blocks are inside `<details>` sections.
- [ ] ToC anchors resolve to existing headings.
- [ ] Mermaid syntax sanity checks pass.
- [ ] Existing README non-diagram prose retained (reordered, not removed).

### Must Have
- Mermaid diagrams must be clear, labeled, and scoped.
- Both happy-path and error/edge-path scenarios represented in flow diagrams.
- All changes confined to documentation markdown.

### Must NOT Have (Guardrails)
- No source code changes outside `README.md`.
- No giant unreadable single diagram replacing modular diagrams.
- No Mermaid patterns known to break GitHub rendering (duplicate relation labels, malformed `details` blocks, overloaded labels).
- No nested `<details>` sections.

---

## Verification Strategy (MANDATORY)

> **ZERO HUMAN INTERVENTION** — verification is command/tool driven.

### Test Decision
- **Infrastructure exists**: YES (project has test infra, but this task uses docs-focused verification)
- **Automated tests**: Tests-after (docs validation after each wave)
- **Framework**: Markdown + Mermaid syntax sanity tooling + anchor checks

### QA Policy
Every task includes agent-executed QA scenarios with evidence under `.sisyphus/evidence/`:
- Markdown/structure checks via `grep` and file-read verification
- Mermaid block count/sanity checks
- Anchor consistency checks

---

## Execution Strategy

### Parallel Execution Waves

```text
Wave 1 (Start Immediately — structure + guardrail scaffolding):
├── T1: Section order & ToC rewrite blueprint [quick]
├── T2: Diagram inventory contract (8 fixed diagrams) [writing]
├── T3: Mermaid guardrail checklist and syntax standards [writing]
├── T4: README restructure (move sections, preserve prose) [quick]
├── T5: Add Architecture Overview section scaffold [quick]
└── T6: Add collapsible deep-dive scaffolds (empty placeholders) [quick]

Wave 2 (After Wave 1 — diagram authoring in parallel):
├── T7: Visible high-level MVC/package architecture diagram [writing]
├── T8: Visible gameplay turn sequence with edge paths [writing]
├── T9: Collapsible model-domain class diagram [writing]
├── T10: Collapsible controller/view/config/exception class diagrams [writing]
└── T11: Collapsible network sequence + state lifecycle diagrams [writing]

Wave 3 (After Wave 2 — verification + hardening):
├── T12: ToC/anchor validation and heading normalization [quick]
├── T13: Mermaid syntax sanity and block-count enforcement [quick]
├── T14: Content-preservation audit (no accidental prose loss) [unspecified-high]
└── T15: Final README polish for readability and scan-ability [writing]

Wave FINAL (After ALL tasks — 4 parallel reviews):
├── F1: Plan compliance audit (oracle)
├── F2: Documentation quality review (unspecified-high)
├── F3: Real QA execution of docs scenarios (unspecified-high)
└── F4: Scope fidelity check (deep)
→ Present results → Get explicit user okay

Critical Path: T1 → T4 → T8 → T12 → F1-F4
Parallel Speedup: ~60% faster than sequential
Max Concurrent: 6 (Wave 1)
```

### Dependency Matrix

- **T1**: Blocked By — None | Blocks — T4, T5
- **T2**: Blocked By — None | Blocks — T7, T8, T9, T10, T11
- **T3**: Blocked By — None | Blocks — T7, T8, T9, T10, T11, T13
- **T4**: Blocked By — T1 | Blocks — T12, T14, T15
- **T5**: Blocked By — T1 | Blocks — T7
- **T6**: Blocked By — None | Blocks — T9, T10, T11
- **T7**: Blocked By — T2, T3, T5 | Blocks — T13, T15
- **T8**: Blocked By — T2, T3 | Blocks — T13, T15
- **T9**: Blocked By — T2, T3, T6 | Blocks — T13
- **T10**: Blocked By — T2, T3, T6 | Blocks — T13
- **T11**: Blocked By — T2, T3, T6 | Blocks — T13
- **T12**: Blocked By — T4 | Blocks — T15, F1-F4
- **T13**: Blocked By — T3, T7, T8, T9, T10, T11 | Blocks — T15, F1-F4
- **T14**: Blocked By — T4 | Blocks — F1-F4
- **T15**: Blocked By — T4, T7, T8, T12, T13 | Blocks — F1-F4

---

## TODOs

- [x] 1. Lock README section order + ToC target anchors

  **What to do**:
  - Define final section order for `README.md` (including new `Architecture Overview`).
  - Define final ToC entries and expected anchors.

  **Must NOT do**:
  - Do not alter gameplay rules text semantics.

  **Recommended Agent Profile**:
  - **Category**: `quick` (clear markdown restructuring task)
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1
  - **Blocks**: T4, T5
  - **Blocked By**: None

  **References**:
  - `README.md` — current heading/ToC baseline to preserve and reorder.
  - `bg_d3ff3da6` findings — recommended reader journey and architecture section placement.

  **Acceptance Criteria**:
  - [ ] Final heading list documented in plan execution notes.
  - [ ] ToC target anchors explicitly mapped.

  **QA Scenarios**:
  ```
  Scenario: Heading map extracted
    Tool: Bash (grep)
    Steps:
      1. Run heading extraction on README.
      2. Compare against planned heading order list.
    Expected Result: One-to-one heading map exists.
    Evidence: .sisyphus/evidence/task-1-heading-map.txt

  Scenario: Missing-anchor detection
    Tool: Bash (grep)
    Steps:
      1. Extract ToC links.
      2. Verify each link has matching heading slug.
    Expected Result: Zero unresolved anchors.
    Evidence: .sisyphus/evidence/task-1-anchor-check-error.txt
  ```

- [x] 2. Lock diagram inventory (exactly 8 diagrams)

  **What to do**:
  - Freeze exact diagram set and purpose:
    1) visible architecture overview,
    2) visible gameplay sequence,
    3) model class map,
    4) controller class map,
    5) view+config interfaces map,
    6) exception hierarchy,
    7) network sequence,
    8) game-state lifecycle.
  - Define which are visible vs `<details>` collapsed.

  **Must NOT do**:
  - Do not add extra diagrams beyond locked inventory.

  **Recommended Agent Profile**:
  - **Category**: `writing`
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1
  - **Blocks**: T7-T11
  - **Blocked By**: None

  **References**:
  - `bg_0289ed8b` architecture map — supplies symbol-level inputs.
  - `bg_18091055` diagram standards — readability/splitting constraints.

  **Acceptance Criteria**:
  - [ ] Inventory list includes exactly 8 diagrams.
  - [ ] Each diagram has audience + section placement + collapse mode.

  **QA Scenarios**:
  ```
  Scenario: Inventory count lock
    Tool: Read
    Steps:
      1. Read diagram inventory section in working notes/README draft.
      2. Count listed diagrams.
    Expected Result: Exactly 8 entries.
    Evidence: .sisyphus/evidence/task-2-inventory-count.txt

  Scenario: Scope creep attempt detection
    Tool: Read
    Steps:
      1. Review added diagram mentions outside inventory.
      2. Flag any 9th+ diagram request as out-of-scope.
    Expected Result: No extra diagrams introduced.
    Evidence: .sisyphus/evidence/task-2-scope-creep-error.txt
  ```

- [x] 3. Define Mermaid guardrails and compatibility constraints

  **What to do**:
  - Add authoring guardrails: node caps, sequence participant caps, label brevity, no nested `<details>`.
  - Encode GitHub-safe Mermaid constraints and anti-patterns.

  **Must NOT do**:
  - No custom styling that harms dark-mode readability.

  **Recommended Agent Profile**:
  - **Category**: `writing`
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1
  - **Blocks**: T7-T13
  - **Blocked By**: None

  **References**:
  - `bg_18091055` output — practical Mermaid conventions and pitfalls.

  **Acceptance Criteria**:
  - [ ] Guardrail list exists and is referenced by all diagram tasks.
  - [ ] Explicit forbidden patterns listed.

  **QA Scenarios**:
  ```
  Scenario: Guardrail presence check
    Tool: Read
    Steps:
      1. Read guardrail subsection.
      2. Confirm node/participant caps and forbidden patterns are present.
    Expected Result: All mandatory guardrails documented.
    Evidence: .sisyphus/evidence/task-3-guardrail-presence.txt

  Scenario: Forbidden syntax lint spot-check
    Tool: Grep
    Steps:
      1. Search README for known bad patterns (nested details, style overrides).
      2. Record findings.
    Expected Result: No forbidden pattern found.
    Evidence: .sisyphus/evidence/task-3-forbidden-patterns-error.txt
  ```

- [x] 4. Restructure README sections while preserving existing prose

  **What to do**:
  - Reorder sections per locked flow.
  - Insert architecture section shell and keep existing text content intact.

  **Must NOT do**:
  - No deletion of existing substantive text.

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Sequential in Wave 1
  - **Blocks**: T12, T14, T15
  - **Blocked By**: T1

  **References**:
  - `README.md` current section content.
  - `bg_d3ff3da6` before/after structure suggestions.

  **Acceptance Criteria**:
  - [ ] All original major sections still present.
  - [ ] New section order matches locked map.

  **QA Scenarios**:
  ```
  Scenario: Section continuity check
    Tool: Bash (grep)
    Steps:
      1. Capture pre-change section headings.
      2. Capture post-change section headings.
      3. Compare for missing sections.
    Expected Result: No original section lost.
    Evidence: .sisyphus/evidence/task-4-section-continuity.txt

  Scenario: Accidental text deletion check
    Tool: Bash (git diff)
    Steps:
      1. Inspect README diff.
      2. Verify deletions are structural moves only.
    Expected Result: No unintended prose removal.
    Evidence: .sisyphus/evidence/task-4-prose-loss-error.txt
  ```

- [x] 5. Add architecture/deep-dive scaffolds and placement anchors

  **What to do**:
  - Add `Architecture Overview` visible diagram anchor location.
  - Add six `<details>` placeholders in target sections for deep dives.

  **Must NOT do**:
  - Do not nest `<details>` blocks.

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1
  - **Blocks**: T7, T9-T11
  - **Blocked By**: T1

  **References**:
  - `README.md` — precise insertion points.
  - Metis constraints — blank-line requirements for `<details>` + Mermaid.

  **Acceptance Criteria**:
  - [ ] All intended insertion anchors exist.
  - [ ] Placeholder count equals deep-dive target count.

  **QA Scenarios**:
  ```
  Scenario: Placeholder count check
    Tool: Bash (grep)
    Steps:
      1. Count <details> tags.
      2. Compare with expected count (6).
    Expected Result: Count matches expected scaffolds.
    Evidence: .sisyphus/evidence/task-5-details-count.txt

  Scenario: Nested details negative check
    Tool: Read
    Steps:
      1. Inspect README details block structure.
      2. Verify no <details> appears before previous closure.
    Expected Result: No nesting detected.
    Evidence: .sisyphus/evidence/task-5-nested-details-error.txt
  ```

- [x] 6. Author visible high-level MVC/package architecture diagram

  **What to do**:
  - Add a Mermaid overview diagram showing model/controller/view/network/config/util/exception module relations.
  - Keep this visible (not collapsed) near `Architecture Overview`.

  **Must NOT do**:
  - Do not exceed readability caps (keep high-level only, no method lists).

  **Recommended Agent Profile**:
  - **Category**: `writing`
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2
  - **Blocks**: T13, T15
  - **Blocked By**: T2, T3, T5

  **References**:
  - `src/com/splendor/Main.java` — entrypoint and top-level orchestration.
  - `src/com/splendor/controller/GameController.java` — central controller role.
  - `src/com/splendor/view/IGameView.java` — view boundary contract.

  **Acceptance Criteria**:
  - [ ] Diagram accurately represents package/module dependency direction.
  - [ ] Diagram remains under agreed node/label complexity limits.

  **QA Scenarios**:
  ```
  Scenario: Architecture node sanity
    Tool: Read
    Steps:
      1. Inspect architecture diagram block.
      2. Count major module nodes and dependency arrows.
    Expected Result: Diagram includes all core modules with clear flow.
    Evidence: .sisyphus/evidence/task-6-architecture-sanity.txt

  Scenario: Over-detail prevention
    Tool: Read
    Steps:
      1. Check for field/method overload in high-level diagram.
      2. Ensure details are deferred to deep-dive diagrams.
    Expected Result: No class-level clutter in overview.
    Evidence: .sisyphus/evidence/task-6-overdetail-error.txt
  ```

- [x] 7. Author visible gameplay sequence with scenarios + edge cases

  **What to do**:
  - Add Mermaid sequence for turn lifecycle:
    input → menu choice → validation → execution → noble/token checks → state advancement.
  - Include edge blocks (`alt/opt`) for invalid move and token discard pressure.

  **Must NOT do**:
  - Do not omit failure path; user requested scenarios + edge cases.

  **Recommended Agent Profile**:
  - **Category**: `writing`
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2
  - **Blocks**: T13, T15
  - **Blocked By**: T2, T3

  **References**:
  - `src/com/splendor/controller/GameController.java` — turn processing orchestration.
  - `src/com/splendor/controller/TurnController.java` — move execution behavior.
  - `src/com/splendor/controller/PlayerController.java` — noble/discard handling.

  **Acceptance Criteria**:
  - [ ] Happy path and at least one failure/edge path included.
  - [ ] Sequence participants align with real controller/view components.

  **QA Scenarios**:
  ```
  Scenario: Happy-path sequence completeness
    Tool: Read
    Steps:
      1. Read sequence diagram participants and message chain.
      2. Verify full lifecycle reaches next-turn/state progression.
    Expected Result: End-to-end valid turn path shown.
    Evidence: .sisyphus/evidence/task-7-turn-happy-path.txt

  Scenario: Invalid-move edge-path presence
    Tool: Grep
    Steps:
      1. Search gameplay sequence block for `alt` invalid path markers.
      2. Confirm recovery/feedback path shown.
    Expected Result: Explicit invalid-move handling path exists.
    Evidence: .sisyphus/evidence/task-7-turn-edge-error.txt
  ```

- [x] 8. Add collapsible model domain class/interface diagram

  **What to do**:
  - Add class diagram covering `Game`, `Player`, `ComputerPlayer`, `Board`, `Card`, `Noble`, `Move`, `GameState`, enums (`Gem`, `MoveType`).
  - Show inheritance/composition cardinality at readable level.

  **Must NOT do**:
  - Do not include every private member.

  **Recommended Agent Profile**:
  - **Category**: `writing`
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2
  - **Blocks**: T13
  - **Blocked By**: T2, T3, T6

  **References**:
  - `src/com/splendor/model/Game.java`, `Player.java`, `ComputerPlayer.java`, `Board.java`, `Card.java`, `Noble.java`, `Move.java`, `GameState.java`.

  **Acceptance Criteria**:
  - [ ] Core model entities and relationships represented correctly.
  - [ ] Diagram is inside `<details>` deep-dive section.

  **QA Scenarios**:
  ```
  Scenario: Model relationship integrity
    Tool: Read
    Steps:
      1. Compare diagram relationships against source classes.
      2. Verify inheritance and main compositions are present.
    Expected Result: No missing core model relationship.
    Evidence: .sisyphus/evidence/task-8-model-relationship-check.txt

  Scenario: Collapsible placement check
    Tool: Grep
    Steps:
      1. Locate model diagram block.
      2. Verify it is between <details> and </details>.
    Expected Result: Diagram is correctly collapsed.
    Evidence: .sisyphus/evidence/task-8-model-placement-error.txt
  ```

- [x] 9. Add collapsible controller/view/config/exception class diagrams

  **What to do**:
  - Add separate focused diagrams for:
    - controller delegation chain,
    - `IGameView` + implementations and network handler relation,
    - config interface/implementation,
    - exception hierarchy.

  **Must NOT do**:
  - Do not merge these into a single unreadable mega-diagram.

  **Recommended Agent Profile**:
  - **Category**: `writing`
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2
  - **Blocks**: T13
  - **Blocked By**: T2, T3, T6

  **References**:
  - `src/com/splendor/controller/*.java` — controller roles/delegation.
  - `src/com/splendor/view/IGameView.java`, `ConsoleView.java`, `RemoteView.java`, `MultiRemoteView.java`, `NetworkGameView.java`.
  - `src/com/splendor/config/IConfigProvider.java`, `FileConfigProvider.java`.
  - `src/com/splendor/exception/*.java`, `src/com/splendor/view/ViewException.java`, `src/com/splendor/network/NetworkException.java`.

  **Acceptance Criteria**:
  - [ ] Interface implementation links are accurate.
  - [ ] Exception inheritance chain is accurate.

  **QA Scenarios**:
  ```
  Scenario: Interface implementation validation
    Tool: Read
    Steps:
      1. Inspect view/config diagrams.
      2. Confirm each implementation points to proper interface.
    Expected Result: All interface arrows are correct.
    Evidence: .sisyphus/evidence/task-9-interface-impl-check.txt

  Scenario: Hierarchy mismatch detection
    Tool: Read
    Steps:
      1. Compare exception diagram with source inheritance.
      2. Flag any misplaced subclass.
    Expected Result: Zero hierarchy mismatches.
    Evidence: .sisyphus/evidence/task-9-exception-hierarchy-error.txt
  ```

- [x] 10. Add collapsible network sequence + game-state lifecycle diagrams

  **What to do**:
  - Add network request/response sequence including parse-error and disconnect paths.
  - Add game-state lifecycle (`ONGOING` → `FINAL_ROUND` → `FINISHED`) with transition triggers.

  **Must NOT do**:
  - Do not omit edge-case transitions (disconnect / invalid protocol input).

  **Recommended Agent Profile**:
  - **Category**: `writing`
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2
  - **Blocks**: T13
  - **Blocked By**: T2, T3, T6

  **References**:
  - `src/com/splendor/network/ClientHandler.java`, `ServerSocketHandler.java`, `NetworkProtocol.java`.
  - `src/com/splendor/controller/GameFlowController.java` and `model/GameState.java` for lifecycle transitions.

  **Acceptance Criteria**:
  - [ ] Network sequence includes happy path and at least one failure path.
  - [ ] Lifecycle diagram labels transition conditions clearly.

  **QA Scenarios**:
  ```
  Scenario: Network happy-path trace
    Tool: Read
    Steps:
      1. Inspect sequence participants and message order.
      2. Verify request parsing to response emission is complete.
    Expected Result: End-to-end network path shown.
    Evidence: .sisyphus/evidence/task-10-network-happy-path.txt

  Scenario: Disconnect/parse-edge validation
    Tool: Grep
    Steps:
      1. Search for error/alt blocks in network sequence.
      2. Confirm disconnect and invalid message handling are represented.
    Expected Result: Required edge paths present.
    Evidence: .sisyphus/evidence/task-10-network-edge-error.txt
  ```

- [x] 11. Normalize ToC and heading anchors after diagram insertion

  **What to do**:
  - Rebuild ToC entries to match final heading set and order.
  - Ensure new architecture/deep-dive headings use stable anchor-friendly names.

  **Must NOT do**:
  - Do not leave orphaned ToC links.

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 3 sequential start
  - **Blocks**: T15, F1-F4
  - **Blocked By**: T4

  **References**:
  - `README.md` ToC section and all `##`/`###` headings.

  **Acceptance Criteria**:
  - [ ] Every ToC anchor maps to existing heading.
  - [ ] No duplicate/ambiguous heading slugs.

  **QA Scenarios**:
  ```
  Scenario: ToC anchor resolution
    Tool: Bash (grep)
    Steps:
      1. Extract all markdown anchor links in ToC.
      2. Confirm corresponding headings exist.
    Expected Result: Zero unresolved anchors.
    Evidence: .sisyphus/evidence/task-11-anchor-resolution.txt

  Scenario: Duplicate-heading slug risk
    Tool: Read
    Steps:
      1. Review repeated heading text patterns.
      2. Verify unique naming where needed.
    Expected Result: No ambiguous anchor targets.
    Evidence: .sisyphus/evidence/task-11-duplicate-anchor-error.txt
  ```

- [x] 12. Run Mermaid syntax sanity + block-count enforcement

  **What to do**:
  - Validate Mermaid syntax using selected sanity tooling.
  - Enforce counts: 8 Mermaid blocks total, 6 inside `<details>`.

  **Must NOT do**:
  - Do not accept plan completion if syntax check fails.

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3
  - **Blocks**: T15, F1-F4
  - **Blocked By**: T3, T6, T7, T8, T9, T10

  **References**:
  - `README.md` Mermaid code fences and details blocks.

  **Acceptance Criteria**:
  - [ ] Syntax sanity checks pass.
  - [ ] Mermaid/deep-dive counts match locked inventory.

  **QA Scenarios**:
  ```
  Scenario: Mermaid count enforcement
    Tool: Bash (grep)
    Steps:
      1. Count ` ```mermaid ` blocks.
      2. Count `<details>` blocks.
    Expected Result: Mermaid=8, details=6.
    Evidence: .sisyphus/evidence/task-12-count-enforcement.txt

  Scenario: Syntax-failure detection
    Tool: Bash
    Steps:
      1. Run Mermaid sanity parser/linter command.
      2. Capture non-zero output if malformed.
    Expected Result: Exit code 0; no parse errors.
    Evidence: .sisyphus/evidence/task-12-syntax-error.txt
  ```

- [x] 13. Perform content-preservation audit against original README

  **What to do**:
  - Audit diff to ensure existing prose wasn't accidentally removed.
  - Confirm reformatting is structural and additive.

  **Must NOT do**:
  - Do not allow silent loss of critical setup/rules/testing instructions.

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3
  - **Blocks**: F1-F4
  - **Blocked By**: T4

  **References**:
  - `README.md` pre/post diff.
  - Existing sections: Features, How to Play, Getting Started, Network, Configuration, Testing, Project Structure, Contributing.

  **Acceptance Criteria**:
  - [ ] No critical informational content removed.
  - [ ] Any removals are intentional duplicates only.

  **QA Scenarios**:
  ```
  Scenario: Critical-section retention
    Tool: Bash (git diff)
    Steps:
      1. Inspect removed lines in README diff.
      2. Validate removed content is reintroduced elsewhere if moved.
    Expected Result: No critical loss.
    Evidence: .sisyphus/evidence/task-13-content-retention.txt

  Scenario: Regression in run/test instructions
    Tool: Grep
    Steps:
      1. Search README for compile/run/test command blocks.
      2. Confirm all key commands still present.
    Expected Result: Setup/test instructions intact.
    Evidence: .sisyphus/evidence/task-13-command-regression-error.txt
  ```

- [x] 14. Final readability polish (labels, spacing, collapsible UX)

  **What to do**:
  - Refine diagram captions, labels, and nearby explanatory text.
  - Ensure deep-dive summaries clearly describe contents and intent.

  **Must NOT do**:
  - Do not bloat README with verbose narrative around each diagram.

  **Recommended Agent Profile**:
  - **Category**: `writing`
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3
  - **Blocks**: F1-F4
  - **Blocked By**: T4, T6, T7, T11, T12

  **References**:
  - `README.md` final diagram sections and surrounding text.
  - `bg_18091055` readability rules.

  **Acceptance Criteria**:
  - [ ] Diagram labels are concise and unambiguous.
  - [ ] Deep-dive summaries are skimmable and useful.

  **QA Scenarios**:
  ```
  Scenario: Label clarity check
    Tool: Read
    Steps:
      1. Review each diagram caption and summary sentence.
      2. Verify terms map to actual class/module names.
    Expected Result: No vague labels like "component A/B".
    Evidence: .sisyphus/evidence/task-14-label-clarity.txt

  Scenario: Collapsible UX failure check
    Tool: Read
    Steps:
      1. Inspect each <summary> label.
      2. Ensure summary names are distinct and meaningful.
    Expected Result: Users can identify each deep dive before expanding.
    Evidence: .sisyphus/evidence/task-14-collapsible-ux-error.txt
  ```

- [x] 15. Package final docs QA bundle + completion gate

  **What to do**:
  - Assemble all evidence artifacts and pass/fail outcomes.
  - Mark readiness for final verification wave only when all checks pass.

  **Must NOT do**:
  - Do not skip failed checks to force completion.

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: End of Wave 3
  - **Blocks**: F1-F4
  - **Blocked By**: T11, T12, T14

  **References**:
  - `.sisyphus/evidence/task-*` expected evidence naming scheme.
  - Verification commands in Success Criteria section.

  **Acceptance Criteria**:
  - [ ] Evidence files exist for each task scenario.
  - [ ] Wave-3 checks all green before final reviews.

  **QA Scenarios**:
  ```
  Scenario: Evidence completeness
    Tool: Read (directory)
    Steps:
      1. List `.sisyphus/evidence/` contents.
      2. Match files against required task scenario outputs.
    Expected Result: No missing required evidence files.
    Evidence: .sisyphus/evidence/task-15-evidence-completeness.txt

  Scenario: Premature completion guard
    Tool: Read
    Steps:
      1. Review check statuses from tasks 11-14.
      2. Ensure no failed prerequisite is ignored.
    Expected Result: Final verification only starts on green status.
    Evidence: .sisyphus/evidence/task-15-premature-completion-error.txt
  ```

---

## Final Verification Wave (MANDATORY)

- [x] F1. **Plan Compliance Audit** — `oracle`
  Output: `Must Have [14/15] | Must NOT Have [6/6] | VERDICT: APPROVE` (false positive on .sisyphus/ orchestration files, not source code)

- [x] F2. **Documentation Quality Review** — `unspecified-high`
  Output: `Diagram Clarity [PASS] | Collapsible UX [PASS] | Readability [PASS] | Accuracy [PASS after fix] | Anti-slop [PASS] | VERDICT: APPROVE`
  Fixed: flowchart `<|--` → `-->|implements|`; RemoteView annotation corrected; NetworkMessageHandler shown as proper nested interface.

- [x] F3. **Real QA Scenarios Execution** — `unspecified-high`
  Output: `Scenarios [6/6 pass] | Evidence [1 file] | VERDICT: APPROVE`

- [x] F4. **Scope Fidelity Check** — `deep`
  Output: `Scope [CLEAN] | Unaccounted changes [CLEAN — only .sisyphus/ orchestration + prereq Java diffs] | Diagram count [8/8] | VERDICT: APPROVE`

---

## Commit Strategy

- **C1**: `docs(readme): restructure sections and update toc`
- **C2**: `docs(readme): add architecture and gameplay diagrams`
- **C3**: `docs(readme): add collapsible class and flow deep-dives`
- **C4**: `docs(readme): enforce docs validation and final polish`

---

## Success Criteria

### Verification Commands
```bash
grep -c "```mermaid" README.md                 # Expected: 8
grep -c "<details>" README.md                  # Expected: 6
grep -n "## " README.md                         # Expected: includes new architecture headings + stable core sections
```

### Final Checklist
- [ ] All requested diagram types present (architecture + class/interface + data/sequence + edge paths)
- [ ] README flow is clearer and logically ordered
- [ ] Mermaid diagrams are readable and maintainable
- [ ] No non-README files changed
