package com.brandforge.app.presentation.navigation

enum class BrandForgeDestination(
    val label: String,
    val shortLabel: String,
) {
    BrandDnaSetup("Brand DNA", "DNA"),
    Command("Command", "CMD"),
    Agents("Agents", "AGT"),
    Memory("Memory", "MEM"),
    Trends("Trends", "TRD"),
    Studio("Studio", "STD"),
    Leads("Leads", "LED"),
    PrRisk("PR Audit", "PR"),
    Competitors("Competitors", "CMP"),
    WarRoom("War Room", "WAR"),
    Twin("Twin", "TWN"),
}
