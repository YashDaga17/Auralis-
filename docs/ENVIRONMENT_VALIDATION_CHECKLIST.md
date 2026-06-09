# Environment Validation Checklist

Use this before the final demo.

| Check | Expected Result | Failure Condition | Criticality |
|---|---|---|---|
| `OPENROUTER_API_KEY` configured | OpenRouter content routing can run | Blank, placeholder, revoked, or wrong account | High |
| `GEMINI_API_KEY` configured | Gemini embeddings, chat, lead classification, and analysis can run | Blank, invalid, quota exceeded | Critical |
| `FIRECRAWL_API_KEY` configured | Trend and competitor web fetches can run | Blank, invalid, quota exceeded | High |
| `YOUTUBE_API_KEY` configured | YouTube trend and competitor fetches can run | Blank, invalid, API disabled | High |
| `QDRANT_URL` configured | Remote vector memory endpoint is reachable | Blank, malformed URL, missing trailing slash handled but bad host fails | Critical |
| `QDRANT_API_KEY` configured | Qdrant vector writes and retrieval can authenticate | Blank, invalid, wrong cluster | Critical |
| `APIFY_API_TOKEN` blank or configured | App remains production-ready because Apify is optional | Treated as required by docs or demo script | Low |
| `.env` ignored | Real secrets are not published | `.env` is staged for GitHub | Critical |
| `.env.example` committed | Reviewers know required keys without seeing secrets | Missing template | Medium |
| Startup audit row created | App records configured/missing key status locally | Room write fails or EnvironmentManager not bound | Medium |

