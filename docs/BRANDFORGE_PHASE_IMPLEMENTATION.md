# BrandForge Phase Implementation Contract

BrandForge is implemented as a phone-first Android creator operating system. The phone is canonical storage and execution authority. Office Kit laptop views are synchronized read and handoff surfaces unless a later product decision explicitly promotes them to write-capable collaborators.

## System Shape

```text
iQOO Android phone
  -> Compose MVI screens
  -> ViewModels
  -> Use cases
  -> Offline-first repositories
  -> Room + DataStore local state
  -> WorkManager autonomous workflows
  -> Sync adapters and API clients
  -> AI router
  -> Gemini / Gemma / OpenRouter MiniMax / Qdrant / Firecrawl / Apify / YouTube / Trends / News

Office Kit laptop
  -> War Room mode
  -> Handoff reader
  -> Report reviewer
  -> Phone source-of-truth sync channel
```

## Package Standard

```text
com.brandforge.app
  core.model
  core.designsystem
  core.network
  core.database
  core.datastore
  core.worker
  core.ai
  data.<feature>
  domain.<feature>
  presentation.<feature>
```

Current Phase 1 code already follows the same dependency direction:

```text
presentation -> domain -> data contracts -> core model
```

## Phase 1: Command Center Foundation

Architecture:
- Single Android app module with Clean Architecture package boundaries.
- MVI-style `StateFlow<BrandForgeState>` observed by Compose.
- `CommandCenterViewModel` owns UI events and delegates workflows to repository contracts.
- `OfflineFirstCommandCenterRepository` is the first local-first state store. Room replaces its backing store in Phase 2 without changing the ViewModel contract.
- Foundation infrastructure now includes Hilt, Retrofit, OkHttp, Room, DataStore, OpenRouter API contracts, BuildConfig-backed environment loading, and startup environment validation. See `docs/BRANDFORGE_PHASE1_FOUNDATION.md`.

Folder structure:

```text
app/src/main/java/com/brandforge/app/
  MainActivity.kt
  core/model/BrandForgeModels.kt
  data/BrandForgeSeedData.kt
  data/CommandCenterRepository.kt
  data/OfflineFirstCommandCenterRepository.kt
  data/di/CommandCenterRepositoryModule.kt
  domain/agent/AgentWorkflowEngine.kt
  domain/metrics/CreatorScoreCalculator.kt
  domain/voice/VoiceCommandParser.kt
  presentation/BrandForgeApp.kt
  presentation/commandcenter/CommandCenterViewModel.kt
  presentation/navigation/BrandForgeDestination.kt
  presentation/voice/AndroidSpeechCommandRecognizer.kt
  ui/components/ForgeComponents.kt
  ui/theme/BrandForgeTheme.kt
  core/config/
  core/network/
  core/di/
  core/database/
  core/storage/
  core/ai/openrouter/
  core/startup/
```

Database schema:
- `foundation_audit` stores the latest startup environment validation result.
- Product state contracts are shaped to map directly to Phase 2 Room entities.

API contracts:
- `OpenRouterApi` and `OpenRouterClient` are available through Retrofit/OkHttp.
- Gemini, Firecrawl, Apify, Qdrant, and YouTube credentials/endpoints are configured but feature clients are created in their dedicated phases.

Repositories:
- `CommandCenterRepository`
- `OfflineFirstCommandCenterRepository`

Use cases:
- `AgentWorkflowEngine.reduce`
- `VoiceCommandParser.parse`
- `CreatorScoreCalculator.calculate`

ViewModels:
- `CommandCenterViewModel`

Compose screens:
- Command Center
- Agents
- Memory
- Content Studio
- Office Kit War Room
- Creator Twin Chat

Navigation updates:
- Custom terminal navigation via `BrandForgeDestination`.
- Adaptive phone bottom command strip and wide War Room side rail.

WorkManager jobs:
- None in Phase 1.

