package com.maropost.taxicab.view.fragments

import android.Manifest
import android.arch.lifecycle.Observer
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.kotlinpermissions.KotlinPermissions
import com.maropost.taxicab.R
import com.maropost.taxicab.utils.LatLngInterpolator
import com.maropost.taxicab.utils.MarkerAnimation
import com.maropost.taxicab.utils.Utility
import com.maropost.taxicab.viewmodel.MapsViewModel
import kotlinx.android.synthetic.main.maps_fragment.*

class MapFragment : BaseFragment(), OnMapReadyCallback {

    private var mView : View?= null
    private val REQUEST_CHECK_SETTINGS = 100
    private var mGoogleMap: GoogleMap? = null
    private var mapFragment: SupportMapFragment? = null
    private var mCurrLocationMarker: Marker? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    //private val GEOFENCE_RADIUS : Float = 500F
    private lateinit var geofencingClient :GeofencingClient
    //private var mapCircle : Circle ? = null
    //private var yourLocationMarker : MarkerOptions ?= null
    private lateinit var points : ArrayList<LatLng>
    private var firstTimeFlag: Boolean = true
    private val mapsViewModel = MapsViewModel()


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
        setTitle("")
        showNavigationDrawer(false)
        showToolbar(false)
        observeLiveDataChanges()
        initialiseListeners()
    }

    override fun onResume() {
        super.onResume()
        requestLocationUpdates()
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

    private fun observeLiveDataChanges(){
        mapsViewModel.currentLocation.observe(this, Observer { location ->
            if (firstTimeFlag && mGoogleMap != null) {
                animateCamera(location!!)
                firstTimeFlag = false
                showMarker(location)
                txtPickup.text= Utility.getInstance().getCompleteAddressString(activity!!,location.latitude,location.longitude)
            }
        })
    }

    private fun initialiseListeners() {
        lnrPickup.setOnClickListener{
        replaceFragment(SearchFragment(),true)
        }
        lnrDrop.setOnClickListener{

        }
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

    private fun requestLocationUpdates(){
        if(mGoogleMap != null)
        mapsViewModel.requestLocationUpdates(activity!!,mFusedLocationClient,mGoogleMap!!,REQUEST_CHECK_SETTINGS)
    }


    private fun animateCamera(location: Location) {
        if(mGoogleMap != null) {
            val latLng = LatLng(location.latitude, location.longitude)
            mGoogleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(getCameraPositionWithBearing(latLng)));
        }
    }

     private fun getCameraPositionWithBearing(latLng: LatLng):CameraPosition {
        return CameraPosition.Builder().target(latLng).zoom(16f).build()
    }

    private fun showMarker(currentLocation: Location) {
        val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
        if (mCurrLocationMarker == null)
            mCurrLocationMarker = mGoogleMap?.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker()).position(latLng))
        else
            MarkerAnimation.animateMarkerToGB(mCurrLocationMarker!!, latLng, LatLngInterpolator.Spherical())
    }


   private fun removeLocationUpdates() {
        if (mFusedLocationClient != null)
            mapsViewModel.removeLocationUpdates(mFusedLocationClient!!)
    }

/*
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
    */
}