# Contributing

Thanks for helping improve BrandForge.

## Local Setup

1. Install Android Studio.
2. Clone the repository.
3. Add required secrets to `local.properties`.
4. Run:

```bash
./gradlew :app:compileDebugKotlin
```

## Development Rules

- Do not commit secrets.
- Do not add fake repositories for completed feature slices.
- Keep the retro command-center UI consistent.
- Prefer existing repository and agent boundaries.
- Keep the phone as source of truth.
- Add migrations for Room schema changes.
- Keep AI output grounded in Brand DNA, memory, trends, or competitor data.

## Pull Request Checklist

- Build passes.
- No real API keys in code, docs, screenshots, or logs.
- New Room entities are registered in `BrandForgeDatabase`.
- New DAOs are provided through Hilt.
- Navigation changes handle all destinations.
- Docs are updated when feature behavior changes.