Prompts:
- Prompt assets begin in Phase 2 when Brand DNA and memory retrieval are persistent.

State management:
- `BrandForgeState` is immutable.
- UI events call `runWorkflow`, `routeVoiceCommand`, and `selectDestination`.

Implementation steps:
- Replace starter Material dashboard with BrandForge terminal OS shell.
- Add agent state models, logs, metrics, memory shards, opportunities, leads, and content queue models.
- Add Android `SpeechRecognizer` bridge with runtime mic permission.
- Add Hilt composition root and modules for environment, network, API, AI, database, storage, and coroutines.
- Add `.env.example`, BuildConfig fields, `EnvironmentManager`, `SecretManager`, and startup validation persistence.
- Compile and verify.

Potential risks:
- Fonts are currently monospace fallbacks until Press Start 2P and JetBrains Mono font files are added.
- The first repository is process-local and must be replaced with Room-backed persistence before real creator data is captured.
- Hilt currently requires AGP classic DSL compatibility flags in this project. Track before AGP 10 migration.

## Phase 2: Brand DNA and Creator Memory

Architecture:
- Add Room as canonical phone store.
- Add DataStore for onboarding completion, model preferences, sync settings, and Office Kit mode.
- Add Qdrant remote vector store through a repository implementation that always writes a local memory transaction first.
- Memory retrieval becomes a mandatory dependency for all AI use cases.

Folder structure:

```text
core/database/
  BrandForgeDatabase.kt
  dao/BrandDnaDao.kt
  dao/MemoryShardDao.kt
  dao/ContentSampleDao.kt
  entity/BrandDnaEntity.kt
  entity/MemoryShardEntity.kt
  entity/ContentSampleEntity.kt
core/datastore/CreatorPreferencesStore.kt
core/ai/EmbeddingClient.kt
data/memory/
  CreatorMemoryRepositoryImpl.kt
  QdrantMemoryRemoteDataSource.kt
  RoomMemoryLocalDataSource.kt
domain/memory/
  UpsertBrandDnaUseCase.kt
  RetrieveCreatorMemoryUseCase.kt
  ScoreMemoryRelevanceUseCase.kt
presentation/memory/
  MemoryViewModel.kt
  BrandDnaOnboardingScreen.kt
  MemoryGraphScreen.kt
```

Package structure:
- `com.brandforge.app.data.memory`
- `com.brandforge.app.domain.memory`
- `com.brandforge.app.presentation.memory`

Database schema:

```sql
brand_dna(
  creator_id TEXT PRIMARY KEY,
  creator_name TEXT NOT NULL,
  archetype TEXT NOT NULL,
  voice_rules_json TEXT NOT NULL,
  banned_claims_json TEXT NOT NULL,
  business_goals_json TEXT NOT NULL,
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL
);

memory_shard(
  id TEXT PRIMARY KEY,
  creator_id TEXT NOT NULL,
  type TEXT NOT NULL,
  title TEXT NOT NULL,
  summary TEXT NOT NULL,
  source_uri TEXT,
  embedding_id TEXT,
  retrieval_weight REAL NOT NULL,
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL
);

content_sample(
  id TEXT PRIMARY KEY,
  creator_id TEXT NOT NULL,
  platform TEXT NOT NULL,
  body TEXT NOT NULL,
  performance_json TEXT NOT NULL,
  style_features_json TEXT NOT NULL,
  created_at INTEGER NOT NULL
);

CREATE INDEX idx_memory_creator_type ON memory_shard(creator_id, type);
CREATE INDEX idx_content_creator_platform ON content_sample(creator_id, platform);
```

Implemented in:
- `core/database/entity/BrandDnaEntity.kt`
- `core/database/entity/MemoryShardEntity.kt`
- `core/database/entity/ContentSampleEntity.kt`
- `core/database/dao/BrandDnaDao.kt`
- `core/database/dao/MemoryShardDao.kt`
- `core/database/dao/ContentSampleDao.kt`
- `core/database/BrandForgeMigrations.kt`

