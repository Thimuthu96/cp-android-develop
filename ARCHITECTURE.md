# Element X Android — Architecture & Customization Guide

> Internal reference for engineers working on / customizing this codebase.
> Element X Android is a next-generation Matrix client. The heavy lifting (sync, E2E
> encryption, key management, local storage) is done by a native **Rust core**
> (`matrix-rust-sdk`); this Android project is a **presentation + session-management**
> layer around it.

---

## 1. Snapshot

| | |
|---|---|
| App ID | `io.element.android.x` |
| Min SDK | 24 (FOSS) / 33 (Enterprise) |
| Target / Compile SDK | 37 |
| JVM / Kotlin | Java 21 / Kotlin 2.4 (KSP) |
| Modules | ~266 Gradle modules |
| License | AGPL-3.0 **OR** commercial Element license (dual) |

---

## 2. Technology Stack

| Concern | Technology |
|---|---|
| UI | Jetpack Compose (BOM 2026.08), Material3, **single-Activity, full Compose** |
| Design system | **Compound** (Element token system) + `libraries/designsystem` widgets |
| Navigation | **Appyx 1.7** — model-driven node tree |
| State / presenters | **Molecule 2.2** — Compose-runtime presenters |
| Dependency injection | **Metro 1.4** — compile-time DI (migrated from Dagger/Anvil) |
| Matrix core | `matrix-rust-sdk` via UniFFI Kotlin bindings (`.aar`), rustls TLS |
| Async | Coroutines/Flow 1.11, kotlinx immutable collections |
| Media | Media3/ExoPlayer 1.11, Coil 3 |
| Networking | Retrofit + OkHttp + kotlinx.serialization |
| Storage | SQLDelight 2.3 + SQLCipher (encrypted), DataStore (prefs) |
| Push | Firebase FCM (`gplay`) + UnifiedPush (`fdroid`) |
| Maps / Calls | MapTiler+MapLibre (location) / Element Call (WebView) |
| Rich text | `wysiwyg` (Element WYSIWYG editor) |
| Analytics | PostHog + Sentry (opt-in) |
| Background | WorkManager (Metro-based WorkerFactory) |
| Testing | Molecule/Turbine (presenters), Paparazzi+Showkase (screenshots), Maestro (E2E), Kover (coverage), Konsist (arch rules), Detekt |

---

## 3. Module Architecture

Strict dependency direction:

```
app  ──▶  appnav  ──▶  features/*/api      libraries/*/api
 │                        ▲                     ▲
 └──▶ features/*/impl ─────┘   libraries/*/impl ─┘
                     └──▶ libraries/matrix/api ──▶ Rust SDK
```

Most features/libraries use a **3-module split**:

- `api` — public interfaces, data classes, and an **`EntryPoint`** (the only surface other modules see)
- `impl` — internal Node/Presenter/View/State/Event + DI wiring
- `test` — fakes & test utilities

**Rules:**
- Features never depend on other features' `impl` — only on `api`. Cross-feature glue lives in `appnav`.
- **The Rust SDK never leaks into UI/feature code.** `libraries/matrix/api` exposes clean Kotlin types (`MatrixClient`, `JoinedRoom`, `RoomInfo`, `Timeline`); `libraries/matrix/impl` maps raw FFI types into them. This boundary is the most important invariant in the repo.

**Top-level dirs:** `app` (application + DI graphs), `appnav` (navigation tree), `features/*`, `libraries/*`, `services/*`, `appconfig` (customization constants), `plugins` (Gradle convention plugins + build-time config), `appicon/*` (launcher icons per variant), `enterprise` (private submodule).

---

## 4. Screen Pattern: Node → Presenter → View

There is **no ViewModel** — Compose handles configuration changes. Pattern inspired by Slack's Circuit.

Per screen `Foo`: `FooNode`, `FooPresenter`, `FooView`, `FooState`, `FooEvent`, `FooStatePreviewParam`, `FooPresenterTest`.

| Role | Contract |
|---|---|
| `Presenter<State>` | `fun interface` with one `@Composable fun present(): State`. Molecule folds `Event`s into immutable `State`; collects SDK flows via `collectAsState`/`produceState`. |
| `View` | **Stateless** `@Composable` rendering `State`, emitting `Event`s. |
| `Node` | Appyx node — builds presenter (DI), calls `present()`, pipes state into view, owns navigation + DI sub-graph. |

Presenter and View **never talk directly** — only via `State` (down) and `Event` (up). Events travel up through a single `eventSink: (Event) -> Unit` field on the `State`.

