package com.divisas.simulator.domain.repository

import android.net.Uri
import com.divisas.simulator.core.result.Resource

interface MediaRepository {
    suspend fun uploadProfilePhoto(imageUri: Uri): Resource<String>
}
