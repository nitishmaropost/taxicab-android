package com.maropost.taxicab.utils

import com.maropost.taxicab.R
import com.maropost.taxicab.application.MyApplication

object Constants {

    const val DIRECTION_API = "https://maps.googleapis.com/maps/api/directions/json?origin="
    const val DIRECTORY_NAME = "YOUR_APP_NAME"
    const val DIRECTORY_MEDIA = "media"
    const val IMAGE_DIRECTORY_NAME = "images";
    const val VIDEO_DIRECTORY_NAME = "videos";
    const val WEB_BASE_URL = "http://logappserver.ignivastaging.com"
    const val LOGIN_API = "/api/users/login"

    /**
     * Enum web service request types
     */
    enum class REQUEST_TYPE (val stringValue: String) {
        POST("Post"),
        PUT("Put"),
        GET("Get"),
        DELETE("Delete");
        override fun toString(): String {
            return stringValue
        }
    }

    fun getUrl(originLat: String,originLon: String,destinationLat:String
               ,destinationLon: String): String{
        return "$DIRECTION_API$originLat,$originLon&destination=$destinationLat,$destinationLon&key=" +MyApplication.getInstance().getString(
            R.string.application_key)
    }
}