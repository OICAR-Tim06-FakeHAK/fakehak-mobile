package hr.algebra.myapplication.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hr.algebra.myapplication.data.AuthPreferences
import hr.algebra.myapplication.data.EncryptedAuthPreferences
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesModule {

    @Binds
    @Singleton
    abstract fun bindAuthPreferences(impl: EncryptedAuthPreferences): AuthPreferences
}
