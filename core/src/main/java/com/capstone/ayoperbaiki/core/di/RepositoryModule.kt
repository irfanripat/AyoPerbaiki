package com.capstone.ayoperbaiki.core.di

import com.capstone.ayoperbaiki.core.data.ReportRepository
import com.capstone.ayoperbaiki.core.domain.repository.IReportRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module(includes = [FirebaseModule::class])
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun provideRepository(reportRepository: ReportRepository) : IReportRepository

}