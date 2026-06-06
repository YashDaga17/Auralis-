package com.brandforge.app.domain.content

import com.brandforge.app.domain.competitor.CompetitorInsight
import com.brandforge.app.domain.memory.BrandDna
import com.brandforge.app.domain.memory.MemoryShard
import com.brandforge.app.domain.trend.TrendOpportunity
import javax.inject.Inject

class PromptAssembler @Inject constructor() {
    fun assemble(
        format: ContentFormat,
        trend: TrendOpportunity,
        brandDna: BrandDna,
        memories: List<MemoryShard>,
        competitorInsights: List<CompetitorInsight> = emptyList(),
    ): PromptPayload {
        val memoryBlock = memories.joinToString(separator = "\n") { memory ->
            "- [${memory.id}] ${memory.type.name}: ${memory.title} :: ${memory.summary}"
        }.ifBlank { "- No relevant memory shards were found; use Brand DNA and creator goals only." }
        val competitorBlock = competitorInsights.joinToString(separator = "\n") { insight ->
            "- [${insight.id}] Focus=${insight.pattern}; Gap=${insight.gap}; Opportunity=${(insight.opportunityScore * 100).toInt()}%; Format=${insight.recommendedContentFormat}; Hook=${insight.recommendedHook}; Angle=${insight.recommendedAngle}; Reasoning=${insight.reasoning}"
        }.ifBlank { "- No competitor gap insights available." }

        val formatRules = when (format) {
            ContentFormat.ReelScript -> """
                FORMAT: Reel Script
                - Output a 30-45 second reel script.
                - Include: hook, scene beats, spoken lines, on-screen text, caption, CTA.
                - Keep sentences punchy and mobile-native.
            """.trimIndent()
            ContentFormat.InstagramCarousel -> """
                FORMAT: Instagram Carousel
                - Output 7 slides.
                - Include each slide title and body copy.
                - Include caption and CTA after the slides.
                - Make slide 1 a save-worthy hook.
            """.trimIndent()
            ContentFormat.XThread -> """
                FORMAT: X Thread
                - Output 6-8 posts.
                - Number each post.
                - Make post 1 a sharp hook.
                - End with a useful CTA, not engagement bait.
            """.trimIndent()
            ContentFormat.ImageConcept -> """
                FORMAT: Image Concept
                - Output a production-ready image generation brief, not a rendered image.
                - Include: visual concept, subject, composition, background, colors, typography, brand motifs, negative prompts, caption, CTA.
                - Make it usable for a designer or future image generation model.
            """.trimIndent()
            ContentFormat.VideoStoryboard -> """
                FORMAT: Video Storyboard
                - Output a short-form video production brief, not a rendered video.
                - Include: 5-7 shots, camera direction, scene text, voiceover, b-roll notes, edit pacing, caption, CTA.
                - Make it ready for a creator to shoot or for a future video generation model.
            """.trimIndent()
        }

        val systemPrompt = """
            You are BrandForge Content Agent, the creator's digital twin content strategist.
            Generate content that sounds creator-specific, not generic AI advice.
            You must obey the Brand DNA, creator goals, banned claims, and retrieved memory shards.
            Do not invent metrics, revenue claims, client outcomes, or personal stories that are not present in context.
            Avoid filler phrases, corporate phrasing, and generic "in today's world" openings.
            Return only the draft content. Do not wrap in markdown fences.
        """.trimIndent()

        val userPrompt = """
            CREATOR
            Name: ${brandDna.creatorName}
            Archetype: ${brandDna.archetype}

            BRAND VOICE RULES
            ${brandDna.voiceRulesJson}

            BANNED CLAIMS
            ${brandDna.bannedClaimsJson}

            CREATOR GOALS
            ${brandDna.businessGoalsJson}

            TREND OPPORTUNITY
            Title: ${trend.title}
            Summary: ${trend.summary}
            Source Platform: ${trend.sourcePlatform}
            Source URL: ${trend.sourceUrl}
            Opportunity Score: ${(trend.opportunityScore * 100).toInt()}%
            Recommended Format From Trend Agent: ${trend.recommendedFormat}
            Trend Rationale: ${trend.rationale}

            RETRIEVED MEMORY SHARDS
            $memoryBlock

            COMPETITOR GAP INSIGHTS
            $competitorBlock

            $formatRules

            REQUIREMENTS
            - Make the content feel like ${brandDna.creatorName} would actually post it.
            - Tie the trend to the creator's goals.
            - Use at least two concrete signals from retrieved memory or Brand DNA.
            - If competitor gap insights are available, use them to make the content differentiated instead of imitating competitors.
            - Include a clear reason this content exists inside the draft as a short "Why this works:" note.
            - Keep the output ready for a mobile creator to review and edit.
        """.trimIndent()

        return PromptPayload(
            systemPrompt = systemPrompt,
            userPrompt = userPrompt,
        )
    }
}
