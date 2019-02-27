package com.maropost.taxicab.view.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.kotlinpermissions.KotlinPermissions
import com.maropost.taxicab.R
import com.maropost.taxicab.services.LocationAlertIntentService
import com.maropost.taxicab.utils.Constants
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MapFragment : BaseFragment(), OnMapReadyCallback {

    private var mView : View?= null
    private val REQUEST_CHECK_SETTINGS = 100
    private var mGoogleMap: GoogleMap? = null
    private var mapFragment: SupportMapFragment? = null
    private var mLocationRequest: LocationRequest? = null
    private var mLastLocation: Location? = null
    private var mCurrLocationMarker: Marker? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    //meters
    private val GEOFENCE_RADIUS : Float = 500F
    //in milli seconds
    private val GEOFENCE_EXPIRATION = 6000
    private lateinit var geofencingClient :GeofencingClient
    private var mapCircle : Circle ? = null
    //private var yourLocationMarker : MarkerOptions ?= null
    private lateinit var points : ArrayList<LatLng>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if(mView == null) {
            mView = inflater.inflate(R.layout.maps_fragment, container, false)
            points = ArrayList()
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
            geofencingClient = LocationServices.getGeofencingClient(activity!!)
            mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment?.getMapAsync(this);
        }
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitle(resources.getString(R.string.Fragment2))
        showNavigationDrawer(false)
        showToolbar(false)
    }

    override fun onPause() {
        super.onPause()
        //stop location updates when Activity is no longer active
        removeLocationUpdates()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        mGoogleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
        checkForLocationPermission()
    }



    private fun checkForLocationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity!!,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                requestLocationUpdates()
            } else{
                KotlinPermissions.with(activity!!) // where this is an FragmentActivity instance
                    .permissions(Manifest.permission.ACCESS_FINE_LOCATION)
                    .onAccepted { permissions ->
                        //List of accepted permissions
                        requestLocationUpdates()
                    }
                    .ask()
            }
        } else {
            requestLocationUpdates()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates(){
        mLocationRequest = LocationRequest()
        mLocationRequest?.interval = 12000 // two minute interval
        mLocationRequest?.fastestInterval = 12000
        mLocationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(mLocationRequest!!)
        builder.setAlwaysShow(true)
        val locationServices = LocationServices.getSettingsClient(activity!!).checkLocationSettings(builder.build());
        locationServices.addOnCompleteListener { task ->
            try {
                // The request for location has been provided. Just a simple response callback is provided
                // If required, can have a look at the response then.
                // NOTE -> Location will automatically be fetched because it has already been requested in the call outside the block
                val response = task.getResult(ApiException::class.java)
            }
            catch (exception: ApiException) {
                when(exception.statusCode){
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->{
                        // Location settings are not satisfied. But could be fixed by showing the user a dialog.
                        try {
                            // Cast to a resolvable exception.
                            val resolvable = exception as ResolvableApiException
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            resolvable.startResolutionForResult(activity,REQUEST_CHECK_SETTINGS);
                        }
                        catch (e: IntentSender.SendIntentException) {
                            // Ignore the error.
                        }
                        catch (e: ClassCastException) {
                            // Ignore, should be an impossible error.
                        }
                    }
                }
            }
        }
        mFusedLocationClient?.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
        mGoogleMap?.isMyLocationEnabled = true
    }


    /**
     * Callback for location received
     */
    internal var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.size > 0) {
                //The last location in the list is the newest
                val location = locationList[locationList.size - 1]
                Log.i("MapsActivity", "Location: " + location.latitude + " " + location.longitude)
                mLastLocation = location
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker?.remove()
                }

                //Place current location marker
                val latLng = LatLng(location.latitude, location.longitude)
                Toast.makeText(activity,
                    "Latitude :" + location.latitude + " " + "Longitude " + location.longitude,
                    Toast.LENGTH_LONG).show()
                val markerOptions = MarkerOptions()
                markerOptions.position(latLng)
                markerOptions.title("Current Position")
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                mCurrLocationMarker = mGoogleMap?.addMarker(markerOptions)

                //move map camera
                mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11f))

                // If do not want location updates further, remove them.
                //removeLocationUpdates()
            }
        }
    }

    private fun removeLocationUpdates() {
        if (mFusedLocationClient != null)
            mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
    }

    @SuppressLint("MissingPermission")
    private fun addLocationAlert(lat: Double, lng: Double){

        val key : String = "" + lat + "" + lng
        val geofence = getGeofence(lat, lng, key)

        if(mapCircle != null) {
            mapCircle?.remove()
            mapCircle = null
        }
        mapCircle = mGoogleMap?.addCircle(CircleOptions()
            .center(LatLng(lat,lng))
            .radius(GEOFENCE_RADIUS.toDouble())
            .strokeColor(Color.RED)
            .fillColor(Color.BLUE))


        geofencingClient.addGeofences(getGeofencingRequest(geofence),
            getGeofencePendingIntent())
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                    Toast.makeText(activity, "Location alert has been added", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(activity, "Location alert could not be added", Toast.LENGTH_SHORT).show();
            }
    }

    private  fun getGeofencePendingIntent() : PendingIntent {
        val intent = Intent(activity, LocationAlertIntentService::class.java)
        return PendingIntent.getService(activity, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private fun getGeofencingRequest(geofence: Geofence): GeofencingRequest {
        val builder = GeofencingRequest.Builder()
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofence(geofence)
        return builder.build()
    }

    private fun getGeofence(lat: Double,lang: Double,key: String) : Geofence {
        return Geofence.Builder()
            .setRequestId(key)
            .setCircularRegion(lat, lang, GEOFENCE_RADIUS)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .setLoiteringDelay(10000)
            .build();
    }
}