**Composite presenters:** big screens compose sub-presenters recursively (mirroring the Compose tree). Example: `MessagesPresenter` composes ~14 sub-presenters (timeline, composer, action list, reactions, pinned banner, moderation…), calls each `present()`, and forwards events to their sub-state event sinks.

```kotlin
data class MessagesState(
    val roomId: RoomId,
    val composerState: MessageComposerState,
    val timelineState: TimelineState,
    // …aggregated sub-states…
    val eventSink: (MessagesEvent) -> Unit,
)

sealed interface MessagesEvent {
    data class ToggleReaction(val emoji: String, val id: EventOrTransactionId) : MessagesEvent
    data object MarkAsFullyReadAndExit : MessagesEvent
}
```

---

## 5. Navigation (Appyx node tree)

Single Activity → single Compose tree → hierarchy of Appyx **flow nodes**, each holding a
backstack of typed `@Parcelize NavTarget`s + a `resolve()` that builds child nodes:

```
ElementXApplication (builds AppGraph)
  └─ MainActivity (NodeActivity — the only Activity)
       └─ MainNode
            └─ RootFlowNode              Splash / Login / LoggedIn / SignedOut / BugReport
                 └─ LoggedInAppScopeFlowNode   ← creates the SessionScope DI graph
                      └─ LoggedInFlowNode        FTUE / Home / Room / Settings / CreateRoom…
                           └─ RoomFlowNode         resolves & joins a room
                                └─ JoinedRoomFlowNode
                                     └─ JoinedRoomLoadedFlowNode  Messages / Details / Members…
```

- `RootFlowNode` watches session state (`SessionStore`) and swaps the backstack root between Login / LoggedIn / SignedOut. Also resolves deep links & notification intents (`IntentResolver`).
- `LoggedInFlowNode` caps the room backstack at 5 (`MAX_ROOM_NODE_COUNT`) to bound memory.
- `BaseFlowNode<NavTarget>` (`libraries/architecture`) unifies Appyx `BackStack + Overlay + PermanentNavModel`.

---

## 6. Dependency Injection (Metro)

Three nested scopes, each a Metro `@GraphExtension` owned by an Appyx node:

| Scope | Graph | Created by | Lifetime |
|---|---|---|---|
| `AppScope` | `AppGraph` | `ElementXApplication` | whole app |
| `SessionScope` | `SessionGraph` | `LoggedInAppScopeFlowNode` | one logged-in session |
| `RoomScope` | `RoomGraph` | `RoomFlowNode`'s joined children | one open room |

Annotations:
- `@Inject` constructor injection; `@AssistedInject` + `@Assisted` + `@AssistedFactory` for runtime args (navigators, IDs, Appyx `buildContext`/`plugins`).
- `@ContributesBinding(Scope::class)` — bind a service impl into a scope.
- `@ContributesNode(Scope::class)` — custom annotation; KSP generates an `AssistedNodeFactory` and registers the node into the scope's multibound node-factory map. `Node.createNode<FooNode>()` resolves it.
- Nodes reach their graph via `Node.bindings<T>()`, which walks the Appyx parent chain for `DependencyInjectionGraphOwner`s.

---

## 7. Functionality Inventory

**Auth / Onboarding:** `login` (server select, password, QR, OIDC/SSO via `oauth`), `ftue`, `migration`, `linknewdevice`, `signedout`, `logout`, `deactivation`, `announcement`.

**Messaging (`messages` is the core):** timeline (send/read, replies, threads, edits, reactions, receipts, typing), rich composer + mentions + `/slashcommands`, attachments (image editor, video), voice messages, pinned messages (banner + list), `poll`, `location`, `forward`, `share`, report, `viewfolder`.

**Rooms & Spaces:** `home` (room list), `space`, `createroom`, `startchat`, `joinroom`, `leaveroom`, `roomdirectory`, `roomaliasresolver`, `roomdetails`/`roomdetailsedit`, `invite`/`invitepeople`, `knockrequests`, `rolesandpermissions`, `roommembermoderation`, `reportroom`, `userprofile`.

**Calls:** `call` (Element Call/WebView), `roomcall` (in-room banner).

**Security / Encryption:** `verifysession` (cross-signing), `securebackup` (key backup + recovery key), `securityandprivacy`, `lockscreen` (PIN/biometric), `contentscanner` (enterprise AV).

**Settings & Infra:** `preferences` (settings hub, labs, dev options), `analytics`, `licenses`, `rageshake`, `networkmonitor`, `cachecleaner`.

---

## 8. Key Libraries & Services