API contracts:

```kotlin
interface CreatorMemoryRepository {
    fun observeBrandDna(creatorId: String): Flow<BrandDna>
    suspend fun upsertBrandDna(input: BrandDnaInput)
    suspend fun writeMemory(shard: MemoryShardDraft): MemoryShard
    suspend fun retrieve(query: MemoryQuery): List<MemoryShard>
}

interface EmbeddingClient {
    suspend fun embed(text: String): EmbeddingVector
}
```

Repositories:
- `CreatorMemoryRepositoryImpl`
- `QdrantMemoryRemoteDataSource`
- `RoomMemoryLocalDataSource`

Persistence rule:
- Brand DNA, content samples, and memory shards write to Room first.
- Embeddings are generated after local persistence.
- Qdrant sync runs after local persistence and local data remains available if remote sync fails.
- Retrieval uses Qdrant scores when available and local relevance scoring as the always-available fallback.

Use cases:
- `UpsertBrandDnaUseCase`
- `RetrieveCreatorMemoryUseCase`
- `BuildCreatorVoiceProfileUseCase`
- `ScoreMemoryRelevanceUseCase`

ViewModels:
- `BrandDnaViewModel`
- `MemoryViewModel`

Compose screens:
- Brand DNA onboarding
- Memory graph
- Memory shard detail
- Voice lock audit

Navigation updates:
- Add `BrandDnaSetup` route before Command Center for first-run users.
- Keep Memory tab as post-onboarding memory browser.

WorkManager jobs:
- `MemorySyncWorker`
- `EmbeddingBackfillWorker`

Prompts:

```text
SYSTEM: You are BrandForge Brand DNA Agent. Preserve the creator's identity, vocabulary, claim boundaries, business goals, and audience expectations.
INPUTS: creator profile, content samples, retrieved memories, proposed output.
OUTPUT: brand_fit_score, violations, required edits, safe vocabulary, rejected claims.
```

State management:
- `BrandDnaUiState`
- `MemoryUiState`
- One-shot events for onboarding completion and sync errors.

Implementation steps:
- Add Room, DataStore, Hilt, and Qdrant client.
- Migrate Phase 1 seed data into Room bootstrap on first launch.
- Require `RetrieveCreatorMemoryUseCase` in every content and agent workflow.

Potential risks:
- Vector writes can fail while local writes succeed. Mark memory shards with `embeddingStatus` and retry through WorkManager.
- Creator identity data is sensitive. Encrypt local database if real user data is stored.

## Phase 3: Trend Intelligence

Architecture:
- Add source adapters for Firecrawl, Apify, YouTube Data API, Google Trends, and news.
- Normalize all external signals into `TrendSignalEntity`.
- Trend Agent scores opportunities with brand fit, velocity, competition, freshness, and content-format suitability.

Folder structure:

```text
core/network/
  RetrofitProvider.kt
  AuthInterceptor.kt
data/trends/
  TrendRepositoryImpl.kt
  FirecrawlTrendDataSource.kt
  ApifyTrendDataSource.kt
  YouTubeTrendDataSource.kt
  GoogleTrendsDataSource.kt
  NewsTrendDataSource.kt
domain/trends/
  FetchTrendSignalsUseCase.kt
  ScoreTrendOpportunityUseCase.kt
  BuildTrendBriefUseCase.kt
presentation/trends/
  TrendRadarViewModel.kt
  TrendRadarScreen.kt
  TrendDetailScreen.kt
```

Database schema:

