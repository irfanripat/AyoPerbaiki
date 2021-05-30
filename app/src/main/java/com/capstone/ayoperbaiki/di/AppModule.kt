package com.capstone.ayoperbaiki.di

import com.capstone.ayoperbaiki.core.domain.usecase.ReportInteractor
import com.capstone.ayoperbaiki.core.domain.usecase.ReportUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    abstract fun provideReportUseCase(reportInteractor: ReportInteractor) : ReportUseCase
}