package com.brandforge.app.presentation.content

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.brandforge.app.core.model.RiskLevel
import com.brandforge.app.domain.content.ContentDraft
import com.brandforge.app.domain.content.ContentFormat
import com.brandforge.app.domain.content.ContentMediaArtifact
import com.brandforge.app.domain.content.MediaArtifactStatus
import com.brandforge.app.domain.content.MediaArtifactType
import com.brandforge.app.domain.trend.TrendOpportunity
import com.brandforge.app.ui.components.ForgePanel
import com.brandforge.app.ui.components.KeyValueLine
import com.brandforge.app.ui.components.PixelButton
import com.brandforge.app.ui.components.SectionHeader
import com.brandforge.app.ui.components.SignalBar
import com.brandforge.app.ui.components.StatusPill
import com.brandforge.app.ui.theme.ForgeColor
import kotlin.math.roundToInt

@Composable
fun ContentStudioScreen(
    state: ContentStudioUiState,
    onCreatorIdChange: (String) -> Unit,
    onGenerate: (String, ContentFormat) -> Unit,
    onMediaPromptChange: (String) -> Unit,
    onGenerateMedia: (MediaArtifactType) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedArtifact by remember { mutableStateOf<ContentMediaArtifact?>(null) }
    selectedArtifact?.let { artifact ->
        MediaArtifactViewerDialog(
            artifact = artifact,
            onDismiss = { selectedArtifact = null },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ForgePanel {
            SectionHeader("Content Agent", trailing = "${ContentFormat.entries.size} FORMATS")
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = state.creatorId,
                onValueChange = onCreatorIdChange,
                label = { Text("Creator ID") },
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )
            state.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                StatusPill(label = error.take(34), riskLevel = RiskLevel.High)
            }
        }

        ForgePanel {
            SectionHeader("Rendered Media", trailing = if (state.generatingMediaType == null) "GEMINI / VEO" else "GENERATING")
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = state.mediaPrompt,
                onValueChange = onMediaPromptChange,
                label = { Text("Image/video prompt") },
                textStyle = MaterialTheme.typography.bodyMedium,
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PixelButton(
                    label = if (state.generatingMediaType == MediaArtifactType.Image) "Rendering image..." else "Render image",
                    onClick = { onGenerateMedia(MediaArtifactType.Image) },
                    modifier = Modifier.weight(1f),
                    active = state.generatingMediaType == MediaArtifactType.Image,
                )
                PixelButton(
                    label = if (state.generatingMediaType == MediaArtifactType.Video) "Rendering video..." else "Render video",
                    onClick = { onGenerateMedia(MediaArtifactType.Video) },
                    modifier = Modifier.weight(1f),
                    active = state.generatingMediaType == MediaArtifactType.Video,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Images are saved locally. Videos are saved locally when provider bytes/download are available; otherwise the generated remote URI is shown.",
                style = MaterialTheme.typography.bodySmall,
                color = ForgeColor.Muted,
            )
        }

        ForgePanel {
            SectionHeader("Generated Media", trailing = "${state.mediaArtifacts.size} FILES")
            Spacer(modifier = Modifier.height(10.dp))
            if (state.mediaArtifacts.isEmpty()) {
                Text(
                    text = "No rendered media yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ForgeColor.Muted,
                )
            } else {
                state.mediaArtifacts.forEach { artifact ->
                    MediaArtifactItem(
                        artifact = artifact,
                        onView = { selectedArtifact = artifact },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        ForgePanel {
            SectionHeader("Trend Opportunities", trailing = "${state.opportunities.size} SOURCES")
            Spacer(modifier = Modifier.height(10.dp))
            if (state.opportunities.isEmpty()) {
                Text(
                    text = "No trend opportunities available yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ForgeColor.Muted,
                )
            } else {
                state.opportunities.forEach { opportunity ->
                    OpportunityGenerationItem(
                        opportunity = opportunity,
                        generatingTrendId = state.generatingTrendId,
                        generatingFormat = state.generatingFormat,
                        onGenerate = onGenerate,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        ForgePanel {
            SectionHeader("Generated Drafts", trailing = "${state.drafts.size} SAVED")
            Spacer(modifier = Modifier.height(10.dp))
            if (state.drafts.isEmpty()) {
                Text(
                    text = "No generated drafts persisted yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ForgeColor.Muted,
                )
            } else {
                state.drafts.forEach { draft ->
                    DraftItem(draft)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun MediaArtifactItem(
    artifact: ContentMediaArtifact,
    onView: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val canView = !artifact.localUri.isNullOrBlank() || !artifact.remoteUri.isNullOrBlank()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ForgeColor.White.copy(alpha = 0.34f))
            .padding(9.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusPill(label = artifact.type.label, riskLevel = RiskLevel.Low)
            StatusPill(
                label = artifact.status.name,
                riskLevel = if (artifact.status == MediaArtifactStatus.Failed) RiskLevel.High else RiskLevel.Low,
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        KeyValueLine("Model", artifact.model, valueColor = ForgeColor.Yellow)
        KeyValueLine("MIME", artifact.mimeType)
        artifact.localUri?.let { localUri ->
            KeyValueLine("Local", localUri.takeLast(34), valueColor = ForgeColor.Green)
        }
        artifact.remoteUri?.let { remoteUri ->
            KeyValueLine("Remote", remoteUri.takeLast(34), valueColor = ForgeColor.Yellow)
        }
        Spacer(modifier = Modifier.height(8.dp))
        when (artifact.type) {
            MediaArtifactType.Image -> ImagePreview(
                localUri = artifact.localUri,
                onView = onView,
            )
            MediaArtifactType.Video -> VideoPreview(artifact.localUri, artifact.remoteUri)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PixelButton(
                label = if (canView) "View full" else "No file",
                onClick = { if (canView) onView() },
                modifier = Modifier.weight(1f),
                active = canView,
            )
            artifact.remoteUri?.let { remoteUri ->
                PixelButton(
                    label = "Open remote",
                    onClick = {
                        if (remoteUri.startsWith("http://") || remoteUri.startsWith("https://")) {
                            uriHandler.openUri(remoteUri)
                        }
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = artifact.prompt,
            style = MaterialTheme.typography.bodySmall,
            color = ForgeColor.Muted,
            maxLines = 5,
            overflow = TextOverflow.Ellipsis,
        )
        artifact.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = ForgeColor.Red,
            )
        }
    }
}

@Composable
private fun ImagePreview(
    localUri: String?,
    onView: () -> Unit,
) {
    val bitmap = remember(localUri) { decodeLocalBitmap(localUri) }
    if (bitmap == null) {
        Text(
            text = "Image file unavailable.",
            style = MaterialTheme.typography.bodyMedium,
            color = ForgeColor.Muted,
        )
    } else {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Generated image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .border(1.dp, ForgeColor.Yellow.copy(alpha = 0.5f))
                .clickable(onClick = onView),
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = "Tap image to view full size.",
            style = MaterialTheme.typography.bodySmall,
            color = ForgeColor.Green,
        )
    }
}

@Composable
private fun MediaArtifactViewerDialog(
    artifact: ContentMediaArtifact,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ForgeColor.Black.copy(alpha = 0.98f))
                .padding(12.dp)
                .border(1.dp, ForgeColor.Yellow.copy(alpha = 0.72f))
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${artifact.type.label} Viewer",
                        style = MaterialTheme.typography.titleMedium,
                        color = ForgeColor.Yellow,
                    )
                    Text(
                        text = artifact.model,
                        style = MaterialTheme.typography.bodySmall,
                        color = ForgeColor.Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                PixelButton(
                    label = "Close",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(0.32f),
                )
            }

            when (artifact.type) {
                MediaArtifactType.Image -> FullImagePreview(artifact.localUri)
                MediaArtifactType.Video -> VideoPreview(artifact.localUri, artifact.remoteUri)
            }

            KeyValueLine("Status", artifact.status.name, valueColor = if (artifact.status == MediaArtifactStatus.Failed) ForgeColor.Red else ForgeColor.Green)
            artifact.localUri?.let { KeyValueLine("Local file", it.takeLast(42), valueColor = ForgeColor.Green) }
            artifact.remoteUri?.let { KeyValueLine("Remote file", it.takeLast(42), valueColor = ForgeColor.Yellow) }
            Text(
                text = artifact.prompt,
                style = MaterialTheme.typography.bodyMedium,
                color = ForgeColor.White,
            )
            artifact.errorMessage?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ForgeColor.Red,
                )
            }
        }
    }
}

@Composable
private fun FullImagePreview(localUri: String?) {
    val bitmap = remember(localUri) { decodeLocalBitmap(localUri) }
    if (bitmap == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 320.dp)
                .border(1.dp, ForgeColor.White.copy(alpha = 0.34f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Image file unavailable.",
                style = MaterialTheme.typography.bodyMedium,
                color = ForgeColor.Muted,
            )
        }
    } else {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Generated image full view",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 320.dp, max = 620.dp)
                .border(1.dp, ForgeColor.Yellow.copy(alpha = 0.64f)),
        )
    }
}

private fun decodeLocalBitmap(localUri: String?) =
    localUri
        ?.let { Uri.parse(it).path }
        ?.let { BitmapFactory.decodeFile(it) }

@Composable
private fun VideoPreview(localUri: String?, remoteUri: String?) {
    val playableUri = localUri ?: remoteUri
    if (playableUri.isNullOrBlank()) {
        Text(
            text = "Video URI unavailable.",
            style = MaterialTheme.typography.bodyMedium,
            color = ForgeColor.Muted,
        )
        return
    }
    AndroidView(
        factory = { context ->
            VideoView(context).apply {
                setVideoURI(Uri.parse(playableUri))
                setMediaController(MediaController(context).also { controller ->
                    controller.setAnchorView(this)
                })
                setOnPreparedListener { player ->
                    player.isLooping = true
                    seekTo(1)
                }
            }
        },
        update = { view ->
            view.setVideoURI(Uri.parse(playableUri))
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .border(1.dp, ForgeColor.Yellow.copy(alpha = 0.5f)),
    )
}

@Composable
private fun OpportunityGenerationItem(
    opportunity: TrendOpportunity,
    generatingTrendId: String?,
    generatingFormat: ContentFormat?,
    onGenerate: (String, ContentFormat) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ForgeColor.White.copy(alpha = 0.34f))
            .padding(9.dp),
    ) {
        Text(
            text = opportunity.title,
            style = MaterialTheme.typography.titleMedium,
            color = ForgeColor.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(5.dp))
        KeyValueLine("Opportunity", opportunity.opportunityScore.percent(), valueColor = ForgeColor.Green)
        SignalBar(progress = opportunity.opportunityScore, color = ForgeColor.Green)
        Spacer(modifier = Modifier.height(8.dp))
        ContentFormat.entries.forEach { format ->
            val generating = generatingTrendId == opportunity.id && generatingFormat == format
            PixelButton(
                label = if (generating) "Generating ${format.label}" else "Generate ${format.label}",
                onClick = { onGenerate(opportunity.id, format) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                active = generating,
            )
        }
    }
}

@Composable
private fun DraftItem(draft: ContentDraft) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ForgeColor.White.copy(alpha = 0.34f))
            .padding(9.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = draft.format.label,
                style = MaterialTheme.typography.labelMedium,
                color = ForgeColor.Yellow,
                maxLines = 1,
            )
            Text(
                text = draft.opportunityScore.percent(),
                style = MaterialTheme.typography.labelMedium,
                color = ForgeColor.Green,
                maxLines = 1,
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = draft.title,
            style = MaterialTheme.typography.titleMedium,
            color = ForgeColor.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = draft.content,
            style = MaterialTheme.typography.bodyMedium,
            color = ForgeColor.Muted,
            maxLines = 8,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = draft.whyGenerated,
            style = MaterialTheme.typography.bodySmall,
            color = ForgeColor.Green,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun Float.percent(): String =
    "${(coerceIn(0f, 1f) * 100).roundToInt()}%"