```sql
trend_signal(
  id TEXT PRIMARY KEY,
  source TEXT NOT NULL,
  platform TEXT NOT NULL,
  title TEXT NOT NULL,
  raw_text TEXT NOT NULL,
  url TEXT,
  velocity_score INTEGER NOT NULL,
  observed_at INTEGER NOT NULL,
  expires_at INTEGER NOT NULL
);

trend_opportunity(
  id TEXT PRIMARY KEY,
  creator_id TEXT NOT NULL,
  trend_signal_id TEXT NOT NULL,
  brand_fit INTEGER NOT NULL,
  opportunity_score INTEGER NOT NULL,
  recommended_format TEXT NOT NULL,
  rationale TEXT NOT NULL,
  risk_level TEXT NOT NULL,
  created_at INTEGER NOT NULL
);

CREATE INDEX idx_trend_platform_time ON trend_signal(platform, observed_at);
CREATE INDEX idx_opportunity_creator_score ON trend_opportunity(creator_id, opportunity_score);
```

API contracts:

```kotlin
interface TrendRepository {
    fun observeOpportunities(creatorId: String): Flow<List<TrendOpportunity>>
    suspend fun refreshSignals(request: TrendRefreshRequest): TrendRefreshResult
    suspend fun scoreForCreator(creatorId: String): List<TrendOpportunity>
}
```

Repositories:
- `TrendRepositoryImpl`

Use cases:
- `FetchTrendSignalsUseCase`
- `ScoreTrendOpportunityUseCase`
- `BuildTrendBriefUseCase`

ViewModels:
- `TrendRadarViewModel`
- Command Center consumes trend opportunities through shared state.

Compose screens:
- Trend radar
- Trend opportunity detail
- Source drilldown

Navigation updates:
- Promote Trend Radar from Command Center panel to full drilldown route.

WorkManager jobs:
- `TrendRefreshWorker` every 2-4 hours, constrained by network and battery.
- `TrendExpiryWorker` daily.

Prompts:

```text
SYSTEM: You are Trend Agent. Convert noisy trend signals into creator-specific opportunities.
RETRIEVE: Brand DNA, top content, audience insights, competitor observations.
OUTPUT: opportunity_score, window, recommended_format, hook_angle, risk_level, source_urls.
```

State management:
- `TrendRadarUiState`
- Paging state for historical trend signals.

Implementation steps:
- Add Retrofit clients and request authentication.
- Persist raw signals before scoring.
- Score with deterministic features first, then AI rationale.

Potential risks:
- Third-party scraping providers can rate-limit or return noisy pages. Use source health metrics and cache successful responses.
- Trends are time-sensitive. Expire stale opportunities aggressively.

## Phase 4: Content Studio

Architecture:
- Content generation is a multi-step pipeline: retrieve memory, draft, brand audit, virality audit, PR audit, queue for approval.
- No content is published automatically in this phase.
- Outputs support Reels, Instagram carousel, X threads, LinkedIn posts, YouTube Shorts, and WhatsApp broadcasts.

Folder structure:

```text
data/content/
  ContentRepositoryImpl.kt
  RoomContentDraftDataSource.kt
domain/content/
  GenerateContentUseCase.kt
  AuditBrandFitUseCase.kt
  ScoreViralityUseCase.kt
  PrepareContentPackageUseCase.kt
presentation/studio/
  ContentStudioViewModel.kt
  ContentStudioScreen.kt
  ContentEditorScreen.kt
  ViralityAuditPanel.kt
```

Database schema:

```sql
content_draft(
  id TEXT PRIMARY KEY,
  creator_id TEXT NOT NULL,
  format TEXT NOT NULL,
  title TEXT NOT NULL,
  body TEXT NOT NULL,
  status TEXT NOT NULL,
  brand_fit INTEGER NOT NULL,
  virality_score INTEGER NOT NULL,
  pr_risk TEXT NOT NULL,
  source_memory_ids_json TEXT NOT NULL,
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL
);

content_audit(
  id TEXT PRIMARY KEY,
  draft_id TEXT NOT NULL,
  audit_type TEXT NOT NULL,
  score INTEGER NOT NULL,
  findings_json TEXT NOT NULL,
  created_at INTEGER NOT NULL
);

CREATE INDEX idx_draft_creator_status ON content_draft(creator_id, status);
```

