package com.brandforge.app.data.lead.di

import com.brandforge.app.data.lead.LeadRepositoryImpl
import com.brandforge.app.domain.lead.LeadRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LeadRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindLeadRepository(
        implementation: LeadRepositoryImpl,
    ): LeadRepository
}
