package com.example.bitlist

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.webkit.MimeTypeMap

object Constants {
    const val BOARD: String="board"
    const val USER:String="user"
    const val EMAIL:String="email"
    const val ID:String="id"
    const val POSITION:String="position"
    const val DOCUMENT_ID:String="documentId"
    const val TASK_LIST:String="taskList"
    const val BOARD_DETAILS:String="boardDetails"
    const val TASK_DETAILS:String="taskDetails"
    const val ASSIGNED_TO:String="assignedTo"
    const val IMAGE:String="image"
    const val NAME:String="name"
    const val MOBILE:String="mobile"
    const val CREATED_BY:String="createdBy"
    const val PICK_IMAGE_REQUEST_CODE=2
    const val READ_STORAGE_PERMISSION_CODE=1
    const val DOC="doc"
    const val BASE_URL="https://fcm.googleapis.com"
    const val SERVER_KEY="AAAASUn_q-0:APA91bH-NGF3otQ51vD0unLbYYHScuK1UJLW4x2OsU6kE7PERjkzm7czMiOUiCZn-fiSgXvLUKqdUj2xRb8K0rEdtV6ZGwwP0t-jBSLVEFkPlzPoxHa7a_Luo5y-jHYrDCYKC18olAGu"
    const val CONTENT_TYPE="application/json"

    fun getFileExtension(activity: Activity,uri: Uri?):String?{ //getting extension of a file through it's uri
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }

    fun showImageChooser(activity: Activity){
        var galleryIntent= Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI) //open gallery for selecting image
        activity.startActivityForResult(galleryIntent,PICK_IMAGE_REQUEST_CODE)//open gallery with request code
    }

    fun isNetworkAvailable(context: Context):Boolean{
        val connectivityManager=context.
        getSystemService(Context.CONNECTIVITY_SERVICE) as
                ConnectivityManager

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){ //new method
            val network=connectivityManager.activeNetwork?:return false
            val activeNetwork=connectivityManager.getNetworkCapabilities(network)?:return false

            return when{
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ->  true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)-> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)-> true
                else -> false
            }
        }else{ //old way
            val networkInfo=connectivityManager.activeNetworkInfo
            return networkInfo!=null && networkInfo.isConnectedOrConnecting
        }
    }
}