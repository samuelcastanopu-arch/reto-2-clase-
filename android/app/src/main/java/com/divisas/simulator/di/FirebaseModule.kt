package com.divisas.simulator.di

import com.divisas.simulator.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.functions.FirebaseFunctions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        val auth = FirebaseAuth.getInstance()
        if (BuildConfig.USE_FIREBASE_EMULATOR) {
            auth.useEmulator(BuildConfig.EMULATOR_HOST, 9099)
        }
        return auth
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        val firestore = FirebaseFirestore.getInstance()
        if (BuildConfig.USE_FIREBASE_EMULATOR) {
            firestore.useEmulator(BuildConfig.EMULATOR_HOST, 8080)
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build()
            firestore.firestoreSettings = settings
        }
        return firestore
    }

    @Provides
    @Singleton
    fun provideFirebaseFunctions(): FirebaseFunctions {
        val functions = FirebaseFunctions.getInstance()
        if (BuildConfig.USE_FIREBASE_EMULATOR) {
            functions.useEmulator(BuildConfig.EMULATOR_HOST, 5001)
        }
        return functions
    }
}
