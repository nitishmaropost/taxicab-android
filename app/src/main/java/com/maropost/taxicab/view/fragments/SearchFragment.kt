package com.maropost.taxicab.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import com.maropost.taxicab.R
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceSelectionListener
import com.google.android.gms.location.places.AutocompleteFilter


class SearchFragment : BaseFragment() {

    private var mView : View?= null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if(mView == null) {
            mView = inflater.inflate(R.layout.search_fragment, container, false)
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

    private fun setAutoCompleteSearch() {
        val autocompleteFragment =
            fragmentManager!!.findFragmentById(R.id.place_autocomplete_fragment) as PlaceAutocompleteFragment?
        val typeFilter = AutocompleteFilter.Builder()
            .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
            .build()
        autocompleteFragment?.setFilter(typeFilter)

        autocompleteFragment?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {

                Log.i("", "Place: " + place.name)//get place details here
            }

            override fun onError(p0: Status?) {

            }
        })
    }
}