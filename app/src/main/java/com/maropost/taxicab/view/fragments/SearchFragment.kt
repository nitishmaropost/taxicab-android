package com.maropost.taxicab.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.PlaceBuffer
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.maropost.taxicab.R
import com.maropost.taxicab.view.adapters.PlaceArrayAdapter


class SearchFragment : BaseFragment(),GoogleApiClient.OnConnectionFailedListener,
    GoogleApiClient.ConnectionCallbacks {

    private var mView : View?= null
    private var searchFragmentCallbacks:SearchFragmentCallbacks ?= null
    private var mGoogleApiClient: GoogleApiClient?= null
    private var mPlaceArrayAdapter: PlaceArrayAdapter? = null
    private val GOOGLE_API_CLIENT_ID = 0
    private var mAutocompleteTextView: AutoCompleteTextView ?= null
    private var placeName = ""
    private var requestType: MapFragment.REQUEST_TYPE ?= null
    private val BOUNDS_INDIA = LatLngBounds(
        LatLng(6.4626999, 68.1097),
        LatLng(35.513327, 97.39535869999999)
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if(mView == null) {
            mView = inflater.inflate(R.layout.search_fragment, container, false)
            mAutocompleteTextView = mView?.findViewById(R.id.autoCompleteTextView) as AutoCompleteTextView
            setAutoCompleteSearch()
        }
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitle("Enter Pickup Location")
        showNavigationDrawer(false)
        showToolbar(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        mGoogleApiClient?.stopAutoManage(activity!!)
        mGoogleApiClient?.disconnect()
    }

    private fun setAutoCompleteSearch() {
        mAutocompleteTextView?.threshold = 3
        mGoogleApiClient = GoogleApiClient.Builder(activity!!)
            .addApi(Places.GEO_DATA_API)
            .enableAutoManage(activity!!, GOOGLE_API_CLIENT_ID, this)
            .addConnectionCallbacks(this)
            .build()

        mAutocompleteTextView?.onItemClickListener = mAutocompleteClickListener
        val typeFilter = AutocompleteFilter.Builder()
            .setCountry("IN")
            .build()
        mPlaceArrayAdapter = PlaceArrayAdapter(
            activity!!, android.R.layout.simple_list_item_1,
            BOUNDS_INDIA, typeFilter
        )
        mAutocompleteTextView?.setAdapter(mPlaceArrayAdapter)
    }

    private val mAutocompleteClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
        val item = mPlaceArrayAdapter?.getItem(position)
        placeName = item.toString()
        val placeId = item?.placeId
        val placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient!!, placeId.toString())
        placeResult.setResultCallback(mUpdatePlaceDetailsCallback)
    }

    private val mUpdatePlaceDetailsCallback = object : ResultCallback<PlaceBuffer> {
        override fun onResult(places: PlaceBuffer) {
            if (!places.status.isSuccess) {
                return
            }
            // Selecting the first object buffer.
            val place = places.get(0)
            popCurrentFragment()
            if(requestType == MapFragment.REQUEST_TYPE.PICKUP)
                searchFragmentCallbacks?.onPickupLocationSelected(place,placeName)
            else
                searchFragmentCallbacks?.onDropLocationSelected(place,placeName)
            //places.release()
        }
    }

    override fun onConnected(p0: Bundle?) {
        mPlaceArrayAdapter?.setGoogleApiClient(mGoogleApiClient)
    }
    override fun onConnectionSuspended(i: Int) {
        mPlaceArrayAdapter?.setGoogleApiClient(null)
    }
    override fun onConnectionFailed(connectionResult: ConnectionResult) {
    }

    fun initialiseCallback(searchFragmentCallbacks: SearchFragmentCallbacks,
                           requestType: MapFragment.REQUEST_TYPE) {
        this.searchFragmentCallbacks = searchFragmentCallbacks
        this.requestType = requestType
    }

    interface SearchFragmentCallbacks{
        fun onPickupLocationSelected(place: Place, placeName: String)
        fun onDropLocationSelected(place: Place, placeName: String)
    }
}