- `libraries/matrix` — Rust SDK wrapper (the boundary). Start here for anything Matrix.
- `libraries/compound` (tokens) + `libraries/designsystem` (widgets) — build all UI from these.
- `libraries/push` + `pushproviders` (firebase/unifiedpush) + `pushstore` — notification pipeline: push → `SyncOnNotifiableEvent` → SDK fetches event → notification shown.
- Media: `mediaplayer`, `voiceplayer`, `voicerecorder`, `audio`, `mediaupload`, `mediapickers`, `mediaviewer`, `matrixmedia`.
- Security/storage: `cryptography` (Keystore AES), `encrypted-db` (SQLCipher), `session-storage`, `oauth`.
- Text/format: `textcomposer` (wysiwyg), `eventformatter`, `dateformatter`, `emoji`, `usersearch`, `slashcommands`.
- Infra: `featureflag`, `deeplink`, `network`, `permissions`, `preferences`, `workmanager`, `wellknown`, `qrcode`.
- `services/`: `analytics` + `analyticsproviders` (posthog/sentry), `apperror`, `appnavstate`, `toolbox`.

---

## 9. Customization / White-label Playbook

### 9.1 The intended customization surface

| What | Where |
|---|---|
| App ID, app name, service URLs, keys, push toggles | `plugins/src/main/kotlin/config/BuildTimeConfig.kt` |
| Version & SDK levels | `plugins/src/main/kotlin/Versions.kt` |
| Feature behavior constants | `appconfig/*` (`AuthenticationConfig`, `TimelineConfig`, `MessageComposerConfig`, `OnBoardingConfig`, `ElementCallConfig`, `NotificationConfig`, …) |
| Default homeserver | `appconfig/.../AuthenticationConfig.kt` → `MATRIX_ORG_URL` (fallback account provider in `features/login/impl/.../accountprovider/AccountProviderDataSource.kt`) |
| Launcher icon | `appicon/element/src/*/res/mipmap-*` (replace `.webp`s). Enterprise variant uses `appicon/enterprise`. |
| Colors / typography | `libraries/compound` — `ElementTheme(...)` accepts `compoundLight`/`compoundDark`/`materialColorsLight/Dark`; tokens generated under `compound/tokens/generated`. |
| Strings | New English strings go in `temporary.xml`. **Never edit `localazy.xml`** (auto-generated). |
| Product flavor | `gplay` (Firebase) vs `fdroid` (UnifiedPush) in `app/build.gradle.kts`. |

### 9.2 Variant selection

`isEnterpriseBuild` is `File("enterprise/README.md").exists()` — the presence of the private
`enterprise` submodule flips the build to the Enterprise variant (icon, min SDK 33, groups, proguard, etc.). For a white-label you typically stay on the FOSS/`element` path and override branding via `BuildTimeConfig` + `appicon/element` + compound tokens.

### 9.3 Feature flags

`libraries/featureflag/FeatureFlags.kt` gates features on/off (developer options screen at runtime; hard defaults in code).

### 9.4 Adding a new screen/feature

1. Generate a feature module via the AS "Generate Module from Template" plugin (`tools/templates/`).
2. Add Presentation classes (`Node/Presenter/View/State/Event/StatePreviewParam`) with the template.
3. Expose an `EntryPoint` in the `api` module; register a `NavTarget` + `resolve()` branch in the appropriate `appnav` flow node.
4. Annotate the node `@ContributesNode(Scope::class)`.

---

## 10. Conventions (CI-enforced)

- Naming suffixes matter (`*Presenter`, `*State`, `*View`, `*Node`) — they drive coverage rules (Kover) and architecture checks (Konsist).
- Logging: **Timber only**, never `android.util.Log`. **Never log message content, secrets, or keys.** Matrix IDs are safe.
- Strings: new English → `temporary.xml`; key prefixes `action_`, `common_`, `a11y_`, `screen_<name>_`, `error_`.
- Previews: `@PreviewsDayNight` + `PreviewParameterProvider` for all main Composable states, wrapped in `ElementPreview { }`.
- Prefer Fakes over mocking; `sealed interface` over `sealed class`; 160-char line width.

---

## 11. Build & Run

```bash
./gradlew assembleDebug          # build (gplayDebug by default)
./gradlew test                   # unit tests
./gradlew :app:koverHtmlReport   # coverage report
./gradlew ktlintFormat           # format
./gradlew lint                   # lint
```

The Rust SDK is consumed prebuilt from Maven (`org.matrix.rustcomponents:sdk-android`).
Build it locally only to modify the SDK itself: `tools/sdk/build-rust-sdk` → produces
`libraries/rustsdk/matrix-rust-sdk.aar` (auto-picked-up). Delete that `.aar` to revert to the published version.
