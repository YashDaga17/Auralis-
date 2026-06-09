# Project Structure

```text
app/src/main/java/com/brandforge/app
  core/
    ai/                 Embeddings, Gemini, OpenRouter clients
    config/             EnvironmentManager, SecretManager
    database/           Room database, migrations, DAOs, entities
    datastore/          DataStore preferences
    di/                 Hilt modules
    model/              Command-center UI models
    network/            OkHttp qualifiers and interceptors
    startup/            Startup validation recording
  data/
    competitor/         Competitor repository, fetch sources, mappers
    content/            Content draft persistence
    lead/               Lead persistence
    memory/             Room and Qdrant memory data sources
    trend/              Firecrawl, YouTube, Room trend data sources
    twin/               Twin chat persistence
  domain/
    agent/              Command-center workflow simulation state
    competitor/         CompetitorAgent and GapAnalysisEngine
    content/            ContentAgent, PromptAssembler, ModelRouter
    lead/               LeadDetectionAgent
    memory/             Memory use cases and retrieval engine
    metrics/            Creator scoring
    trend/              TrendAgent and opportunity scoring
    twin/               TwinChatAgent and ContextAssembler
    voice/              Voice command parser
  presentation/
    commandcenter/      Main command state
    competitor/         Competitor Intelligence screen
    content/            Content Studio screen
    lead/               Lead Inbox screen
    memory/             Brand DNA and Memory screens
    navigation/         BrandForgeDestination
    trend/              Trend Radar screen
    twin/               Digital Twin Chat screen
    voice/              Android SpeechRecognizer wrapper
  ui/
    components/         Retro cyber UI components
    theme/              Typography and colors
```

## Feature Slices

- Brand DNA: `presentation/memory`, `domain/memory`, `data/memory`, `brand_dna`
- Creator Memory: `CreatorMemoryRepository`, Room memory shards, Qdrant remote source
- Trends: `TrendAgent`, Firecrawl, YouTube, `trend_signal`, `trend_opportunity`
- Content: `ContentAgent`, OpenRouter/Gemini routing, `content_draft`
- Twin Chat: `TwinChatAgent`, `ContextAssembler`, `twin_chat_message`
- Leads: `LeadDetectionAgent`, `lead_detection`
- Competitors: `CompetitorAgent`, `GapAnalysisEngine`, competitor tables

