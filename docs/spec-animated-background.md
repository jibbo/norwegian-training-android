# Specification: Replace Main Screen Background Video with Hardcoded Animated Gradient

**Project:** norwegian-training-android
**Component:** `MainView` (workout session screen)
**Status:** Ready for implementation

---

## 1. Background & Goal

`MainView` (`app/src/main/java/com/github/jibbo/norwegiantraining/main/MainActivityComposables.kt:71`) currently renders a looping background video via `VideoBackground(R.raw.bg)`:

- `VideoBackground` / `ExoplayerExample` (`components/SharedComposables.kt:47,61`) play `res/raw/bg.mp4` (3.7 MB) through an ExoPlayer `AndroidView`, dimmed by a 90%-alpha black overlay
- Costs: APK size, video decode battery/CPU usage, ExoPlayer runtime dependency

**Goal:** Remove the video and replace it with a fully procedural Compose animation: a **visible lime glow drifting over a near-black base**, on a seamless eased loop. Fully remove all video/ExoPlayer artifacts.

**Decisions agreed during brainstorming:**

| # | Decision | Choice |
|---|---|---|
| 1 | Animation type | Animated gradient (no asset files) |
| 2 | Palette | Dark base + lime tint (brand colors) |
| 3 | Motion | Drifting radial glow |
| 4 | Removal scope | Full (components + asset + dependency) |
| 5 | Drift pattern | Eased seamless loop (20 s cycle) |
| 6 | Previews | Always rendered (guard removed) |
| 7 | Intensity | Clearly visible, contrast-safe |

---

## 2. Visual Design Requirements

### 2.1 Composition (back to front, inside `Scaffold` content slot)

1. **Base layer** — full-screen `Box` painted with theme `Black` (`0xFF070D0D`, `ui/theme/Color.kt:10`)
2. **Glow layer** — radial gradient over the base:
   - Center color: `Primary` (`0xFFCFFF04`) at **alpha 0.35**
   - Edge color: `Primary` at **alpha 0.0** (fades smoothly to base)
   - Brush: `Brush.radialGradient(colorStops, center, radius)`
   - Radius: **0.7 × screen diagonal** (`sqrt(w² + h²)`, measured via `onSizeChanged` or `BoxWithConstraints`)
   - Center position: x ∈ **25%–75%** of width, y ∈ **15%–85%** of height (diagonal drift)

### 2.2 Animation behavior

- Single phase value `0f → 1f`, driven by `Animatable` with:
  `infiniteRepeatable(tween(durationMillis = 20_000, easing = EaseInOut), RepeatMode.Reverse)` in a `LaunchedEffect(Unit)`
- Glow center x = `lerp(0.25f, 0.75f, phase) * width`, y = `lerp(0.15f, 0.85f, phase) * height`
- Result: slow diagonal drift, eases at both ends, **seamless infinite loop** (phase 1 → 0 is a no-op due to `Reverse`)
- API note: use `Animatable.animateTo(...)` with the repeatable spec — do **not** use `rememberInfiniteTransition` / `InfiniteTransitionScope` (deprecated in recent BOMs; project uses BOM `2026.06.01`). If the toolchain flags `infiniteRepeatable` as deprecated on this BOM, fall back to computing the eased phase from `withFrameNanos` frame time into a `mutableStateOf` — visual result must be identical.

### 2.3 Contrast (verified against current foreground)

Composite color at glow peak ≈ `0.35·(207,255,4) + 0.65·(7,13,13)` ≈ dark olive `(77,91,8)`:

| Foreground element | Color | Contrast over glow peak |
|---|---|---|
| Instructions / countdown / header text | White | ≈ 7:1 ✅ |
| Main button (idle) | Black on lime `Primary` | unaffected (opaque button) ✅ |
| Main button (running) | White on `Red` | unaffected ✅ |
| Skip text | Underlined White | ≈ 7:1 ✅ |

No dimming overlay is needed (the base color replaces the old 0.9-alpha black box).

### 2.4 Accessibility

- If `LocalAccessibilityManager.current?.isReduceMotionEnabled == true`, render the glow **static** at center (phase frozen at `0.5f`) — no drift animation. *(New requirement added for spec completeness; veto if unwanted.)*

### 2.5 Performance

- One radial gradient redrawn per frame — trivial GPU cost; strictly cheaper than video decoding
- Scope invalidation: the animated gradient must live in its **own `Box`** so per-frame redraws don't invalidate the content `Column`
- Behavior on rotation/recomposition: phase restarts from 0 — acceptable, no persistence needed

---

## 3. Architecture & Code Changes

### 3.1 `components/SharedComposables.kt`

- **Delete** `VideoBackground` (line 47) and `ExoplayerExample` (line 61)
- **Add** `AnimatedBackground(modifier: Modifier = Modifier)` at the same location (public, like its file siblings)
- **Remove now-unused imports** (verify each with IDE/lint after deletion):
  - `androidx.annotation.RawRes` (line 5)
  - ExoPlayer: `ExoPlayer`, `MediaItem`, `Player`, `AspectRatioFrameLayout`, `StyledPlayerView` (lines 39–43)
  - `androidx.compose.ui.viewinterop.AndroidView` (line 34)
  - `androidx.core.net.toUri` (line 35)
  - `ui.theme.Black` (line 37) — only used by the deleted overlay
