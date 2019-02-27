package com.maropost.taxicab.view.fragments

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.support.v4.view.ViewCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.maropost.taxicab.R
import kotlinx.android.synthetic.main.splash_fragment.*

class SplashFragment : BaseFragment() {

    private var mView : View?= null
    private val handler = Handler()
    private var animationDrawable: AnimationDrawable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if(mView == null)
            mView = inflater.inflate(R.layout.splash_fragment, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showNavigationDrawer(false)
        showToolbar(false)
        setupTransitionAnimation()
        launchView()
    }

    /**
     * Setup animation to be performed for the view background
     */
    private fun setupTransitionAnimation() {
        // initializing animation drawable by getting background from parent layout
        animationDrawable = relSplashParent.background as AnimationDrawable?
        // setting enter fade animation duration to 1 second
        animationDrawable?.setEnterFadeDuration(1000)
        // setting exit fade animation duration to 1 second
        animationDrawable?.setExitFadeDuration(1000)

        startAnimation()
    }

    /**
     * Start transition element animation
     */
    private fun startAnimation(){
        if (animationDrawable != null && !animationDrawable!!.isRunning) {
            // start the animation
            animationDrawable?.start()
        }
    }

    /**
     * Stop transition element animation
     */
    private fun stopAnimation(){
        if (animationDrawable != null && animationDrawable!!.isRunning) {
            // stop the animation
            animationDrawable?.stop()
        }
    }

    /**
     * Display the screen depending on user key present in preference or not
     * If available - Open Home screen i.e. user has already logged in
     * If unavailable - Open Login screen
     */
    private fun launchView() {
        handler.postDelayed({
            stopAnimation()
            ViewCompat.setTransitionName(lnrTransition, getString(R.string.simple_fragment_transition))
            replaceFragment(LoginFragment(),false,lnrTransition)
            //replaceFragment(HomeFragment(),true)
        }, 2000)
    }

    override fun onPause() {
        super.onPause()
        stopAnimation()
    }
}