API contracts:

```kotlin
interface ContentRepository {
    fun observeDrafts(creatorId: String): Flow<List<ContentDraft>>
    suspend fun generate(request: ContentGenerationRequest): ContentDraft
    suspend fun updateDraft(draft: ContentDraftUpdate): ContentDraft
    suspend fun audit(draftId: String): ContentAuditBundle
}
```

Repositories:
- `ContentRepositoryImpl`

Use cases:
- `GenerateContentUseCase`
- `AuditBrandFitUseCase`
- `ScoreViralityUseCase`
- `PrepareContentPackageUseCase`

ViewModels:
- `ContentStudioViewModel`
- `ContentEditorViewModel`

Compose screens:
- Content Studio
- Draft editor
- Virality score sheet
- Brand fit diff

Navigation updates:
- Studio route opens queue.
- Draft detail route opens editor.

WorkManager jobs:
- `DraftBackfillWorker` for overnight generation.
- `DraftAuditWorker` for queued audit retries.

Prompts:

```text
SYSTEM: You are Content Agent. Create content as the creator's Digital Twin, not as a generic generator.
REQUIRED MEMORY: Brand DNA, top hooks, audience objections, trend opportunity, business goal.
OUTPUT JSON: format, title, body, hook, CTA, platform_notes, source_memory_ids.
```

State management:
- `ContentStudioUiState`
- Draft save operations are optimistic with local rollback on database failure.

Implementation steps:
- Add AI router.
- Implement prompt templates as versioned assets.
- Require audit pass before status can become `ReadyForApproval`.

Potential risks:
- Model drift can weaken brand consistency. Store prompt versions and audit every output.
- Long drafts can be expensive. Route short classification to lightweight models and deep strategy to primary reasoning only when needed.

## Phase 5: Social Listener, Lead Detection, and PR Detection

Architecture:
- Social Listener ingests comments, replies, DMs where APIs permit, and manual imports where APIs do not.
- Lead Agent and PR Risk Agent classify the same normalized interaction stream.
- High-risk items require creator review.

Folder structure:

```text
data/social/
  SocialSignalRepositoryImpl.kt
  YouTubeCommentDataSource.kt
  ManualImportDataSource.kt
domain/social/
  ClassifySocialSignalUseCase.kt
  DetectLeadUseCase.kt
  DetectPrRiskUseCase.kt
presentation/social/
  SocialInboxViewModel.kt
  LeadInboxScreen.kt
  RiskMonitorScreen.kt
```

Database schema:

```sql
social_signal(
  id TEXT PRIMARY KEY,
  creator_id TEXT NOT NULL,
  platform TEXT NOT NULL,
  author_handle TEXT,
  text TEXT NOT NULL,
  source_url TEXT,
  observed_at INTEGER NOT NULL,
  classification_json TEXT NOT NULL
);

lead_opportunity(
  id TEXT PRIMARY KEY,
  signal_id TEXT NOT NULL,
  creator_id TEXT NOT NULL,
  score INTEGER NOT NULL,
  intent TEXT NOT NULL,
  suggested_action TEXT NOT NULL,
  status TEXT NOT NULL,
  created_at INTEGER NOT NULL
);

pr_risk_event(
  id TEXT PRIMARY KEY,
  signal_id TEXT,
  draft_id TEXT,
  creator_id TEXT NOT NULL,
  risk_level TEXT NOT NULL,
  finding TEXT NOT NULL,
  mitigation TEXT NOT NULL,
  status TEXT NOT NULL,
  created_at INTEGER NOT NULL
);

CREATE INDEX idx_social_creator_time ON social_signal(creator_id, observed_at);
CREATE INDEX idx_lead_creator_score ON lead_opportunity(creator_id, score);
CREATE INDEX idx_pr_creator_level ON pr_risk_event(creator_id, risk_level);
```

API contracts:

