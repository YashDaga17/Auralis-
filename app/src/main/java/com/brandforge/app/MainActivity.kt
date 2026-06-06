package com.brandforge.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.brandforge.app.presentation.BrandForgeApp
import com.brandforge.app.presentation.commandcenter.CommandCenterViewModel
import com.brandforge.app.presentation.competitor.CompetitorViewModel
import com.brandforge.app.presentation.content.ContentStudioViewModel
import com.brandforge.app.presentation.debug.DebugViewModel
import com.brandforge.app.presentation.lead.LeadInboxViewModel
import com.brandforge.app.presentation.memory.BrandDnaViewModel
import com.brandforge.app.presentation.memory.MemoryViewModel
import com.brandforge.app.presentation.pr.PrRiskAuditViewModel
import com.brandforge.app.presentation.trend.TrendRadarViewModel
import com.brandforge.app.presentation.twin.TwinChatViewModel
import com.brandforge.app.presentation.warroom.WarRoomViewModel
import com.brandforge.app.ui.theme.BrandForgeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: CommandCenterViewModel by viewModels()
    private val brandDnaViewModel: BrandDnaViewModel by viewModels()
    private val memoryViewModel: MemoryViewModel by viewModels()
    private val trendRadarViewModel: TrendRadarViewModel by viewModels()
    private val contentStudioViewModel: ContentStudioViewModel by viewModels()
    private val twinChatViewModel: TwinChatViewModel by viewModels()
    private val leadInboxViewModel: LeadInboxViewModel by viewModels()
    private val competitorViewModel: CompetitorViewModel by viewModels()
    private val debugViewModel: DebugViewModel by viewModels()
    private val prRiskAuditViewModel: PrRiskAuditViewModel by viewModels()
    private val warRoomViewModel: WarRoomViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BrandForgeTheme {
                BrandForgeApp(
                    viewModel = viewModel,
                    brandDnaViewModel = brandDnaViewModel,
                    memoryViewModel = memoryViewModel,
                    trendRadarViewModel = trendRadarViewModel,
                    contentStudioViewModel = contentStudioViewModel,
                    twinChatViewModel = twinChatViewModel,
                    leadInboxViewModel = leadInboxViewModel,
                    competitorViewModel = competitorViewModel,
                    debugViewModel = debugViewModel,
                    prRiskAuditViewModel = prRiskAuditViewModel,
                    warRoomViewModel = warRoomViewModel,
                )
            }
        }
    }
}
