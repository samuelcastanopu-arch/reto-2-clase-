package com.divisas.simulator.data.repository

import android.content.Context
import android.net.Uri
import com.divisas.simulator.core.constants.AppConstants
import com.divisas.simulator.core.result.Resource
import com.divisas.simulator.domain.repository.MediaRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

class MediaRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions
) : MediaRepository {

    override suspend fun uploadProfilePhoto(imageUri: Uri): Resource<String> = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext Resource.Error("Usuario no autenticado.")

        try {
            // 1. Obtener firma efímera desde Cloud Function segura
            val signatureCall = functions
                .getHttpsCallable(AppConstants.Functions.GET_CLOUDINARY_SIGNATURE)
                .call(hashMapOf("folder" to "profile_photos"))
                .await()

            @Suppress("UNCHECKED_CAST")
            val sigData = signatureCall.getData() as? Map<String, Any>
                ?: return@withContext Resource.Error("No se pudo obtener la firma de subida.")

            val signature = sigData["signature"] as? String ?: ""
            val timestamp = (sigData["timestamp"] as? Number)?.toLong() ?: 0L
            val apiKey = sigData["apiKey"] as? String ?: ""
            val cloudName = sigData["cloudName"] as? String ?: ""
            val folder = sigData["folder"] as? String ?: "profile_photos"
            val publicId = sigData["publicId"] as? String ?: "user_$uid"

            // 2. Leer bytes de la imagen
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: return@withContext Resource.Error("No se pudo leer la imagen seleccionada.")
            val imageBytes = inputStream.use { it.readBytes() }

            // 3. Subir directamente a Cloudinary mediante POST multipart/form-data
            val uploadUrl = URL("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
            val boundary = "===Boundary" + System.currentTimeMillis() + "==="
            val connection = (uploadUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                doInput = true
                useCaches = false
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            }

            DataOutputStream(connection.outputStream).use { dos ->
                fun writeFormField(name: String, value: String) {
                    dos.writeBytes("--$boundary\r\n")
                    dos.writeBytes("Content-Disposition: form-data; name=\"$name\"\r\n\r\n")
                    dos.writeBytes("$value\r\n")
                }

                writeFormField("api_key", apiKey)
                writeFormField("timestamp", timestamp.toString())
                writeFormField("signature", signature)
                writeFormField("folder", folder)
                writeFormField("public_id", publicId)
                writeFormField("overwrite", "true")

                // Archivo
                dos.writeBytes("--$boundary\r\n")
                dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"profile.jpg\"\r\n")
                dos.writeBytes("Content-Type: image/jpeg\r\n\r\n")
                dos.write(imageBytes)
                dos.writeBytes("\r\n")

                dos.writeBytes("--$boundary--\r\n")
                dos.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseString = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseString)
                val secureUrl = json.getString("secure_url")

                // 4. Actualizar referencia en Firestore
                firestore.collection(AppConstants.Collections.USERS)
                    .doc(uid)
                    .update(
                        mapOf(
                            "photoUrl" to secureUrl,
                            "photoPublicId" to publicId
                        )
                    )
                    .await()

                Resource.Success(secureUrl)
            } else {
                val errorString = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                Resource.Error("Error en Cloudinary ($responseCode): $errorString")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al subir la fotografía.", e)
        }
    }
}
