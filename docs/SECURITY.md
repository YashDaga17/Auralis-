# Security

## Secret Handling

Real API keys must live in `local.properties`, exported environment variables, or secure CI secrets. Do not commit `.env`, `.env.*`, or `local.properties`.

`SecretManager` redacts configured values and detects blank or placeholder values. Startup validation stores only configured/missing key names, not raw secrets.

## Sensitive Data

BrandForge stores creator strategy data locally in Room. This may include brand goals, content history, competitor notes, and audience interactions.

Do not enter private customer data, payment data, passwords, or regulated personal data into demo prompts.

## AI Safety

- Prompt outputs are grounded in Brand DNA, memory, trends, and competitor insights.
- The app avoids silent fallback to fake intelligence.
- Lead and competitor classifiers fail visibly when model output is invalid.
- Docs and demo should avoid claiming autonomous publishing or CRM actions.

## Network Security

External providers:

- Gemini
- OpenRouter
- Firecrawl
- YouTube Data API
- Qdrant

Use provider dashboards to rotate keys before public release. Restrict API keys by project, API, referrer, or quota where provider controls allow.

## Reporting Issues

For a portfolio or hackathon repository, open a GitHub issue with:

- Summary
- Reproduction steps
- Device and Android version
- Expected result
- Actual result
- Redacted logs only