```kotlin
interface SocialSignalRepository {
    fun observeSignals(creatorId: String): Flow<List<SocialSignal>>
    fun observeLeads(creatorId: String): Flow<List<LeadOpportunity>>
    fun observePrRisks(creatorId: String): Flow<List<PrRiskEvent>>
    suspend fun refreshSignals(creatorId: String)
}
```

Repositories:
- `SocialSignalRepositoryImpl`

Use cases:
- `ClassifySocialSignalUseCase`
- `DetectLeadUseCase`
- `DetectPrRiskUseCase`

ViewModels:
- `SocialInboxViewModel`
- `LeadInboxViewModel`
- `RiskMonitorViewModel`

Compose screens:
- Lead inbox
- PR monitor
- Social signal detail

Navigation updates:
- Leads and PR risk become drilldowns from Command Center.

WorkManager jobs:
- `SocialRefreshWorker`
- `LeadClassificationWorker`
- `PrRiskWorker`

Prompts:

```text
SYSTEM: You are Social Listener Agent. Classify audience signals into praise, objection, support, lead, risk, and content request.
OUTPUT JSON: category, intent, urgency, suggested_action, memory_write_candidate.
```

State management:
- Interaction streams are Room-backed flows.
- Risk escalation emits one-shot UI events and command center alerts.

Implementation steps:
- Normalize platform interactions.
- Run lightweight classification before expensive reasoning.
- Store leads and PR risks as first-class entities.

Potential risks:
- Platform APIs may restrict DM access. Support manual imports and YouTube-first ingestion.
- False positives in PR risk are disruptive. Use thresholds and human review.

## Phase 6: Competitor Intelligence and Strategy Reports

Architecture:
- Competitor Agent tracks creator-defined competitors, source URLs, posting cadence, formats, hooks, claims, and audience reactions.
- Strategy Report service combines competitor deltas, memory retrieval, content output, trend capture, and lead data.

Folder structure:

```text
data/competitor/
  CompetitorRepositoryImpl.kt
  CompetitorSourceDataSource.kt
domain/competitor/
  TrackCompetitorUseCase.kt
  AnalyzeCompetitorGapUseCase.kt
  GenerateStrategyReportUseCase.kt
presentation/competitor/
  CompetitorViewModel.kt
  CompetitorRadarScreen.kt
  StrategyReportScreen.kt
```

Database schema:

```sql
competitor(
  id TEXT PRIMARY KEY,
  creator_id TEXT NOT NULL,
  name TEXT NOT NULL,
  platform_handles_json TEXT NOT NULL,
  tracked_since INTEGER NOT NULL
);

competitor_observation(
  id TEXT PRIMARY KEY,
  competitor_id TEXT NOT NULL,
  platform TEXT NOT NULL,
  title TEXT NOT NULL,
  body TEXT NOT NULL,
  metrics_json TEXT NOT NULL,
  observed_at INTEGER NOT NULL
);

strategy_report(
  id TEXT PRIMARY KEY,
  creator_id TEXT NOT NULL,
  period TEXT NOT NULL,
  summary TEXT NOT NULL,
  recommendations_json TEXT NOT NULL,
  created_at INTEGER NOT NULL
);
```

API contracts:

```kotlin
interface CompetitorRepository {
    fun observeCompetitors(creatorId: String): Flow<List<Competitor>>
    suspend fun refreshObservations(creatorId: String): List<CompetitorObservation>
    suspend fun generateReport(request: StrategyReportRequest): StrategyReport
}
```

Repositories:
- `CompetitorRepositoryImpl`

Use cases:
- `TrackCompetitorUseCase`
- `AnalyzeCompetitorGapUseCase`
- `GenerateStrategyReportUseCase`

ViewModels:
- `CompetitorViewModel`
- `StrategyReportViewModel`

Compose screens:
- Competitor radar
- Strategy report
- Competitor detail

Navigation updates:
- War Room deep links into competitor reports.

