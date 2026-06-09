# Environment Setup Guide

BrandForge loads secrets from `local.properties` first, then from exported environment variables. The checked-in `.env.example` is a template only.

## Required Keys

Add these values to `local.properties` for local Android Studio or Gradle builds:

```properties
OPENROUTER_API_KEY=replace_me
GEMINI_API_KEY=replace_me
FIRECRAWL_API_KEY=replace_me
YOUTUBE_API_KEY=replace_me
QDRANT_URL=https://your-cluster.region.cloud.qdrant.io/
QDRANT_API_KEY=replace_me
```

`sdk.dir` may also be present in `local.properties`; Android Studio usually manages it.

## Optional Keys

```properties
APIFY_API_TOKEN=
OPENROUTER_BASE_URL=https://openrouter.ai/api/v1/
GEMINI_BASE_URL=https://generativelanguage.googleapis.com/
FIRECRAWL_BASE_URL=https://api.firecrawl.dev/
APIFY_BASE_URL=https://api.apify.com/v2/
YOUTUBE_BASE_URL=https://www.googleapis.com/youtube/v3/
```

Apify is foundation-configured but not used by the final demo features.

## BuildConfig Integration

`app/build.gradle.kts` reads each key with this priority:

1. `local.properties`
2. exported environment variable
3. blank or default value

The values become `BuildConfig` fields such as `BuildConfig.GEMINI_API_KEY` and `BuildConfig.QDRANT_URL`.

## Runtime Validation

`BuildConfigEnvironmentManager` maps `BuildConfig` fields to `EnvironmentKey` values.

`SecretManager` checks for blanks and placeholder tokens, then redacts configured values.

`StartupCheckRecorder` writes a local `foundation_audit` row with configured and missing keys.

## Security Rule

Never commit real keys. If a key is pasted in chat, slides, issue comments, or GitHub, rotate it.

