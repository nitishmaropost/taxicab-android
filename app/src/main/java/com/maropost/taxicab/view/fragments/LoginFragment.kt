package com.maropost.taxicab.view.fragments

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.maropost.taxicab.R
import kotlinx.android.synthetic.main.login_fragment.*

class LoginFragment : BaseFragment() {

    private var mView : View?= null
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
               .setDuration(600) //This param can also be neglected to use default duration
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if(mView == null)
            mView = inflater.inflate(R.layout.login_fragment, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showNavigationDrawer(false)
        showToolbar(false)
        handler.postDelayed({
            replaceFragment(MapFragment(),true)
        }, 2000)


        //***************** Animate edit text with transition animation ***********************

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            lnrLayout.transitionName = getString(R.string.simple_fragment_transition)
        }
    }

}