- **Keep** `Toolbar` and `AnimatedToolbar` untouched (`LocalContext`/`LocalDensity`/`animateFloatAsState` etc. remain in use)

### 3.2 `main/MainActivityComposables.kt`

- Replace lines 70–72:
  ```kotlin
  if (!LocalInspectionMode.current) {
      VideoBackground(R.raw.bg)
  }
  ```
  with:
  ```kotlin
  AnimatedBackground()
  ```
  (no inspection-mode guard — always render, per decision 6)
- Keep it **before** the content `Column`, ignoring `innerPadding` exactly as the old `VideoBackground` did (background must extend under system bars)
- Remove imports: `VideoBackground` (line 47), `LocalInspectionMode` (line 37 — sole usage was the guard)
- `GreetingPreview` (line 315) now shows the animated background automatically — no preview changes needed

### 3.3 `home/HomeComposables.kt`

- Remove stale unused import `VideoBackground` (line 57). No other Home changes — Home never actually rendered the video.

### 3.4 Build files & assets

| File | Change |
|---|---|
| `app/build.gradle.kts:103` | Delete `implementation(libs.exoplayer.core)` |
| `gradle/libs.versions.toml:18` | Delete `exoplayer = "2.19.1"` |
| `gradle/libs.versions.toml:55` | Delete `exoplayer-core = { ... }` library entry |
| `app/src/main/res/raw/bg.mp4` | Delete file (3.7 MB) — only references were the two removed call sites |

**No ProGuard/R8 rule changes required** (verified: no ExoPlayer references in `*.pro`/`*.xml`). Release minification (`isMinifyEnabled = true` + `isShrinkResources = true`) will drop ExoPlayer classes, further shrinking the release APK.

### 3.5 Tunable constants (implementation defaults, all in one place)

```
GLOW_DURATION_MS   = 20_000
GLOW_EASING        = EaseInOut
GLOW_REPEAT        = RepeatMode.Reverse
GLOW_COLOR         = Primary      (ui/theme/Color.kt)
GLOW_ALPHA         = 0.35f
BASE_COLOR         = Black        (ui/theme/Color.kt)
GLOW_RADIUS_FACTOR = 0.7f  (× screen diagonal)
GLOW_X_RANGE       = 0.25f..0.75f (fraction of width)
GLOW_Y_RANGE       = 0.15f..0.85f (fraction of height)
```

---

## 4. Error Handling

No new failure modes are introduced — the animation is purely procedural (no I/O, no resource loading, no async). Notes:

- No `rememberLottieComposition`-style composition failures to handle (that pattern is Lottie-only)
- Animation survives config changes by normal recomposition; no state to persist
- The only defensive code: null-safe read of `LocalAccessibilityManager` (reduced-motion check)

---

## 5. Testing Plan

**No new unit tests required** (change is 100% Compose UI; no view-model/domain logic touched). Verification:

| # | Check | Command / method | Pass criteria |
|---|---|---|---|
| 1 | Compile | `./gradlew :app:compileDebugKotlin` | Zero errors; no dangling refs to `VideoBackground`, `ExoplayerExample`, `R.raw.bg`, ExoPlayer |
| 2 | Lint | `./gradlew :app:lintDebug` | No new warnings (unused imports, deprecations) |
| 3 | Unit suite | `./gradlew :app:testDebugUnitTest` | All existing tests green |
| 4 | Debug APK | `./gradlew :app:assembleDebug` | Builds; APK ≈ 3.7 MB smaller (bg.mp4) |
| 5 | Release APK | `./gradlew :app:assembleRelease` | R8/minify succeeds without ExoPlayer |
| 6 | Manual — main screen | Launch a workout | Glow drifts diagonally, eases at ends, loops seamlessly at 20 s; no flicker/jumps |
| 7 | Manual — contrast | Watch glow pass under text | All text/buttons remain legible at glow peak (Section 2.3) |
| 8 | Manual — preview | `GreetingPreview` in Android Studio | Animated background renders in preview |
| 9 | Manual — a11y | System setting → Reduce motion ON | Glow static, centered |
| 10 | Manual — battery sanity | Timer running 5+ min | No perceptible extra drain vs. before (video decode removed) |

---

## 6. Acceptance Criteria

1. No `VideoBackground`, `ExoplayerExample`, ExoPlayer dependency, or `bg.mp4` remains anywhere in the repo
2. Main screen background is a procedural lime glow over near-black, animated per §2.2, visible per §2.3
3. Reduced-motion users get a static glow
4. All commands in §5 (checks 1–5) pass; manual checks 6–10 confirmed on device/emulator
5. Debug and release builds both succeed

## 7. Out of Scope

- No changes to `MainActivity`, `MainViewModel`, `UiState`, timers, or any other screen
- No new dependencies
- No screenshot-test infrastructure (none exists in the project)
- Home screen background (it never rendered the video)
