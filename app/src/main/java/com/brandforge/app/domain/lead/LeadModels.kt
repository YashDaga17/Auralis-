package com.brandforge.app.domain.lead

enum class AudienceInteractionType(
    val label: String,
) {
    Comment("Comment"),
    DirectMessage("DM-like Message"),
    AudienceInteraction("Audience Interaction"),
}

enum class LeadClassification(
    val label: String,
) {
    Lead("Lead"),
    Question("Question"),
    Collaboration("Collaboration"),
    Feedback("Feedback"),
    PRRisk("PR Risk"),
    Ignore("Ignore"),
}

enum class LeadPriority(
    val label: String,
) {
    Critical("Critical"),
    High("High"),
    Medium("Medium"),
    Low("Low"),
}

data class Lead(
    val id: String,
    val creatorId: String,
    val sourceType: AudienceInteractionType,
    val platform: String,
    val authorHandle: String,
    val text: String,
    val classification: LeadClassification,
    val confidence: Float,
    val suggestedReply: String,
    val priority: LeadPriority,
    val reason: String,
    val receivedAt: Long,
    val classifiedAt: Long,
    val rawModelResponse: String,
)

data class LeadInteractionInput(
    val creatorId: String,
    val sourceType: AudienceInteractionType,
    val platform: String,
    val authorHandle: String,
    val text: String,
    val receivedAt: Long = System.currentTimeMillis(),
)

data class LeadDetectionInput(
    val id: String,
    val creatorId: String,
    val sourceType: AudienceInteractionType,
    val platform: String,
    val authorHandle: String,
    val text: String,
    val classification: LeadClassification,
    val confidence: Float,
    val suggestedReply: String,
    val priority: LeadPriority,
    val reason: String,
    val receivedAt: Long,
    val classifiedAt: Long,
    val rawModelResponse: String,
)
