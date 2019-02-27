package com.maropost.taxicab.services

import android.app.IntentService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.support.v4.app.NotificationCompat
import android.text.TextUtils
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.maropost.taxicab.R
import java.util.*

class LocationAlertIntentService : IntentService("") {
    private val IDENTIFIER = "LocationAlertIS";


    override fun onHandleIntent(intent: Intent) {

        val geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Log.e(IDENTIFIER, "" + getErrorString(geofencingEvent.getErrorCode()));
            return;
        }

        Log.i(IDENTIFIER, geofencingEvent.toString());

         val geofenceTransition = geofencingEvent.getGeofenceTransition()

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {

            val triggeringGeofences = geofencingEvent.getTriggeringGeofences()

            val transitionDetails = getGeofenceTransitionInfo(
                    triggeringGeofences)

            val transitionType = getTransitionString(geofenceTransition);


            notifyLocationAlert(transitionType, transitionDetails);
        }
    }

    private fun getGeofenceTransitionInfo(triggeringGeofences: List<Geofence>): String {
        val locationNames = ArrayList<String>()
        for (i in 0 until triggeringGeofences.size) {
        locationNames.add(getLocationName(triggeringGeofences[i].requestId))
    }
        val triggeringLocationsString = TextUtils.join(", ", locationNames)

        return triggeringLocationsString
    }

    private fun getLocationName(key: String): String {
        val strs = key.split("-")

        var locationName = "";
        if (strs.size == 2) {
            val lat = strs[0]
            val lng = strs[1]

            locationName = getLocationNameGeocoder(lat.toDouble(), lng.toDouble());
        }
        if (!TextUtils.isEmpty(locationName)) {
            return locationName
        } else {
            return key
        }
    }

    private fun getLocationNameGeocoder(lat: Double,lng: Double) : String {
        val geocoder = Geocoder(this, Locale.getDefault());
        var addresses : List<Address> ?= null

        try {
            addresses = geocoder.getFromLocation(lat, lng, 1);
        } catch (ioException: Exception) {
            Log.e("", "Error in getting location name for the location");
        }

        if (addresses == null || addresses.isEmpty()) {
            Log.d("", "no location name");
            return "";
        } else {
            val address = addresses[0]
            val addressInfo = ArrayList<String>()
            for (i in 0 until  address.maxAddressLineIndex) {
                addressInfo.add(address.getAddressLine(i));
            }

            return TextUtils.join(System.getProperty("line.separator"), addressInfo);
        }
    }

    private fun getErrorString(errorCode: Int): String {

        return when(errorCode){
            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> "Geofence not available"
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> "geofence too many_geofences"
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> "geofence too many pending_intents"
            else -> "geofence error"
        }
    }

    private fun getTransitionString(transitionType: Int) : String{

        return when(transitionType){
            Geofence.GEOFENCE_TRANSITION_ENTER -> "location entered"
                Geofence.GEOFENCE_TRANSITION_EXIT -> "location exited"
            Geofence.GEOFENCE_TRANSITION_DWELL -> "dwell at location"
            else -> "location transition"
        }
    }

    private fun notifyLocationAlert(locTransitionType: String,locationDetails: String) {

        val CHANNEL_ID = "Zoftino";
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(locTransitionType)
            .setContentText(locationDetails);

        builder.setAutoCancel(true);

        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(0, builder.build());
    }

}