WorkManager jobs:
- `CompetitorRefreshWorker`
- `WeeklyStrategyReportWorker`

Prompts:

```text
SYSTEM: You are Competitor Agent. Identify gaps the creator can own without copying competitors.
OUTPUT JSON: competitor_move, gap, creator_countermove, urgency, evidence_urls.
```

State management:
- Report generation uses a loading state with cancellable WorkManager continuation.

Implementation steps:
- Add competitor setup.
- Persist observations.
- Generate weekly report with source evidence.

Potential risks:
- Competitor tracking can become noisy. Let users pin key competitors and mute low-value sources.

## Phase 7: Overnight Agent and Morning Briefing

Architecture:
- Overnight Agent is a WorkManager chain that runs memory sync, trend refresh, social classification, competitor refresh, draft generation, risk audit, and briefing assembly.
- Morning briefing is a persisted artifact, not generated only on screen open.

Folder structure:

```text
core/worker/
  BrandForgeWorkerFactory.kt
data/overnight/
  OvernightRunRepositoryImpl.kt
domain/overnight/
  ScheduleOvernightRunUseCase.kt
  ExecuteOvernightPlanUseCase.kt
  GenerateMorningBriefingUseCase.kt
presentation/briefing/
  MorningBriefingViewModel.kt
  MorningBriefingScreen.kt
```

Database schema:

```sql
overnight_run(
  id TEXT PRIMARY KEY,
  creator_id TEXT NOT NULL,
  status TEXT NOT NULL,
  started_at INTEGER NOT NULL,
  finished_at INTEGER,
  summary TEXT,
  failure_reason TEXT
);

morning_briefing(
  id TEXT PRIMARY KEY,
  run_id TEXT NOT NULL,
  creator_id TEXT NOT NULL,
  briefing_json TEXT NOT NULL,
  created_at INTEGER NOT NULL
);
```

API contracts:

```kotlin
interface OvernightRunRepository {
    fun observeLatestRun(creatorId: String): Flow<OvernightRun?>
    suspend fun createRun(creatorId: String): OvernightRun
    suspend fun completeRun(runId: String, briefing: MorningBriefing)
}
```

Repositories:
- `OvernightRunRepositoryImpl`

Use cases:
- `ScheduleOvernightRunUseCase`
- `ExecuteOvernightPlanUseCase`
- `GenerateMorningBriefingUseCase`

ViewModels:
- `MorningBriefingViewModel`

Compose screens:
- Morning briefing
- Overnight run detail

Navigation updates:
- Command Center hero opens latest Morning Briefing.

WorkManager jobs:
- `OvernightSupervisorWorker`
- `MorningBriefingWorker`
- `RetryFailedAgentWorker`

Prompts:

```text
SYSTEM: You are Overnight Agent. Summarize unattended work as a prioritized action plan for a mobile-first creator.
OUTPUT JSON: top_actions, content_to_approve, leads_to_reply, risks_to_review, office_kit_handoffs.
```

State management:
- Overnight status is observed from Room.
- UI shows last successful briefing if the latest run fails.

Implementation steps:
- Build WorkManager chain.
- Persist intermediate outputs.
- Add notification for morning briefing readiness.

Potential risks:
- Android background restrictions can delay work. Use flexible windows and show last run freshness.

## Phase 8: Office Kit War Room

Architecture:
- Phone remains authoritative.
- War Room receives synchronized packages: content drafts, reports, competitor analysis, and trend boards.
- Conflict policy: phone wins unless explicit creator review accepts laptop edits in a future collaborative mode.

Folder structure:

```text
data/officekit/
  OfficeKitRepositoryImpl.kt
  LocalHandoffDataSource.kt
  NearbyOrNetworkSyncDataSource.kt
domain/officekit/
  PrepareWarRoomHandoffUseCase.kt
  ApplyReviewedHandoffUseCase.kt
presentation/officekit/
  OfficeKitViewModel.kt
  WarRoomScreen.kt
  HandoffDetailScreen.kt
```

