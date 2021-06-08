package com.capstone.ayoperbaiki.core.data.source.firebase

import android.net.Uri
import android.util.Log
import com.capstone.ayoperbaiki.core.data.Resource
import com.capstone.ayoperbaiki.core.data.source.firebase.response.ReportResponse
import com.capstone.ayoperbaiki.core.domain.model.Report
import com.capstone.ayoperbaiki.core.utils.Constants.REF_NAME
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.storage.StorageReference
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class FirebaseDataSource @Inject constructor(
    private val dbCollection: CollectionReference,
    private val storage: StorageReference
) {

    suspend fun getAllReport() : Resource<List<ReportResponse>> =
        suspendCoroutine { cont ->
            dbCollection
                .get()
                .addOnSuccessListener {
                    try {
                        cont.resume(Resource.Success(it.toObjects()))
                    } catch (e: Exception) {
                        cont.resume(Resource.Failure(e))
                    }
                }
                .addOnFailureListener {
                    cont.resume(Resource.Failure(it))
                }
        }

    suspend fun newReport(report: Report) : Resource<Boolean>  =
        suspendCoroutine { cont ->
            dbCollection
                .add(report)
                .addOnSuccessListener {
                    cont.resume(Resource.Success(true))
                }.addOnFailureListener {
                    cont.resume(Resource.Failure(it))
                }
        }

    fun uploadImageWithUri(uri : Uri, block: ((Resource<Uri>, Int) -> Unit)?) {
        Log.d("DisasterReportForm", "uploadImage: string 1 di fire $uri")
        val photoRef = storage.child(REF_NAME).child(uri.lastPathSegment!!)

        photoRef.putFile(uri)
            .addOnProgressListener { taskSnapshot ->
                val percentComplete = if (taskSnapshot.totalByteCount > 0) {
                    (100 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                } else 0

                block?.invoke(Resource.Loading(), percentComplete)
            }
            .continueWithTask { task ->
                // Forward any exceptions
                if (!task.isSuccessful) {
                    throw task.exception!!
                }
                // Request the public download URL
                photoRef.downloadUrl
            }
            .addOnSuccessListener {
                block?.invoke(Resource.Success(it), 100)
            }
            .addOnFailureListener {
                block?.invoke(Resource.Failure(it), 0)
            }
    }

}

data class UploadingProgress(
    val progress: Int = 0,
    val result: Resource<Boolean>
)