# Home Screen Layout Redesign

## Goal
Redesign the home screen layout to match the screenshot's structure: a "Next Up" section with the recommended workout followed by an "All Workouts" 2-column grid.

## Current State
- **Layout:** Horizontal-scrolling rows grouped by difficulty (BEGINNER, INTERMEDIATE, EXPERT)
- **Cards:** Fixed 200dp width, gray background, glow/border for recommended
- **Sections:** Difficulty labels act as section headers
- **Background:** Runner illustration at bottom-right

## Target State (from screenshot)
- **Header:** "Welcome [Name]" + 2 icon buttons (charts, settings) — no change
- **"Next Up" section:** Heading + 1 larger card (recommended workout)
- **"All Workouts" section:** Heading + 2-column grid of remaining cards
- **Cards:** Wider, filling grid columns (no fixed width)
- **Ignore:** "Just Do It" hero card (screenshot top section)

## Implementation Plan

### Files to modify

1. **`app/src/main/java/com/github/jibbo/norwegiantraining/home/HomeComposables.kt`**
   - Rewrite `Workouts()`: replace horizontal-scrolling rows with a `LazyColumn` containing:
     - "Next Up" section (if a recommended workout exists) with a single larger card
     - "All Workouts" section with a `LazyVerticalGrid` (2 columns) for the remaining workouts
   - Update `WorkoutCard()`:
     - Remove fixed `width(200.dp)`, make it width-flexible for grid use
     - Remove glow/border logic (recommended workout is already surface in "Next Up")
     - Keep content: name, duration, calories

2. **`app/src/main/java/com/github/jibbo/norwegiantraining/data/FakeRepos.kt`**
   - Uncomment `FakeWorkoutRepo.getAll()` so the `@Preview` works

### Files NOT changed
- `HomeViewModel.kt` — data flow is unchanged
- `UiState.kt` — already exposes `recommendedWorkoutId` and `hasProgressed`
- Any data/repository layer — no logic changes

### Technical details

```kotlin
// In Workouts():
val state = viewModel.uiStates.collectAsState().value as UiState.Loaded
val recommendedWorkout = state.workouts.values.flatten()
    .find { it.id == state.recommendedWorkoutId }
val otherWorkouts = state.workouts.values.flatten()
    .filter { it.id != state.recommendedWorkoutId }.sortedBy { it.id }

// "Next Up"
Text(text = "Next Up", style = Typography.bodyMedium, modifier = Modifier.padding(bottom = 12.dp))
if (recommendedWorkout != null) {
    WorkoutCard(recommendedWorkout, state.recommendedWorkoutId, state.recommendedLabel, viewModel)
}

// "All Workouts"
Text(text = "All Workouts", style = Typography.bodyMedium, modifier = Modifier.padding(bottom = 12.dp))
LazyVerticalGrid(
    columns = GridCells.Adaptive(minSize = 150.dp),
    verticalArrangement = Arrangement.spacedBy(6.dp),
    horizontalArrangement = Arrangement.spacedBy(6.dp),
    modifier = Modifier.padding(horizontal = 6.dp)
) {
    items(otherWorkouts.size, { it }) { index ->
        WorkoutCard(otherWorkouts[index], null, 0, viewModel)
    }
}
```

Note: The `LazyVerticalGrid` code already exists commented out in the current file (lines 156-166) — it just needs to be uncommented and integrated.

### Difficulty labels
The screenshot shows grouped cards with section headings (Beginner, Intermediate, etc.). Within the "All Workouts" grid, we can render a small difficulty label before each group's cards (e.g., filter by difficulty and insert a section header). This preserves the existing grouping logic without reverting to horizontal scrolling.

## Steps
1. Patch `HomeComposables.kt` — rewrite `Workouts()` and update `WorkoutCard()`
2. Patch `FakeRepos.kt` — uncomment `FakeWorkoutRepo.getAll()`
3. Verify build: `./gradlew app:compileDebugKotlin`
