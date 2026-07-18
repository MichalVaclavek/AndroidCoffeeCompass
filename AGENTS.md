# CoffeeCompass agent guide

This file is for agentic coding assistants working in this repository.
Keep changes consistent with the existing Java/Android codebase.

## Quick project map
- App module: `app`
- Main package: `cz.fungisoft.coffeecompass2`
- UI activities: `cz.fungisoft.coffeecompass2.activity`
- Services: `cz.fungisoft.coffeecompass2.services`
- Async tasks: `cz.fungisoft.coffeecompass2.asynctask`
- Widgets: `cz.fungisoft.coffeecompass2.widgets`
- Utils/helpers: `cz.fungisoft.coffeecompass2.utils`

## Build, lint, and test commands
Use the Gradle wrapper in repo root.

- Build debug APK: `gradlew.bat assembleDebug`
- Build release AAB/APK: `gradlew.bat assembleRelease`
- Clean: `gradlew.bat clean`
- Lint (module): `gradlew.bat :app:lint`
- Lint debug only: `gradlew.bat :app:lintDebug`
- Unit tests (all): `gradlew.bat :app:testDebugUnitTest`
- Instrumented tests (all): `gradlew.bat :app:connectedDebugAndroidTest`

### Run a single unit test
- Class: `gradlew.bat :app:testDebugUnitTest --tests "cz.fungisoft.coffeecompass2.YourTestClass"`
- Method: `gradlew.bat :app:testDebugUnitTest --tests "cz.fungisoft.coffeecompass2.YourTestClass.yourTest"`

### Run a single instrumented test
- Class:
  `gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=cz.fungisoft.coffeecompass2.YourInstrumentedTest`
- Method:
  `gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=cz.fungisoft.coffeecompass2.YourInstrumentedTest#yourTest`

Notes:
- Release build uses a local keystore path; expect failures on machines that do not have it.
- Default workflow is debug builds unless release signing is explicitly required.

## Code style and conventions

### Language and formatting
- Java 17 source/target is configured in Gradle; use Java 17 features cautiously and consistently.
- Indentation: 4 spaces; no tabs.
- Braces on same line (K&R style).
- Keep methods and blocks compact; extract helpers for long methods.
- Use `@Override` on all overridden methods.
- Avoid long lines when reasonable; break chained calls across lines as done in existing code.
- Prefer explicit types for clarity in public APIs.
- Minimize package-private fields; prefer `private` with accessors or helpers.

### Imports
- Order follows current style:
  1) `import static ...`
  2) `android.*`
  3) `androidx.*`
  4) third-party (`com.*`, `org.*`)
  5) `java.*`
  6) project packages (`cz.fungisoft.coffeecompass2.*`)
- Grouping is separated by blank lines.

### Types and nullability
- Use `@NonNull`/`@Nullable` annotations when parameters or return values can be null.
- Prefer `final` for parameters and local variables that are not reassigned.
- Use primitive types when null is not meaningful.

### Naming
- Classes: `UpperCamelCase` (e.g., `GetCoffeeSitesInRangeAsyncTask`).
- Methods/fields: `lowerCamelCase`.
- Constants: `UPPER_SNAKE_CASE` and `private static final`.
- `TAG` constants are used for logging and match class names.
- Use descriptive domain naming (CoffeeSite, Stars, Notification, etc.).

### Comments and docs
- Use Javadoc-style comments for public classes/methods, especially for services and async tasks.
- Inline comments are used for non-obvious logic or platform caveats.
- Mixed Czech/English exists; prefer English for new code unless adjacent code is Czech.

### Resources and localization
- Keep user-facing strings in `res/values/strings.xml` and reference with `R.string.*`.
- Avoid hardcoded UI text in Java; reuse existing string resources where possible.
- When adding icons/colors/dimens, use existing resource conventions.

### Error handling and logging
- Log with `Log.i`/`Log.e` and consistent `TAG` values.
- Do not swallow exceptions silently; log or propagate to listener callbacks.
- Where callbacks exist (e.g., REST async tasks), report errors via listener methods.
- Prefer clear error messages to ease debugging.

### Threading and async work
- Do not perform network or DB operations on the UI thread.
- Existing patterns use `AsyncTask`, Services, and listeners; follow the same pattern unless refactoring.
- When using Retrofit callbacks, call back to service/listener on response or failure.
- Ensure callbacks that touch UI run on the main thread.

### Android and UI patterns
- Activities extend base classes in `activity` package; keep responsibilities clear.
- Prefer helpers in `activity.data` or `utils` instead of duplicating logic.
- Use Material components where already present.
- Keep resource IDs and strings in `res` and use `R.string.*` for user-visible text.
- ButterKnife is used for view binding in existing screens; follow local patterns.
- Avoid large activities; move business logic into services, repositories, or helpers.

### Data and persistence
- Room is used for local DB. Entities live in `entity`, repositories in `entity.repository`.
- Use repository classes for DB access rather than raw queries in UI code.
- Converters are in `entity.repository.DbDataConverters` and `DbDataListsConverters`.
- Keep DB operations off the UI thread; prefer async/repository patterns.

### Networking
- Retrofit is standard; base URLs are configured in `BuildConfig` fields in `app/build.gradle`.
- Use `Utils.getOkHttpClientBuilder()` for consistent TLS/timeout settings.
- Prefer `GsonBuilder` configuration already used (date format, exclude fields without expose).
- Avoid hardcoding URLs; add new endpoints to `BuildConfig` if needed.
- Log request failures with context to aid support.

### Permissions and location
- Use runtime permission checks before location access; follow patterns in activities.
- Use `LocationService` and related connectors for updates instead of ad-hoc listeners.
- Keep location accuracy thresholds and ranges centralized in activity or helper constants.

### Security and secrets
- Do not commit new secrets, tokens, or keystore credentials.
- Treat `app/build.gradle` signing config as local-only unless instructed otherwise.
- Avoid logging sensitive user data or auth tokens.

### Tests
- Unit tests live in `app/src/test/java`.
- Instrumented tests live in `app/src/androidTest/java`.
- Keep test class names and packages aligned with production packages.
- Use JUnit4 (`@Test`) consistent with existing tests.

## Project-specific notes
- BuildConfig defines API URLs for debug/release; keep new endpoints aligned.
- There are hard-coded release keystore values in `app/build.gradle`; do not change unless instructed.
- ButterKnife is used in existing code; follow existing usage where present.
- Some logs and comments are Czech; prefer English unless extending adjacent Czech sections.
- Debug endpoints in `app/build.gradle` use a dedicated test server; keep that separation.

## Cursor/Copilot rules
- No Cursor rules (`.cursor/rules/` or `.cursorrules`) found.
- No Copilot instructions (`.github/copilot-instructions.md`) found.