Database schema:

```sql
office_handoff(
  id TEXT PRIMARY KEY,
  creator_id TEXT NOT NULL,
  type TEXT NOT NULL,
  payload_json TEXT NOT NULL,
  status TEXT NOT NULL,
  created_at INTEGER NOT NULL,
  synced_at INTEGER
);
```

API contracts:

```kotlin
interface OfficeKitRepository {
    fun observeHandoffs(creatorId: String): Flow<List<OfficeHandoff>>
    suspend fun prepareHandoff(request: HandoffRequest): OfficeHandoff
    suspend fun markSynced(handoffId: String)
}
```

Repositories:
- `OfficeKitRepositoryImpl`

Use cases:
- `PrepareWarRoomHandoffUseCase`
- `ApplyReviewedHandoffUseCase`

ViewModels:
- `OfficeKitViewModel`

Compose screens:
- Companion mode
- War Room mode
- Handoff detail

Navigation updates:
- Existing `WarRoom` destination becomes backed by persisted handoffs.

WorkManager jobs:
- `OfficeKitSyncWorker`

Prompts:

```text
SYSTEM: You are Supervisor Agent preparing an Office Kit handoff. Preserve phone authority and include exact evidence for laptop review.
OUTPUT JSON: handoff_type, payload, source_ids, required_creator_decisions.
```

State management:
- Handoff status is a Room flow.
- Sync failures are non-destructive and retryable.

Implementation steps:
- Persist handoff payloads.
- Add synchronization status.
- Add laptop-optimized layout already scaffolded by Phase 1 adaptive shell.

Potential risks:
- Multi-device sync can create conflicting changes. Keep phone canonical and require explicit accept flow.

## Phase 9: Polish, Performance, Security, and Release Readiness

Architecture:
- Add Hilt modules for every repository and use case.
- Add baseline profiles, Compose performance checks, strict mode diagnostics, and structured logging.
- Add encrypted secrets handling and release build hardening.

Folder structure:

```text
core/di/
  DatabaseModule.kt
  NetworkModule.kt
  RepositoryModule.kt
  AiModule.kt
core/observability/
  BrandForgeLogger.kt
  PerformanceTrace.kt
benchmark/
  BaselineProfileGenerator.kt
```

Database schema:
- Add migrations for every schema introduced in Phases 2-8.
- Add retention policy tables for logs and raw signals if storage grows too large.

API contracts:
- Harden all clients with timeouts, retries, auth interceptors, and typed error envelopes.

Repositories:
- Replace service locator with Hilt.
- All repository implementations are constructor-injected.

Use cases:
- Add telemetry wrappers for long-running AI and sync use cases.

ViewModels:
- Inject repositories and saved state handles.
- Add error recovery and retry actions.

Compose screens:
- Add Lottie retro boot sequence.
- Add charts for weekly and monthly performance.
- Add accessibility labels for terminal controls.
- Add offline banners and sync freshness indicators.

Navigation updates:
- Add deep links for briefing, draft, lead, risk, and handoff notifications.

WorkManager jobs:
- Add unique work names and retry policies.
- Add notification channels for briefings, escalated risk, and hot leads.

Prompts:
- Version prompt templates.
- Log model, prompt version, memory IDs, and audit result for evaluation.

State management:
- Add reducer tests for each workflow.
- Add UI screenshot tests for phone and wide War Room layouts.

Implementation steps:
- Replace fallback fonts with bundled Press Start 2P and JetBrains Mono files.
- Add Hilt, Room, WorkManager, Retrofit, OkHttp, Coil, Lottie, and charts.
- Run baseline profile generation and app startup profiling.

Potential risks:
- Over-animation can hurt low-end devices. Keep retro animation cheap, prefer Canvas and finite Lottie loops.
- Secret leakage through logs is a release blocker. Redact prompts, tokens, raw comments, and PII by default.
