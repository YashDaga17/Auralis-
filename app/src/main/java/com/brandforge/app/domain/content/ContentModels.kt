package com.brandforge.app.domain.content

enum class ContentFormat(
    val label: String,
) {
    ReelScript("Reel Script"),
    InstagramCarousel("Instagram Carousel"),
    XThread("X Thread"),
    ImageConcept("Image Concept"),
    VideoStoryboard("Video Storyboard"),
}

data class ContentDraft(
    val id: String,
    val creatorId: String,
    val title: String,
    val content: String,
    val format: ContentFormat,
    val generatedAt: Long,
    val sourceTrendId: String,
    val opportunityScore: Float,
    val memoryIdsUsed: List<String>,
    val whyGenerated: String,
)

data class ContentDraftInput(
    val id: String,
    val creatorId: String,
    val title: String,
    val content: String,
    val format: ContentFormat,
    val generatedAt: Long,
    val sourceTrendId: String,
    val opportunityScore: Float,
    val memoryIdsUsed: List<String>,
    val whyGenerated: String,
)

data class ContentGenerationRequest(
    val creatorId: String,
    val trendOpportunityId: String,
    val format: ContentFormat,
)

data class PromptPayload(
    val systemPrompt: String,
    val userPrompt: String,
)
