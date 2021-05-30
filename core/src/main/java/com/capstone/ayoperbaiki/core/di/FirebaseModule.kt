package com.capstone.ayoperbaiki.core.di

import com.capstone.ayoperbaiki.core.utils.Constants.REF_NAME
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class FirebaseModule {

    @Provides
    fun provideDbInstance() : FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    fun provideDatabaseReference(database : FirebaseFirestore) : CollectionReference {
        return database.collection(REF_NAME)
    }

    @Provides
    fun provideStorage() : FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Provides
    fun provideStorageReference(storage : FirebaseStorage) : StorageReference {
        return storage.reference
    }
}