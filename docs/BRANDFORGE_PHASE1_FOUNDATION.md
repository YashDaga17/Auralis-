# BrandForge Phase 1 Foundation

Phase 1 converts the prototype app foundation from ad hoc construction to production infrastructure. It does not replace the existing command-center UI and does not implement creator memory, trend ingestion, or agent autonomy yet. It creates the runtime substrate those systems require.

## Runtime Architecture

```text
BrandForgeApplication
  -> Hilt SingletonComponent
  -> EnvironmentManager
  -> SecretManager
  -> OkHttp clients
  -> Retrofit APIs
  -> OpenRouterClient
  -> Room database
  -> DataStore preferences
  -> StartupCheckRecorder

MainActivity
  -> Hilt ViewModel
  -> CommandCenterRepository
  -> Existing Compose command center
```

## Implemented Foundation

- Hilt application and activity entry points.
- Constructor-injected `CommandCenterViewModel`.
- Hilt-bound `CommandCenterRepository` in the feature/data layer, outside the foundation DI package.
- BuildConfig-backed environment loading from `local.properties` or process environment.
- `.env.example` with all required keys.
- `EnvironmentManager` for endpoint URLs and required-key validation.
- `SecretManager` for placeholder detection and redaction.
- OkHttp base client with timeouts, retry, and safe BASIC debug logging.
- Auth interceptors for bearer headers, query API keys, and Qdrant API key headers.
- Retrofit OpenRouter API and `OpenRouterClient`.
- Room `BrandForgeDatabase`.
- Room `FoundationAuditEntity` and DAO for startup validation status.
- DataStore `CreatorPreferencesStore`.
- Startup missing-key recorder.
- Network permission.
- Android backup disabled for future creator-memory safety.

## Environment Keys

Required:

```text
OPENROUTER_API_KEY
GEMINI_API_KEY
FIRECRAWL_API_KEY
APIFY_API_TOKEN
QDRANT_URL
QDRANT_API_KEY
YOUTUBE_API_KEY
```

Optional endpoint overrides:

```text
OPENROUTER_BASE_URL
GEMINI_BASE_URL
FIRECRAWL_BASE_URL
APIFY_BASE_URL
YOUTUBE_BASE_URL
```

Values can be placed in `local.properties` for local development. The same names can also be supplied as environment variables in CI.

## Security Posture

- API secrets are injected into `BuildConfig` at build time.
- Network logs use BASIC level in debug and do not log bodies.
- `SecretManager.redact` is used for startup validation reporting.
- Android Auto Backup is disabled to avoid future creator memory leakage.

Important follow-up: mobile clients cannot truly protect third-party API keys from extraction. Production deployment should route provider calls through a backend token broker or creator-owned key flow before public release.

## Phase 1 Technical Debt

- Hilt requires the classic Android Gradle extension in this AGP 9.2.1 project. `android.builtInKotlin=false` and `android.newDsl=false` are enabled as a compatibility bridge and must be revisited before AGP 10.
- The command-center repository is still an in-memory feature repository. It is no longer service-located and no longer lives in the foundation layer, but Phase 2 must replace it with Room-backed memory and feature repositories.
- Only OpenRouter has a typed Retrofit API in Phase 1. Gemini, Firecrawl, Apify, Qdrant, and YouTube clients are intentionally deferred to their feature phases.

Phase 2 update:
- Gemini embedding and Qdrant memory clients have been added in the Creator Memory phase.
- Brand DNA onboarding completion is now persisted in DataStore.

## Phase 2 Entry Criteria

Before starting Creator Memory:

- Keep Hilt as the only object graph.
- Use `EnvironmentManager` and `SecretManager` for all provider credentials.
- Use `BrandForgeDatabase` for local persistence.
- Use `CreatorPreferencesStore` for lightweight app preferences.
- Add Qdrant through the same Retrofit/OkHttp pattern.
