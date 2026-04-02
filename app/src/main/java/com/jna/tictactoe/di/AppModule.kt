package com.jna.tictactoe.di

import android.content.Context
import com.jna.tictactoe.audio.SoundManager
import com.jna.tictactoe.network.discovery.NsdDiscoveryManager
import com.jna.tictactoe.network.socket.GameSocketManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSoundManager(@ApplicationContext context: Context): SoundManager {
        return SoundManager(context)
    }

    @Provides
    @Singleton
    fun provideGameSocketManager(): GameSocketManager {
        return GameSocketManager()
    }

    @Provides
    @Singleton
    fun provideNsdDiscoveryManager(@ApplicationContext context: Context): NsdDiscoveryManager {
        return NsdDiscoveryManager(context)
    }
}
