package com.maropost.taxicab.view.fragments

import android.Manifest
import android.arch.lifecycle.Observer
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.Place
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

class MapFragment : BaseFragment(), OnMapReadyCallback, SearchFragment.SearchFragmentCallbacks {

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
    private var mTxtPickUp: TextView ?= null
    private var txtDropAt: TextView ?= null
    private var txtDrop: TextView ?= null
    private var btnRideNow: Button ? = null
    private var originLatLng : LatLng ?= null
    private var destLatLng : LatLng ?= null

    enum class REQUEST_TYPE{
        PICKUP,
        DROP
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if(mView == null) {
            mView = inflater.inflate(R.layout.maps_fragment, container, false)
            points = ArrayList()
            mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mTxtPickUp = mView?.findViewById(R.id.txtPickup) as TextView
            btnRideNow = mView?.findViewById(R.id.btnRideNow) as Button
            txtDropAt = mView?.findViewById(R.id.txtDropAt) as TextView
            txtDrop = mView?.findViewById(R.id.txtDrop) as TextView
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
            geofencingClient = LocationServices.getGeofencingClient(activity!!)
            mapFragment?.getMapAsync(this)
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
        mapFragment?.onResume()
        //requestLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        mapFragment?.onPause()
        //removeLocationUpdates()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        mGoogleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
        checkForLocationPermission()
    }

    private fun observeLiveDataChanges(){
        mapsViewModel.currentLocation.observe(this, Observer { location ->
            if (firstTimeFlag && mGoogleMap != null) {

                firstTimeFlag = false
                originLatLng = LatLng(location!!.latitude, location.longitude)
                showMarker(originLatLng!!)
                animateCamera(originLatLng!!)
                mTxtPickUp?.text= Utility.getInstance().getCompleteAddressString(activity!!,location.latitude,location.longitude)
                removeLocationUpdates()
            }
        })

        mapsViewModel.mLineOptions.observe(this, Observer { lineOptions ->
            if(mGoogleMap != null)
                mGoogleMap?.addPolyline(lineOptions)
        })
    }

    private fun initialiseListeners() {
        lnrPickup.setOnClickListener{
            openSearchFragment(REQUEST_TYPE.PICKUP)
        }
        lnrDrop.setOnClickListener{
            openSearchFragment(REQUEST_TYPE.DROP)
        }
        btnRideNow?.setOnClickListener{
            btnRideNow?.visibility = View.INVISIBLE
            mapsViewModel.getDirectionsUrl(originLatLng, destLatLng)
        }
    }

    private fun openSearchFragment(requestType: REQUEST_TYPE) {
        val searchFragment = SearchFragment()
        searchFragment.initialiseCallback(this,requestType)
        replaceFragment(searchFragment,true)
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


    private fun animateCamera(latLng: LatLng) {
        if(mGoogleMap != null) {
            //val latLng = LatLng(location.latitude, location.longitude)
            mGoogleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(getCameraPositionWithBearing(latLng)))
        }
    }

    private fun getCameraPositionWithBearing(latLng: LatLng):CameraPosition {
        return CameraPosition.Builder().target(latLng).zoom(16f).build()
    }

    private fun showMarker(latLng: LatLng) {
        //val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
        if (mCurrLocationMarker == null)
            mCurrLocationMarker = mGoogleMap?.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker()).position(latLng))
        else
            MarkerAnimation.animateMarkerToGB(mCurrLocationMarker!!, latLng, LatLngInterpolator.Spherical())
    }


    private fun removeLocationUpdates() {
        if (mFusedLocationClient != null)
            mapsViewModel.removeLocationUpdates(mFusedLocationClient!!)
    }

    override fun onPickupLocationSelected(place: Place, placeName: String) {
        originLatLng = place.latLng
        showMarker(originLatLng!!)
        animateCamera(originLatLng!!)
        mTxtPickUp?.text= placeName
        validatePickupAndDropDetails()
    }

    override fun onDropLocationSelected(place: Place, placeName: String) {
        destLatLng = place.latLng
        mCurrLocationMarker = mGoogleMap?.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker()).position(destLatLng!!))
        animateCamera(destLatLng!!)
        validatePickupAndDropDetails()
        txtDropAt?.visibility = View.VISIBLE
        txtDrop?.text = placeName
    }

    /**
     * Check if origin and destination latlng details are available, then draw route b/w two points
     */
    private fun validatePickupAndDropDetails() {
        if(originLatLng  != null && destLatLng != null)
            btnRideNow?.visibility = View.VISIBLE
        else
            btnRideNow?.visibility = View.INVISIBLE
    }
}