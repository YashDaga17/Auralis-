# Real Device Test Plan

Run on the target Android phone before judging.

| Area | Test | Expected Result | Failure Conditions | Criticality |
|---|---|---|---|---|
| Brand DNA | Save creator profile | Profile persists and reloads | Save fails, blank state after restart | Critical |
| Brand DNA | Edit voice rules | Updated rules appear in later AI prompts | Old voice remains active | High |
| Creator Memory | Retrieve memory by query | Relevant memory shards return | Empty result with known memory | Critical |
| Creator Memory | App restart | Stored memory remains available | Memory disappears | Critical |
| Trend Intelligence | Run trend scan | Firecrawl/YouTube signals persist | API failure, no error shown | High |
| Opportunity Engine | Score trends | Opportunities show score, format, rationale | Hardcoded or missing scores | Critical |
| Content Generation | Generate Reel Script | Draft persists in Room | Generic output, no persistence | Critical |
| Content Generation | Generate Carousel | Carousel draft appears in list | Wrong format or empty draft | High |
| Content Generation | Generate X Thread | Thread draft appears in list | Wrong format or failed model call | High |
| Digital Twin Chat | Ask what to post | Answer uses memory/trends/drafts | Generic chatbot answer | Critical |
| Digital Twin Chat | Ask competitor gap question | Answer cites competitor insights when present | No competitor context | High |
| Lead Detection | Classify lead-like comment | Classification Lead with confidence and reply | Incorrect category or no save | High |
| Lead Detection | Classify spam | Classification Ignore or Low priority | High priority spam | Medium |
| Competitor Intelligence | Analyze competitor URL | Content and insights persist | No content, no visible error | High |
| Competitor Intelligence | Gap output | Insight includes confidence, reasoning, hook, angle, format | Missing required fields | Critical |
| Voice Commands | Trigger command | SpeechRecognizer routes known command | Permission denied or no route | Medium |
| Navigation | Visit every tab | Every screen opens without crash | Broken route or blank screen | Critical |
| Persistence | Restart app | Saved DNA, memory, drafts, leads, competitors remain | Data loss | Critical |
| App Restart | Relaunch after AI run | UI state can recover from local data | Crash or missing data | High |
| Offline Mode | Launch without network | Local screens and persisted data load | Crash on startup | High |
| Offline Mode | Run remote action | Error appears without fake data | Silent failure or fake result | Medium |

